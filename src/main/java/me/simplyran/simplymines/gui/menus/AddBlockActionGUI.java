package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.actions.IAction;
import me.simplyran.simplymines.actions.impl.CommandAction;
import me.simplyran.simplymines.actions.impl.EconomyAction;
import me.simplyran.simplymines.actions.impl.ItemDropAction;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.utils.GuiUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Presents the available action types to attach to a block. Unlike mine/reset
 * requirements, actions are not singletons — you can add as many of each type
 * as you want (e.g. two separate ItemDropActions dropping different items).
 */
public class AddBlockActionGUI {

    private final SimplyMines plugin;
    private final GuiManager guiManager;

    public AddBlockActionGUI(SimplyMines plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    public void open(Player player, String block, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Add Action"))
                .disableAllInteractions()
                .create();

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                        .asGuiItem(event -> guiManager.getBlockActionsGUI().open(player, block, mine)));

        gui.setItem(2, 3,
                ItemBuilder.from(Material.CHEST)
                        .name(Component.text("Item Drop").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW))
                        .lore(Component.text("Drops an item when this block is mined").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY))
                        .asGuiItem(event -> {
                            ItemStack held = player.getInventory().getItemInMainHand();
                            ItemStack itemStack = (held == null || held.getType() == Material.AIR)
                                    ? new ItemStack(Material.STONE)
                                    : held.clone();
                            ItemDropAction action = new ItemDropAction(itemStack);
                            mine.addAction(block, action);
                            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getEditItemDropActionGUI().open(player, block, mine, action));
                        }));

        gui.setItem(2, 5,
                ItemBuilder.from(Material.COMMAND_BLOCK)
                        .name(Component.text("Command").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW))
                        .lore(Component.text("Runs a command as the player when this block is mined").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY))
                        .asGuiItem(event -> {
                            IAction action = new CommandAction("", null, new String[0]);
                            mine.addAction(block, action);
                            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getEditCommandActionGUI().open(player, block, mine, (CommandAction) action));
                        }));

        gui.setItem(2, 7,
                ItemBuilder.from(Material.GOLD_INGOT)
                        .name(Component.text("Economy").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW))
                        .lore(Component.text("Grants a currency amount when this block is mined").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY))
                        .asGuiItem(event -> {
                            EconomyAction action = new EconomyAction(0);
                            mine.addAction(block, action);
                            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getEditEconomyActionGUI().open(player, block, mine, action));
                        }));

        gui.open(player);
    }
}
