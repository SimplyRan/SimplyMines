package me.simplyran.simplymines.gui.menus.actions;

import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.actions.impl.CommandAction;
import me.simplyran.simplymines.actions.impl.EconomyAction;
import me.simplyran.simplymines.actions.impl.ItemDropAction;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AddBlockActionGUI extends Menu {

    private static final String PATH = "menus.actions.add-block-action.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Add Action");
    private final ConfigData<String> itemDropName = lang(PATH + "item-drop-name", "<#ffd166>Item Drop");
    private final ConfigData<String> itemDropLore = lang(PATH + "item-drop-lore", "<#8b9bb4>Drops an item when this block is mined");
    private final ConfigData<String> commandName = lang(PATH + "command-name", "<#ffd166>Command");
    private final ConfigData<String> commandLore = lang(PATH + "command-lore", "<#8b9bb4>Runs a command as the player when this block is mined");
    private final ConfigData<String> economyName = lang(PATH + "economy-name", "<#ffd166>Economy");
    private final ConfigData<String> economyLore = lang(PATH + "economy-lore", "<#8b9bb4>Grants a currency amount when this block is mined");

    public AddBlockActionGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, String block, BasicMine mine) {
        Gui gui = createGui(3, MessageUtils.format(title),
                () -> services.guiManager().getBlockActionsGUI().open(player, block, mine), null);

        gui.setItem(2, 3, Btn.of(Material.CHEST, itemDropName)
                .lore(itemDropLore)
                .onClick(event -> {
                    ItemStack held = player.getInventory().getItemInMainHand();
                    ItemStack itemStack = held.getType() == Material.AIR ? new ItemStack(Material.STONE) : held.clone();
                    ItemDropAction action = new ItemDropAction(itemStack);
                    mine.addAction(block, action);
                    reopenLater(() -> services.guiManager().getEditItemDropActionGUI().open(player, block, mine, action));
                })
                .build());

        gui.setItem(2, 5, Btn.of(Material.COMMAND_BLOCK, commandName)
                .lore(commandLore)
                .onClick(event -> {
                    CommandAction action = new CommandAction("", null, new String[0]);
                    mine.addAction(block, action);
                    reopenLater(() -> services.guiManager().getEditCommandActionGUI().open(player, block, mine, action));
                })
                .build());

        gui.setItem(2, 7, Btn.of(Material.GOLD_INGOT, economyName)
                .lore(economyLore)
                .onClick(event -> {
                    EconomyAction action = new EconomyAction(SimplyMines.getEconomy(), 0);
                    mine.addAction(block, action);
                    reopenLater(() -> services.guiManager().getEditEconomyActionGUI().open(player, block, mine, action));
                })
                .build());

        gui.open(player);
    }
}
