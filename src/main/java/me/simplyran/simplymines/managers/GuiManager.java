package me.simplyran.simplymines.managers;

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
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.entity.Player;

/**
 * Facade that constructs and wires together every GUI menu class, and exposes
 * the original public API (openMainGUI, openMineGUI, ...) so existing call
 * sites throughout the plugin don't need to change.
 *
 * Each menu class is handed a reference to this GuiManager so it can navigate
 * to sibling menus (e.g. "Back" buttons) without the menu classes needing to
 * depend on each other directly.
 */
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

    // ---------------------------------------------------------------------
    // Public API — unchanged signatures, kept so existing callers work as-is
    // ---------------------------------------------------------------------

    public void openMainGUI(Player player) {
        mainMenuGUI.open(player);
    }

    public void openMineGUI(Player player, String mineName) {
        mineEditorGUI.open(player, mineName);
    }

    public void openResetSettingsGUI(Player player, BasicMine mine) {
        resetSettingsGUI.open(player, mine);
    }

    public void openWarnSettingsGUI(Player player, BasicMine mine) {
        warnSettingsGUI.open(player, mine);
    }

    public void openChangeWarnDistanceGUI(Player player, BasicMine mine) {
        warnDistanceGUI.open(player, mine);
    }

    public void openWarnSecondsGUI(Player player, BasicMine mine) {
        warnSecondsGUI.open(player, mine);
    }

    public void openBlocksGUI(Player player, BasicMine mine) {
        blocksGUI.open(player, mine);
    }

    public void openEditBlockGUI(Player player, String block, BasicMine mine) {
        editBlockGUI.open(player, block, mine);
    }

    public void openChangeResetTimeGUI(Player player, BasicMine mine) {
        resetTimeGUI.open(player, mine);
    }

    public void openChangeResetPercentageGUI(Player player, BasicMine mine) {
        resetPercentageGUI.open(player, mine);
    }

    public void openChangeMinEfficiencyGUI(Player player, BasicMine mine) {
        minEfficiencyGUI.open(player, mine);
    }

    // ---------------------------------------------------------------------
    // Internal accessors — used only by the menu classes for cross-navigation
    // ---------------------------------------------------------------------

    public MainMenuGUI getMainMenuGUI() {
        return mainMenuGUI;
    }

    public MineEditorGUI getMineEditorGUI() {
        return mineEditorGUI;
    }

    public ResetSettingsGUI getResetSettingsGUI() {
        return resetSettingsGUI;
    }

    public WarnSettingsGUI getWarnSettingsGUI() {
        return warnSettingsGUI;
    }

    public WarnDistanceGUI getWarnDistanceGUI() {
        return warnDistanceGUI;
    }

    public WarnSecondsGUI getWarnSecondsGUI() {
        return warnSecondsGUI;
    }

    public BlocksGUI getBlocksGUI() {
        return blocksGUI;
    }

    public EditBlockGUI getEditBlockGUI() {
        return editBlockGUI;
    }

    public ResetTimeGUI getResetTimeGUI() {
        return resetTimeGUI;
    }

    public ResetPercentageGUI getResetPercentageGUI() {
        return resetPercentageGUI;
    }

    public MinEfficiencyGUI getMinEfficiencyGUI() {
        return minEfficiencyGUI;
    }
}