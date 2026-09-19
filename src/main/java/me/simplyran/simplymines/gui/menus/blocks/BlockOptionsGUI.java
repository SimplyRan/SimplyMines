package me.simplyran.simplymines.gui.menus.blocks;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.ItemUtils;
import me.simplyran.simplymines.utils.MessageUtils;
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

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.blocks.block-options.title", "Edit <block>");
    private final ConfigData<String> editSpawnChanceName = ConfigFactory.newConfigData(
            "menus.blocks.block-options.edit-spawn-chance-name", "<yellow>Edit Spawn Chance");
    private final ConfigData<String> editSpawnChanceLore = ConfigFactory.newConfigData(
            "menus.blocks.block-options.edit-spawn-chance-lore", "<gray>Current: <percent>%");
    private final ConfigData<String> editActionsName = ConfigFactory.newConfigData(
            "menus.blocks.block-options.edit-actions-name", "<yellow>Edit Actions");
    private final ConfigData<String> editActionsLoreSingular = ConfigFactory.newConfigData(
            "menus.blocks.block-options.edit-actions-lore-singular", "<gray><amount> action configured");
    private final ConfigData<String> editActionsLorePlural = ConfigFactory.newConfigData(
            "menus.blocks.block-options.edit-actions-lore-plural", "<gray><amount> actions configured");

    private final SimplyMines plugin;
    private final GuiManager guiManager;

    public BlockOptionsGUI(ConfigManager configManager, SimplyMines plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;

        configManager.registerLang(title);
        configManager.registerLang(editSpawnChanceName);
        configManager.registerLang(editSpawnChanceLore);
        configManager.registerLang(editActionsName);
        configManager.registerLang(editActionsLoreSingular);
        configManager.registerLang(editActionsLorePlural);
    }

    public void open(Player player, String block, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(MessageUtils.format(title, "block", block))
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getBlocksGUI().open(player, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.BACK)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        gui.setItem(2, 3,
                ItemBuilder.from(ItemUtils.getItemStackFromName(block))
                        .name(MessageUtils.format(editSpawnChanceName)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(MessageUtils.format(editSpawnChanceLore, "percent", String.valueOf(Math.round(mine.getPercentage(block) * 100)))
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> guiManager.getEditBlockGUI().open(player, block, mine)));

        int actionCount = mine.getActions(block).size();
        ConfigData<String> editActionsLore = actionCount == 1 ? editActionsLoreSingular : editActionsLorePlural;
        gui.setItem(2, 7,
                ItemBuilder.from(Material.CHEST)
                        .name(MessageUtils.format(editActionsName)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(MessageUtils.format(editActionsLore, "amount", String.valueOf(actionCount))
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> guiManager.getBlockActionsGUI().open(player, block, mine)));

        gui.open(player);
    }
}
