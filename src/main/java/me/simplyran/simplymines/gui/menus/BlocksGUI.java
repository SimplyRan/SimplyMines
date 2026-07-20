package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.ItemUtils;
import me.simplyran.simplymines.utils.MineSaver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Paginated list of a mine's blocks. Supports adding/editing chances (left click)
 * and removing a block entirely (shift right click).
 */
public class BlocksGUI {

    private final SimplyMines plugin;
    private final GuiManager guiManager;

    public BlocksGUI(SimplyMines plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    public void open(Player player, BasicMine mine) {
        PaginatedGui gui = Gui.paginated()
                .rows(6)
                .title(Component.text("Edit Blocks"))
                .disableAllInteractions()
                .create();

        // Go back to mine GUI, but only on a genuine player-initiated close
        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMineEditorGUI().open(player, mine.getName()));
        });

        gui.setDefaultClickAction(event -> {
            // Shift Right click removes the block from the mine
            if (event.getClick().isShiftClick() && event.isRightClick()
                    && event.getClickedInventory() != null
                    && event.getClickedInventory().equals(gui.getInventory())) {
                ItemStack currentItem = event.getCurrentItem();
                if (currentItem != null
                        && currentItem.getType() != Material.AIR) {
                    mine.removeBlock(ItemUtils.getIDFromItemStack(currentItem));
                    Bukkit.getScheduler().runTask(plugin, () -> open(player, mine));
                    MineSaver.saveAsync(plugin, mine);
                }
            }
        });

        gui.setPlayerInventoryAction(event -> {
                    if (event.getCurrentItem() == null) return;
                    if (ItemUtils.isBlock(event.getCurrentItem())) {
                        guiManager.getEditBlockGUI().open(player,
                                ItemUtils.getIDFromItemStack(event.getCurrentItem()), mine);
                    }
                }
        );

        GuiUtils.fillRow(gui, 6, Material.WHITE_STAINED_GLASS_PANE);

        gui.setItem(6, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        gui.setItem(6, 3,
                ItemBuilder.from(Material.ARROW).name(Component.text("Previous")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> gui.previous()));

        gui.setItem(6, 7,
                ItemBuilder.from(Material.ARROW).name(Component.text("Next")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> gui.next()));

        for (Map.Entry<String, Double> material : mine.getMaterials()) {
            ItemStack itemNow = ItemUtils.getItemStackFromName(material.getKey());
            gui.addItem(
                    ItemBuilder.from(itemNow)
                            .name(itemNow.displayName()
                                    .color(NamedTextColor.YELLOW)
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                            .lore(Component.text("Block Chances: " + material.getValue() * 100 + "%")
                                            .color(NamedTextColor.YELLOW)
                                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                                    Component.text("Click to edit")
                                            .color(NamedTextColor.WHITE)
                                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                                    Component.text("Shift Right Click To Remove")
                                            .color(NamedTextColor.RED)
                                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                            .asGuiItem(event -> {
                                if (event.getClick().isRightClick()) return;
                                guiManager.getEditBlockGUI().open(player, material.getKey(), mine);
                            })
            );
        }

        gui.open(player);
    }
}