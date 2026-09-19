package me.simplyran.simplymines.gui.menus.blocks;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.actions.IAction;
import me.simplyran.simplymines.actions.impl.CommandAction;
import me.simplyran.simplymines.actions.impl.EconomyAction;
import me.simplyran.simplymines.actions.impl.ItemDropAction;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.ItemUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Paginated list of a mine's blocks. Supports adding/editing chances (left click)
 * and removing a block entirely (shift right click).
 */
public class BlocksGUI {

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.blocks.blocks.title", "Edit Blocks");
    private final ConfigData<String> blockChancesLore = ConfigFactory.newConfigData(
            "menus.blocks.blocks.block-chances-lore", "<yellow>Block Chances: <percent>%");
    private final ConfigData<String> actionsCountLore = ConfigFactory.newConfigData(
            "menus.blocks.blocks.actions-count-lore", "<aqua>Actions: <amount>");
    private final ConfigData<String> actionLine = ConfigFactory.newConfigData(
            "menus.blocks.blocks.action-line", "<gray>   <action> (<percent>%)");

    private final ConfigData<String> actionItemDropLabel = ConfigFactory.newConfigData(
            "menus.blocks.blocks.action-item-drop-label", "Item Drop: <amount>x <material>");
    private final ConfigData<String> actionCommandLabel = ConfigFactory.newConfigData(
            "menus.blocks.blocks.action-command-label", "Command: <command>");
    private final ConfigData<String> actionEconomyLabel = ConfigFactory.newConfigData(
            "menus.blocks.blocks.action-economy-label", "Economy: <amount>");

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public BlocksGUI(ConfigManager configManager, SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;

        configManager.registerLang(title);
        configManager.registerLang(blockChancesLore);
        configManager.registerLang(actionsCountLore);
        configManager.registerLang(actionLine);
        configManager.registerLang(actionItemDropLabel);
        configManager.registerLang(actionCommandLabel);
        configManager.registerLang(actionEconomyLabel);
    }

    public void open(Player player, BasicMine mine) {
        PaginatedGui gui = Gui.paginated()
                .rows(6)
                .title(MessageUtils.format(title))
                .pageSize(45)
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMineEditorGUI().open(player, mine.getName()));
        });

        gui.setDefaultClickAction(event -> {
            if (event.getClick().isShiftClick() && event.isRightClick()
                    && event.getClickedInventory() != null
                    && event.getClickedInventory().equals(gui.getInventory())) {
                ItemStack currentItem = event.getCurrentItem();
                if (currentItem != null
                        && currentItem.getType() != Material.AIR) {
                    mine.removeBlock(ItemUtils.getIDFromItemStack(currentItem));
                    Bukkit.getScheduler().runTask(plugin, () -> open(player, mine));
                    mineManager.saveMineAsync(mine);
                }
            }
        });

        gui.setPlayerInventoryAction(event -> {
                    if (event.getCurrentItem() == null) return;
                    if (ItemUtils.isBlock(event.getCurrentItem())) {
                        guiManager.getBlockOptionsGUI().open(player,
                                ItemUtils.getIDFromItemStack(event.getCurrentItem()), mine);
                    }
                }
        );

        GuiUtils.fillRow(gui, 6, Material.WHITE_STAINED_GLASS_PANE);

        gui.setItem(6, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.BACK)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        gui.setItem(6, 3,
                ItemBuilder.from(Material.ARROW).name(MessageUtils.format(MenuCommonText.PREVIOUS)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> gui.previous()));

        gui.setItem(6, 7,
                ItemBuilder.from(Material.ARROW).name(MessageUtils.format(MenuCommonText.NEXT)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> gui.next()));

        for (Map.Entry<String, Double> material : mine.getMaterials()) {
            ItemStack itemNow = ItemUtils.getItemStackFromName(material.getKey());

            List<Component> lore = new ArrayList<>();
            lore.add(MessageUtils.format(blockChancesLore, "percent", String.valueOf(material.getValue() * 100))
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));

            int actionCount = mine.getActions(material.getKey()).size();
            lore.add(MessageUtils.format(actionsCountLore, "amount", String.valueOf(actionCount))
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));

            for (IAction action : mine.getActions(material.getKey())) {
                int chancePercent = (int) Math.round(action.getChance() * 100);
                lore.add(MessageUtils.format(actionLine, "action", actionLabel(action), "percent", String.valueOf(chancePercent))
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            }

            lore.add(MessageUtils.format(MenuCommonText.LEFT_CLICK_EDIT)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(MessageUtils.format(MenuCommonText.SHIFT_RIGHT_CLICK_REMOVE)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));

            gui.addItem(
                    ItemBuilder.from(itemNow)
                            .name(itemNow.displayName()
                                    .color(NamedTextColor.YELLOW)
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                            .lore(lore)
                            .asGuiItem(event -> {
                                if (event.getClick().isRightClick()) return;
                                guiManager.getBlockOptionsGUI().open(player, material.getKey(), mine);
                            })
            );
        }

        gui.open(player);
    }

    private String actionLabel(IAction action) {
        if (action instanceof ItemDropAction itemDrop) {
            return MessageUtils.plainFormat(actionItemDropLabel,
                    "amount", String.valueOf(itemDrop.getAmount()),
                    "material", itemDrop.getItemStack().getType().name());
        }
        if (action instanceof CommandAction commandAction) {
            String name = commandAction.getCommandName();
            return MessageUtils.plainFormat(actionCommandLabel,
                    "command", name.isEmpty() ? MessageUtils.plainFormat(MenuCommonText.NOT_SET) : name);
        }
        if (action instanceof EconomyAction economyAction) {
            return MessageUtils.plainFormat(actionEconomyLabel, "amount", String.valueOf(economyAction.getAmount()));
        }
        return action.name();
    }
}
