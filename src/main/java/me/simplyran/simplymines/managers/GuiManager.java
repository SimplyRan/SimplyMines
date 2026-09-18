package me.simplyran.simplymines.managers;

import lombok.Getter;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.buttons.ToggleButton;
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
        MenuCommonText.register(configManager);
        ToggleButton.register(configManager);

        this.mainMenuGUI = new MainMenuGUI(configManager, mineManager, this);
        this.mineEditorGUI = new MineEditorGUI(configManager, plugin, mineManager, this);
        this.mineSettingsGUI = new MineSettingsGUI(configManager, plugin, mineManager, this);
        this.resetSettingsGUI = new ResetSettingsGUI(configManager, plugin, mineManager, this);
        this.warnSettingsGUI = new WarnSettingsGUI(configManager, plugin, mineManager, this);
        this.warnDistanceGUI = new WarnDistanceGUI(configManager, plugin, mineManager, this);
        this.warnSecondsGUI = new WarnSecondsGUI(configManager, plugin, mineManager, this);
        this.blocksGUI = new BlocksGUI(configManager, plugin, mineManager, this);
        this.editBlockGUI = new EditBlockGUI(configManager, plugin, mineManager, this);
        this.resetTimeGUI = new ResetTimeGUI(configManager, plugin, mineManager, this);
        this.resetPercentageGUI = new ResetPercentageGUI(configManager, plugin, mineManager, this);
        this.minEfficiencyGUI = new MinEfficiencyGUI(configManager, plugin, mineManager, this);
        this.resetRequirementsGUI = new ResetRequirementsGUI(configManager, plugin, mineManager, this);
        this.mineRequirementsGUI = new MineRequirementsGUI(configManager, plugin, mineManager, this);
        this.addResetRequirementGUI = new AddResetRequirementGUI(configManager, plugin, this);
        this.addMineRequirementGUI = new AddMineRequirementGUI(configManager, plugin, this);
        this.permissionRequirementGUI = new PermissionRequirementGUI(configManager, plugin, mineManager, this);
        this.blockOptionsGUI = new BlockOptionsGUI(configManager, plugin, this);
        this.blockActionsGUI = new BlockActionsGUI(configManager, plugin, mineManager, this);
        this.addBlockActionGUI = new AddBlockActionGUI(configManager, plugin, this);
        this.editItemDropActionGUI = new EditItemDropActionGUI(configManager, plugin, mineManager, this);
        this.editCommandActionGUI = new EditCommandActionGUI(configManager, plugin, mineManager, this);
        this.editEconomyActionGUI = new EditEconomyActionGUI(configManager, plugin, mineManager, this);
    }

}
