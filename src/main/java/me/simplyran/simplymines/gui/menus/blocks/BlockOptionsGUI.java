package me.simplyran.simplymines.gui.menus.blocks;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Presented after picking a block from {@link BlocksGUI}: choose whether to
 * edit its spawn chance or the actions that fire when it's mined.
 */
public class BlockOptionsGUI {

    private final SimplyMines plugin;
    private final GuiManager guiManager;

    public BlockOptionsGUI(SimplyMines plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    public void open(Player player, String block, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Edit " + block))
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getBlocksGUI().open(player, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        gui.setItem(2, 3,
                ItemBuilder.from(ItemUtils.getItemStackFromName(block))
                        .name(Component.text("Edit Spawn Chance")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .lore(Component.text("Current: " + Math.round(mine.getPercentage(block) * 100) + "%")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.GRAY))
                        .asGuiItem(event -> guiManager.getEditBlockGUI().open(player, block, mine)));

        int actionCount = mine.getActions(block).size();
        gui.setItem(2, 6,
                ItemBuilder.from(Material.CHEST)
                        .name(Component.text("Edit Actions")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .lore(Component.text(actionCount + " action" + (actionCount == 1 ? "" : "s") + " configured")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.GRAY))
                        .asGuiItem(event -> guiManager.getBlockActionsGUI().open(player, block, mine)));

        gui.open(player);
    }
}
