package me.simplyran.simplymines.utils;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.Bukkit;

/**
 * Shared helper for saving a mine asynchronously. Extracted out of GuiManager
 * so every menu class can reuse it without duplicating the scheduler call.
 */
public final class MineSaver {

    private MineSaver() {
    }

    public static void saveAsync(SimplyMines plugin, BasicMine mine) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> JsonUtils.saveMine(plugin, mine));
    }
}