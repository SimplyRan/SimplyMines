package me.simplyran.simplymines.gui;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;

public record MenuServices(ConfigManager configManager, SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
}
