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
            "menus.common.remove-action", "<#ef6f6c>Remove Action");
    public static final ConfigData<String> LEFT_CLICK_EDIT = ConfigFactory.newConfigData(
            "menus.common.left-click-edit", "<#8b9bb4>Left click to edit");
    public static final ConfigData<String> SHIFT_RIGHT_CLICK_REMOVE = ConfigFactory.newConfigData(
            "menus.common.shift-right-click-remove", "<#ef6f6c>Shift-right click to remove");
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
            "menus.common.materials-header", "<#ffd166>Materials:");
    public static final ConfigData<String> MATERIAL_LINE = ConfigFactory.newConfigData(
            "menus.common.material-line", "<#8b9bb4>   <material>: <white><percent>%");

    public static final ConfigData<String> TYPE_VALUE_NAME = ConfigFactory.newConfigData(
            "menus.common.type-value-name", "<#ffd166>Type Exact Value");
    public static final ConfigData<String> TYPE_VALUE_LORE = ConfigFactory.newConfigData(
            "menus.common.type-value-lore", "<#8b9bb4>Click, then type a number in chat");
    public static final ConfigData<String> TYPE_VALUE_PROMPT = ConfigFactory.newConfigData(
            "menus.common.type-value-prompt", "<#ffd166>Type the new value in chat, or 'cancel'.");
    public static final ConfigData<String> TYPE_VALUE_INVALID = ConfigFactory.newConfigData(
            "menus.common.type-value-invalid", "<#ef6f6c>That is not a valid number.");
    public static final ConfigData<String> FINE_TUNE_NAME = ConfigFactory.newConfigData(
            "menus.common.fine-tune-name", "<#ffd166>Fine Tune");
    public static final ConfigData<String> FINE_TUNE_LORE = ConfigFactory.newConfigData(
            "menus.common.fine-tune-lore", "<#8b9bb4>Small steps for precise values");

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
        configManager.registerLang(TYPE_VALUE_NAME);
        configManager.registerLang(TYPE_VALUE_LORE);
        configManager.registerLang(TYPE_VALUE_PROMPT);
        configManager.registerLang(TYPE_VALUE_INVALID);
        configManager.registerLang(FINE_TUNE_NAME);
        configManager.registerLang(FINE_TUNE_LORE);
    }
}
