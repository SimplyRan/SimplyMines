package me.simplyran.simplymines.gui.buttons;

import dev.triumphteam.gui.guis.BaseGui;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Palette;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

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
    private final Component label;
    private final Component description;
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;
    private final Runnable onToggle;

    public ToggleButton(BaseGui gui, int row, int col, Component label,
                        BooleanSupplier getter, Consumer<Boolean> setter,
                        Runnable onToggle) {
        this(gui, row, col, label, null, getter, setter, onToggle);
    }

    public ToggleButton(BaseGui gui, int row, int col, Component label, Component description,
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

        Btn btn = Btn.of(material, label.colorIfAbsent(Palette.ACCENT.color())
                .append(Component.text(": "))
                .append(MessageUtils.format(state ? ENABLED_TEXT : DISABLED_TEXT)
                        .colorIfAbsent(state ? Palette.SUCCESS.color() : Palette.DANGER.color())));
        if (description != null) {
            btn.lore(description.colorIfAbsent(Palette.MUTED.color()));
        }
        btn.lore(MessageUtils.format(CLICK_TO_TOGGLE).colorIfAbsent(NamedTextColor.DARK_GRAY));

        gui.setItem(row, col, btn.onClick(event -> toggle()).build());
    }

    private void toggle() {
        setter.accept(!getter.getAsBoolean());
        render();
        gui.update();
        if (onToggle != null) onToggle.run();
    }
}
