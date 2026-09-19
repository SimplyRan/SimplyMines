package me.simplyran.simplymines.gui.buttons;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.BaseGui;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.function.IntConsumer;

public class AdjustButton {

    private final BaseGui gui;
    private final int row;
    private final int col;
    private final Material material;
    private final int amount;
    private final ConfigData<String> labelTemplate;
    private final NamedTextColor color;
    private final IntConsumer onClick;

    public AdjustButton(BaseGui gui, int row, int col, Material material,
                        int amount, ConfigData<String> labelTemplate, NamedTextColor color,
                        IntConsumer onClick) {
        this.gui = gui;
        this.row = row;
        this.col = col;
        this.material = material;
        this.amount = amount;
        this.labelTemplate = labelTemplate;
        this.color = color;
        this.onClick = onClick;
    }

    public void render() {
        gui.setItem(row, col, ItemBuilder.from(material)
                .amount(Math.clamp(amount, 1, 64))
                .name(Component.text(MessageUtils.plainFormat(labelTemplate, "amount", String.valueOf(amount)))
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(color))
                .asGuiItem(event -> onClick.accept(amount)));
    }
}
