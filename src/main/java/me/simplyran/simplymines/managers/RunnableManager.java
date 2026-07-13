package me.simplyran.simplymines.managers;

import lombok.Setter;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.utils.JsonUtils;
import me.simplyran.simplymines.utils.WarnUtils;
import org.jetbrains.annotations.NotNull;

public class RunnableManager implements Runnable{

    private final MineManager mineManager;
    private final SimplyMines plugin;
    private long lastMineSaves = 0;
    @Setter private static int SAVE_MINES_FILES = 1800;

    public RunnableManager(@NotNull SimplyMines plugin,
                           @NotNull MineManager mineManager){
        this.plugin = plugin;
        this.mineManager = mineManager;
    }


    @Override
    public void run() {
        long now = System.currentTimeMillis() / 1000;
        boolean shouldSaveMines = now - lastMineSaves >= SAVE_MINES_FILES;

        mineManager.getMines().forEach(mine ->
        {
            long secondsUntilReset = mine.getResetTime() - (now - mine.getLastReset());

            if (secondsUntilReset <= 0) {
                if (mine.isEnabled()) {
                    mine.reset();
                    mine.setLastReset(now);
                }
            } else {
                WarnUtils.checkWarnings(mine, now);
            }

            if (shouldSaveMines) {
                JsonUtils.saveMine(plugin, mine);
            }
        });

        if (shouldSaveMines) {
            lastMineSaves = now;
        }
    }
    public void saveAllMines(){
        mineManager.getMines().forEach(mine -> {
            lastMineSaves = System.currentTimeMillis()/1000;
            JsonUtils.saveMine(plugin, mine);
        });
    }



}
