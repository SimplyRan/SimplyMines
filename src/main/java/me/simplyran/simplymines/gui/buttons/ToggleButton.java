package me.simplyran.simplymines.gui.buttons;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.BaseGui;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ToggleButton {

    private static final ConfigData<String> ENABLED_TEXT = ConfigFactory.newConfigData(
            "menus.common.toggle-enabled", "Enabled");
    private static final ConfigData<String> DISABLED_TEXT = ConfigFactory.newConfigData(
            "menus.common.toggle-disabled", "Disabled");
    private static final ConfigData<String> CLICK_TO_TOGGLE = ConfigFactory.newConfigData(
            "menus.common.click-to-toggle", "Click to toggle");

    public static void register(ConfigManager configManager) {
        configManager.registerLang(ENABLED_TEXT);
        configManager.registerLang(DISABLED_TEXT);
        configManager.registerLang(CLICK_TO_TOGGLE);
    }

    private final BaseGui gui;
    private final int row;
    private final int col;
    private final String label;
    private final String description;
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;
    private final Runnable onToggle;

    public ToggleButton(BaseGui gui, int row, int col, String label,
                        BooleanSupplier getter, Consumer<Boolean> setter,
                        Runnable onToggle) {
        this(gui, row, col, label, null, getter, setter, onToggle);
    }

    public ToggleButton(BaseGui gui, int row, int col, String label, String description,
                        BooleanSupplier getter, Consumer<Boolean> setter,
                        Runnable onToggle) {
        this.gui = gui;
        this.row = row;
        this.col = col;
        this.label = label;
        this.description = description;
        this.getter = getter;
        this.setter = setter;
        this.onToggle = onToggle;
    }

    public void render() {
        boolean state = getter.getAsBoolean();
        Material material = state ? Material.LIME_DYE : Material.RED_DYE;

        List<Component> lore = new ArrayList<>();
        if (description != null) {
            lore.add(Component.text(description)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .color(NamedTextColor.GRAY));
        }
        lore.add(MessageUtils.format(CLICK_TO_TOGGLE)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .colorIfAbsent(NamedTextColor.DARK_GRAY));

        gui.setItem(row, col, ItemBuilder.from(material)
                .name(Component.text(label + ": ")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.YELLOW)
                        .append(MessageUtils.format(state ? ENABLED_TEXT : DISABLED_TEXT)
                                .colorIfAbsent(state ? NamedTextColor.GREEN : NamedTextColor.RED)))
                .lore(lore)
                .asGuiItem(event -> toggle()));
    }

    private void toggle() {
        setter.accept(!getter.getAsBoolean());
        render();
        gui.update();
        if (onToggle != null) onToggle.run();
    }
}
