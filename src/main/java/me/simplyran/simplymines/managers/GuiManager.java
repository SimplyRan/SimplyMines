package me.simplyran.simplymines.managers;

import lombok.Getter;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.gui.menus.BlocksGUI;
import me.simplyran.simplymines.gui.menus.EditBlockGUI;
import me.simplyran.simplymines.gui.menus.MainMenuGUI;
import me.simplyran.simplymines.gui.menus.MineEditorGUI;
import me.simplyran.simplymines.gui.menus.MinEfficiencyGUI;
import me.simplyran.simplymines.gui.menus.ResetPercentageGUI;
import me.simplyran.simplymines.gui.menus.ResetSettingsGUI;
import me.simplyran.simplymines.gui.menus.ResetTimeGUI;
import me.simplyran.simplymines.gui.menus.WarnDistanceGUI;
import me.simplyran.simplymines.gui.menus.WarnSecondsGUI;
import me.simplyran.simplymines.gui.menus.WarnSettingsGUI;

@Getter
public class GuiManager {

    private final MainMenuGUI mainMenuGUI;
    private final MineEditorGUI mineEditorGUI;
    private final ResetSettingsGUI resetSettingsGUI;
    private final WarnSettingsGUI warnSettingsGUI;
    private final WarnDistanceGUI warnDistanceGUI;
    private final WarnSecondsGUI warnSecondsGUI;
    private final BlocksGUI blocksGUI;
    private final EditBlockGUI editBlockGUI;
    private final ResetTimeGUI resetTimeGUI;
    private final ResetPercentageGUI resetPercentageGUI;
    private final MinEfficiencyGUI minEfficiencyGUI;

    public GuiManager(SimplyMines plugin, MineManager mineManager) {
        this.mainMenuGUI = new MainMenuGUI(plugin, mineManager, this);
        this.mineEditorGUI = new MineEditorGUI(plugin, mineManager, this);
        this.resetSettingsGUI = new ResetSettingsGUI(plugin, this);
        this.warnSettingsGUI = new WarnSettingsGUI(plugin, this);
        this.warnDistanceGUI = new WarnDistanceGUI(plugin, this);
        this.warnSecondsGUI = new WarnSecondsGUI(plugin, this);
        this.blocksGUI = new BlocksGUI(plugin, this);
        this.editBlockGUI = new EditBlockGUI(plugin, this);
        this.resetTimeGUI = new ResetTimeGUI(plugin, this);
        this.resetPercentageGUI = new ResetPercentageGUI(plugin, this);
        this.minEfficiencyGUI = new MinEfficiencyGUI(plugin, this);
    }

}