package me.simplyran.simplymines.gui.menus.actions;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.actions.impl.CommandAction;
import me.simplyran.simplymines.actions.impl.EconomyAction;
import me.simplyran.simplymines.actions.impl.ItemDropAction;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AddBlockActionGUI {

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.actions.add-block-action.title", "Add Action");
    private final ConfigData<String> itemDropName = ConfigFactory.newConfigData(
            "menus.actions.add-block-action.item-drop-name", "<yellow>Item Drop");
    private final ConfigData<String> itemDropLore = ConfigFactory.newConfigData(
            "menus.actions.add-block-action.item-drop-lore", "<gray>Drops an item when this block is mined");
    private final ConfigData<String> commandName = ConfigFactory.newConfigData(
            "menus.actions.add-block-action.command-name", "<yellow>Command");
    private final ConfigData<String> commandLore = ConfigFactory.newConfigData(
            "menus.actions.add-block-action.command-lore", "<gray>Runs a command as the player when this block is mined");
    private final ConfigData<String> economyName = ConfigFactory.newConfigData(
            "menus.actions.add-block-action.economy-name", "<yellow>Economy");
    private final ConfigData<String> economyLore = ConfigFactory.newConfigData(
            "menus.actions.add-block-action.economy-lore", "<gray>Grants a currency amount when this block is mined");

    private final SimplyMines plugin;
    private final GuiManager guiManager;

    public AddBlockActionGUI(ConfigManager configManager, SimplyMines plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;

        configManager.registerLang(title);
        configManager.registerLang(itemDropName);
        configManager.registerLang(itemDropLore);
        configManager.registerLang(commandName);
        configManager.registerLang(commandLore);
        configManager.registerLang(economyName);
        configManager.registerLang(economyLore);
    }

    public void open(Player player, String block, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(MessageUtils.format(title))
                .disableAllInteractions()
                .create();

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.BACK).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> guiManager.getBlockActionsGUI().open(player, block, mine)));

        gui.setItem(2, 3,
                ItemBuilder.from(Material.CHEST)
                        .name(MessageUtils.format(itemDropName).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(MessageUtils.format(itemDropLore).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> {
                            ItemStack held = player.getInventory().getItemInMainHand();
                            ItemStack itemStack = held.getType() == Material.AIR
                                    ? new ItemStack(Material.STONE)
                                    : held.clone();
                            ItemDropAction action = new ItemDropAction(itemStack);
                            mine.addAction(block, action);
                            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getEditItemDropActionGUI().open(player, block, mine, action));
                        }));

        gui.setItem(2, 5,
                ItemBuilder.from(Material.COMMAND_BLOCK)
                        .name(MessageUtils.format(commandName).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(MessageUtils.format(commandLore).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> {
                            CommandAction action = new CommandAction("", null, new String[0]);
                            mine.addAction(block, action);
                            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getEditCommandActionGUI().open(player, block, mine, action));
                        }));

        gui.setItem(2, 7,
                ItemBuilder.from(Material.GOLD_INGOT)
                        .name(MessageUtils.format(economyName).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(MessageUtils.format(economyLore).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> {
                            EconomyAction action = new EconomyAction(SimplyMines.getEconomy(), 0);
                            mine.addAction(block, action);
                            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getEditEconomyActionGUI().open(player, block, mine, action));
                        }));

        gui.open(player);
    }
}
