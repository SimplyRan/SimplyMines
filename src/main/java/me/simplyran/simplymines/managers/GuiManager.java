package me.simplyran.simplymines.managers;

import lombok.Getter;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.MenuServices;
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
    private final MinEnchantmentsGUI minEnchantmentsGUI;
    private final AddEnchantmentGUI addEnchantmentGUI;
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

        MenuServices services = new MenuServices(configManager, plugin, mineManager, this);

        this.mainMenuGUI = new MainMenuGUI(services);
        this.mineEditorGUI = new MineEditorGUI(services);
        this.mineSettingsGUI = new MineSettingsGUI(services);
        this.resetSettingsGUI = new ResetSettingsGUI(services);
        this.warnSettingsGUI = new WarnSettingsGUI(services);
        this.warnDistanceGUI = new WarnDistanceGUI(services);
        this.warnSecondsGUI = new WarnSecondsGUI(services);
        this.blocksGUI = new BlocksGUI(services);
        this.editBlockGUI = new EditBlockGUI(services);
        this.resetTimeGUI = new ResetTimeGUI(services);
        this.resetPercentageGUI = new ResetPercentageGUI(services);
        this.minEfficiencyGUI = new MinEfficiencyGUI(services);
        this.minEnchantmentsGUI = new MinEnchantmentsGUI(services);
        this.addEnchantmentGUI = new AddEnchantmentGUI(services);
        this.resetRequirementsGUI = new ResetRequirementsGUI(services);
        this.mineRequirementsGUI = new MineRequirementsGUI(services);
        this.addResetRequirementGUI = new AddResetRequirementGUI(services);
        this.addMineRequirementGUI = new AddMineRequirementGUI(services);
        this.permissionRequirementGUI = new PermissionRequirementGUI(services);
        this.blockOptionsGUI = new BlockOptionsGUI(services);
        this.blockActionsGUI = new BlockActionsGUI(services);
        this.addBlockActionGUI = new AddBlockActionGUI(services);
        this.editItemDropActionGUI = new EditItemDropActionGUI(services);
        this.editCommandActionGUI = new EditCommandActionGUI(services);
        this.editEconomyActionGUI = new EditEconomyActionGUI(services);
    }

}
