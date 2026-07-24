package me.simplyran.simplymines.managers;

import lombok.Getter;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.gui.menus.*;
import me.simplyran.simplymines.gui.menus.actions.*;
import me.simplyran.simplymines.gui.menus.blocks.*;
import me.simplyran.simplymines.gui.menus.requirements.*;
import me.simplyran.simplymines.gui.menus.settings.*;

@Getter
public class GuiManager {

    private final MainMenuGUI mainMenuGUI;
    private final MineEditorGUI mineEditorGUI;
    private final MineSettingsGUI mineSettingsGUI;
    private final ResetSettingsGUI resetSettingsGUI;
    private final WarnSettingsGUI warnSettingsGUI;
    private final WarnDistanceGUI warnDistanceGUI;
    private final WarnSecondsGUI warnSecondsGUI;
    private final BlocksGUI blocksGUI;
    private final EditBlockGUI editBlockGUI;
    private final ResetTimeGUI resetTimeGUI;
    private final ResetPercentageGUI resetPercentageGUI;
    private final MinEfficiencyGUI minEfficiencyGUI;
    private final ResetRequirementsGUI resetRequirementsGUI;
    private final MineRequirementsGUI mineRequirementsGUI;
    private final AddResetRequirementGUI addResetRequirementGUI;
    private final AddMineRequirementGUI addMineRequirementGUI;
    private final PermissionRequirementGUI permissionRequirementGUI;
    private final BlockOptionsGUI blockOptionsGUI;
    private final BlockActionsGUI blockActionsGUI;
    private final AddBlockActionGUI addBlockActionGUI;
    private final EditItemDropActionGUI editItemDropActionGUI;
    private final EditCommandActionGUI editCommandActionGUI;
    private final EditEconomyActionGUI editEconomyActionGUI;

    public GuiManager(ConfigManager configManager, SimplyMines plugin, MineManager mineManager) {
        this.mainMenuGUI = new MainMenuGUI(mineManager, this);
        this.mineEditorGUI = new MineEditorGUI(plugin, mineManager, this);
        this.mineSettingsGUI = new MineSettingsGUI(plugin, mineManager, this);
        this.resetSettingsGUI = new ResetSettingsGUI(plugin, mineManager, this);
        this.warnSettingsGUI = new WarnSettingsGUI(plugin, mineManager, this);
        this.warnDistanceGUI = new WarnDistanceGUI(plugin, mineManager, this);
        this.warnSecondsGUI = new WarnSecondsGUI(plugin, mineManager, this);
        this.blocksGUI = new BlocksGUI(plugin, mineManager, this);
        this.editBlockGUI = new EditBlockGUI(plugin, mineManager, this);
        this.resetTimeGUI = new ResetTimeGUI(plugin, mineManager, this);
        this.resetPercentageGUI = new ResetPercentageGUI(plugin, mineManager, this);
        this.minEfficiencyGUI = new MinEfficiencyGUI(configManager, plugin, mineManager, this);
        this.resetRequirementsGUI = new ResetRequirementsGUI(plugin, mineManager, this);
        this.mineRequirementsGUI = new MineRequirementsGUI(plugin, mineManager, this);
        this.addResetRequirementGUI = new AddResetRequirementGUI(plugin, this);
        this.addMineRequirementGUI = new AddMineRequirementGUI(configManager, plugin, this);
        this.permissionRequirementGUI = new PermissionRequirementGUI(configManager, plugin, mineManager, this);
        this.blockOptionsGUI = new BlockOptionsGUI(plugin, this);
        this.blockActionsGUI = new BlockActionsGUI(plugin, mineManager, this);
        this.addBlockActionGUI = new AddBlockActionGUI(plugin, this);
        this.editItemDropActionGUI = new EditItemDropActionGUI(plugin, mineManager, this);
        this.editCommandActionGUI = new EditCommandActionGUI(plugin, mineManager, this);
        this.editEconomyActionGUI = new EditEconomyActionGUI(plugin, mineManager, this);
    }

}