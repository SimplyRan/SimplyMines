package me.simplyran.simplymines.managers;

import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.requirements.reset.IResetRequirement;
import me.simplyran.simplymines.utils.WarnUtils;
import org.jetbrains.annotations.NotNull;

public class RunnableManager implements Runnable{

    private final MineManager mineManager;
    private final WarnUtils warnUtils;
    private long lastMineSaves;

    private final ConfigData<Integer> saveMinesSeconds = ConfigFactory.newConfigData("save_mines_seconds", 1800);

    public RunnableManager(@NotNull MineManager mineManager,
                           @NotNull ConfigManager configManager){
        this.mineManager = mineManager;
        this.warnUtils = new WarnUtils(configManager);
        configManager.register(saveMinesSeconds);
        //So we don't insta save on startup.
        this.lastMineSaves = System.currentTimeMillis() / 1000;
    }


    @Override
    public void run() {
        long now = System.currentTimeMillis() / 1000;
        boolean shouldSaveMines = now - lastMineSaves >= saveMinesSeconds.getValue();

        mineManager.getMines().forEach(mine ->
        {
            //Checking if mine is enabled
            if (!mine.isEnabled()) return;

            //Checking if any reset requirement are satisfied.
            for (IResetRequirement resetRequirement : mine.getResetRequirements()){
                if (resetRequirement.isEnabled() && resetRequirement.isSatisfied()) {
                    mine.reset();

                    //Not to reset again for no reason.
                    break;
                }
            }

            warnUtils.checkWarnings(mine, now);

            if (shouldSaveMines) {
                mineManager.saveMineAsync(mine);
            }
        });

        if (shouldSaveMines) {
            lastMineSaves = now;
        }
    }
}
