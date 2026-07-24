package me.simplyran.simplymines.gui.menus.actions;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.actions.impl.ItemDropAction;
import me.simplyran.simplymines.gui.buttons.AdjustButton;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.utils.GuiUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Editor for a single {@link ItemDropAction} instance attached to a block.
 */
public class EditItemDropActionGUI {

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public EditItemDropActionGUI(SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;
    }

    public void open(Player player, String block, BasicMine mine, ItemDropAction action) {
        Gui gui = Gui.gui()
                .rows(4)
                .title(Component.text("Edit Item Drop"))
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            mineManager.saveMineAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getBlockActionsGUI().open(player, block, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setPlayerInventoryAction(event -> {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            action.setItemStack(clicked);
            renderDisplay(gui, action);
            gui.update();
        });

        gui.setItem(4, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        gui.setItem(1, 3,
                ItemBuilder.from(Material.HOPPER)
                        .name(Component.text("Set to Held Item").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW))
                        .lore(Component.text("Click while holding an item").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY),
                                Component.text("(or click an item in your inventory below)").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.DARK_GRAY))
                        .asGuiItem(event -> {
                            ItemStack held = player.getInventory().getItemInMainHand();
                            if (held.getType() == Material.AIR) {
                                player.sendMessage(Component.text("You must be holding an item.").color(NamedTextColor.RED));
                                return;
                            }
                            action.setItemStack(held);
                            renderDisplay(gui, action);
                            gui.update();
                        }));

        gui.setItem(1, 7,
                ItemBuilder.from(Material.BARRIER)
                        .name(Component.text("Remove Action").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.RED))
                        .asGuiItem(event -> {
                            mine.removeAction(block, action);
                            mineManager.saveMineAsync(mine);
                            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getBlockActionsGUI().open(player, block, mine));
                        }));

        renderDisplay(gui, action);

        // Amount adjust row
        new AdjustButton(gui, 2, 2, Material.RED_DYE, 10, "Remove 10 Amount", NamedTextColor.RED,
                delta -> adjustAmount(gui, action, -delta)).render();
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, "Remove 5 Amount", NamedTextColor.RED,
                delta -> adjustAmount(gui, action, -delta)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, "Remove 1 Amount", NamedTextColor.RED,
                delta -> adjustAmount(gui, action, -delta)).render();

        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, "Add 1 Amount", NamedTextColor.GREEN,
                delta -> adjustAmount(gui, action, delta)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, "Add 5 Amount", NamedTextColor.GREEN,
                delta -> adjustAmount(gui, action, delta)).render();
        new AdjustButton(gui, 2, 8, Material.LIME_DYE, 10, "Add 10 Amount", NamedTextColor.GREEN,
                delta -> adjustAmount(gui, action, delta)).render();

        // Chance adjust row
        new AdjustButton(gui, 3, 2, Material.RED_DYE, 10, "Remove 10% Chance", NamedTextColor.RED,
                delta -> adjustChance(gui, action, -delta / 100.0)).render();
        new AdjustButton(gui, 3, 3, Material.RED_DYE, 5, "Remove 5% Chance", NamedTextColor.RED,
                delta -> adjustChance(gui, action, -delta / 100.0)).render();
        new AdjustButton(gui, 3, 4, Material.RED_DYE, 1, "Remove 1% Chance", NamedTextColor.RED,
                delta -> adjustChance(gui, action, -delta / 100.0)).render();

        new AdjustButton(gui, 3, 6, Material.LIME_DYE, 1, "Add 1% Chance", NamedTextColor.GREEN,
                delta -> adjustChance(gui, action, delta / 100.0)).render();
        new AdjustButton(gui, 3, 7, Material.LIME_DYE, 5, "Add 5% Chance", NamedTextColor.GREEN,
                delta -> adjustChance(gui, action, delta / 100.0)).render();
        new AdjustButton(gui, 3, 8, Material.LIME_DYE, 10, "Add 10% Chance", NamedTextColor.GREEN,
                delta -> adjustChance(gui, action, delta / 100.0)).render();

        gui.open(player);
    }

    private void adjustAmount(Gui gui, ItemDropAction action, int delta) {
        action.setAmount(action.getAmount() + delta);
        renderDisplay(gui, action);
        gui.update();
    }

    private void adjustChance(Gui gui, ItemDropAction action, double delta) {
        action.setChance(action.getChance() + delta);
        renderDisplay(gui, action);
        gui.update();
    }

    private void renderDisplay(Gui gui, ItemDropAction action) {
        int chancePercent = (int) Math.round(action.getChance() * 100);
        gui.setItem(1, 5,
                ItemBuilder.from(action.getItemStack())
                        .amount(Math.clamp(action.getAmount(), 1, 64))
                        .lore(Component.text("Amount: " + action.getAmount())
                                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                        .color(NamedTextColor.WHITE),
                                Component.text("Chance: " + chancePercent + "%")
                                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                        .color(NamedTextColor.WHITE))
                        .asGuiItem());
    }
}
