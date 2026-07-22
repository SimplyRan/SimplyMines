package me.simplyran.simplymines.managers;

import lombok.Getter;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.gui.menus.*;

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
        this.minEfficiencyGUI = new MinEfficiencyGUI(configManager, plugin, this);
        this.resetRequirementsGUI = new ResetRequirementsGUI(plugin, this);
        this.mineRequirementsGUI = new MineRequirementsGUI(plugin, this);
        this.addResetRequirementGUI = new AddResetRequirementGUI(plugin, this);
        this.addMineRequirementGUI = new AddMineRequirementGUI(configManager, plugin, this);
        this.permissionRequirementGUI = new PermissionRequirementGUI(configManager, plugin, this);
        this.blockOptionsGUI = new BlockOptionsGUI(plugin, this);
        this.blockActionsGUI = new BlockActionsGUI(plugin, this);
        this.addBlockActionGUI = new AddBlockActionGUI(plugin, this);
        this.editItemDropActionGUI = new EditItemDropActionGUI(plugin, this);
        this.editCommandActionGUI = new EditCommandActionGUI(plugin, this);
        this.editEconomyActionGUI = new EditEconomyActionGUI(plugin, this);
    }

}