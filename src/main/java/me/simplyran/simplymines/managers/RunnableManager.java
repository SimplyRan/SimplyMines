package me.simplyran.simplymines.managers;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.utils.JsonUtils;
import org.jetbrains.annotations.NotNull;

public class RunnableManager implements Runnable{

    private final MineManager mineManager;
    private final SimplyMines plugin;
    private long lastMineSaves = 0;
    private final static int SAVE_MINES_FILES = 1800;

    public RunnableManager(@NotNull SimplyMines plugin,
                           @NotNull MineManager mineManager){
        this.plugin = plugin;
        this.mineManager = mineManager;
    }


    @Override
    public void run() {
        long now = System.currentTimeMillis() / 1000;
        boolean shouldSaveMines = now - lastMineSaves >= SAVE_MINES_FILES;

        mineManager.getMines().forEach(mine -> {
            if (now - mine.getLastReset() >= mine.getResetTime()) {
                if (mine.isEnabled()) {
                    mine.reset();
                    mine.setLastReset(now);
                }
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
