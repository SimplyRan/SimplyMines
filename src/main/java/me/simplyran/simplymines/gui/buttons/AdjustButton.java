package me.simplyran.simplymines.gui.buttons;

import dev.triumphteam.gui.guis.BaseGui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Numbers;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.inventory.ItemStack;

import java.util.function.DoubleConsumer;

public class AdjustButton {

    private final BaseGui gui;
    private final int row;
    private final int col;
    private final ItemStack icon;
    private final double amount;
    private final ConfigData<String> labelTemplate;
    private final TextColor color;
    private final DoubleConsumer onClick;

    public AdjustButton(BaseGui gui, int row, int col, ItemStack icon,
                        double amount, ConfigData<String> labelTemplate, TextColor color,
                        DoubleConsumer onClick) {
        this.gui = gui;
        this.row = row;
        this.col = col;
        this.icon = icon;
        this.amount = amount;
        this.labelTemplate = labelTemplate;
        this.color = color;
        this.onClick = onClick;
    }

    public void render() {
        gui.setItem(row, col, Btn.of(icon, MessageUtils.format(labelTemplate, "amount", Numbers.plain(amount)))
                .color(color)
                .amount((int) Math.round(amount))
                .onClick(event -> onClick.accept(amount))
                .build());
    }
}
