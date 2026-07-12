package me.simplyran.simplymines.managers;

import org.jetbrains.annotations.NotNull;

public class RunnableManager implements Runnable{

    private final MineManager mineManager;

    public RunnableManager(@NotNull MineManager mineManager){
        this.mineManager = mineManager;
    }


    @Override
    public void run() {
        mineManager.getMines().forEach(mine -> {
            if ((System.currentTimeMillis() / 1000) - mine.getLastReset() >= mine.getResetTime()) {
                mine.reset();
                mine.setLastReset(System.currentTimeMillis() / 1000);
            }
        });

    }
}
