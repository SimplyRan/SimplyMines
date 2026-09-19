package me.simplyran.simplymines.gui;

import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.objects.ConfigData;

public final class MenuCommonText {

    public static final ConfigData<String> BACK = ConfigFactory.newConfigData(
            "menus.common.back", "Back");
    public static final ConfigData<String> PREVIOUS = ConfigFactory.newConfigData(
            "menus.common.previous", "Previous");
    public static final ConfigData<String> NEXT = ConfigFactory.newConfigData(
            "menus.common.next", "Next");
    public static final ConfigData<String> REMOVE_ACTION = ConfigFactory.newConfigData(
            "menus.common.remove-action", "<red>Remove Action");
    public static final ConfigData<String> LEFT_CLICK_EDIT = ConfigFactory.newConfigData(
            "menus.common.left-click-edit", "<gray>Left click to edit");
    public static final ConfigData<String> SHIFT_RIGHT_CLICK_REMOVE = ConfigFactory.newConfigData(
            "menus.common.shift-right-click-remove", "<red>Shift-right click to remove");
    public static final ConfigData<String> CHANCE_LORE = ConfigFactory.newConfigData(
            "menus.common.chance-lore", "Chance: <percent>%");
    public static final ConfigData<String> AMOUNT_LORE = ConfigFactory.newConfigData(
            "menus.common.amount-lore", "Amount: <amount>");
    public static final ConfigData<String> ADJUST_REMOVE_PERCENT = ConfigFactory.newConfigData(
            "menus.common.adjust-remove-percent", "Remove <amount>% Chance");
    public static final ConfigData<String> ADJUST_ADD_PERCENT = ConfigFactory.newConfigData(
            "menus.common.adjust-add-percent", "Add <amount>% Chance");
    public static final ConfigData<String> ADJUST_REMOVE_AMOUNT = ConfigFactory.newConfigData(
            "menus.common.adjust-remove-amount", "Remove <amount> Amount");
    public static final ConfigData<String> ADJUST_ADD_AMOUNT = ConfigFactory.newConfigData(
            "menus.common.adjust-add-amount", "Add <amount> Amount");
    public static final ConfigData<String> NOT_SET = ConfigFactory.newConfigData(
            "menus.common.not-set", "(not set)");
    public static final ConfigData<String> MATERIALS_HEADER = ConfigFactory.newConfigData(
            "menus.common.materials-header", "<blue>Materials:");
    public static final ConfigData<String> MATERIAL_LINE = ConfigFactory.newConfigData(
            "menus.common.material-line", "<blue>   <material>: <white><percent>%");

    private MenuCommonText() {}

    public static void register(ConfigManager configManager) {
        configManager.registerLang(BACK);
        configManager.registerLang(PREVIOUS);
        configManager.registerLang(NEXT);
        configManager.registerLang(REMOVE_ACTION);
        configManager.registerLang(LEFT_CLICK_EDIT);
        configManager.registerLang(SHIFT_RIGHT_CLICK_REMOVE);
        configManager.registerLang(CHANCE_LORE);
        configManager.registerLang(AMOUNT_LORE);
        configManager.registerLang(ADJUST_REMOVE_PERCENT);
        configManager.registerLang(ADJUST_ADD_PERCENT);
        configManager.registerLang(ADJUST_REMOVE_AMOUNT);
        configManager.registerLang(ADJUST_ADD_AMOUNT);
        configManager.registerLang(NOT_SET);
        configManager.registerLang(MATERIALS_HEADER);
        configManager.registerLang(MATERIAL_LINE);
    }
}
