package me.simplyran.simplymines.gui.menus.actions;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import dev.triumphteam.gui.guis.Gui;
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
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.List;

public class BlockActionsGUI {

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.actions.block-actions.title", "<white>Actions: <block>");
    private final ConfigData<String> addActionName = ConfigFactory.newConfigData(
            "menus.actions.block-actions.add-action-name", "<green>Add Action");
    private final ConfigData<String> typeItemDrop = ConfigFactory.newConfigData(
            "menus.actions.block-actions.type-item-drop", "<gray>Type: Item Drop");
    private final ConfigData<String> commandNameLine = ConfigFactory.newConfigData(
            "menus.actions.block-actions.command-name-line", "<white>Command: <command>");
    private final ConfigData<String> economyNameLine = ConfigFactory.newConfigData(
            "menus.actions.block-actions.economy-name-line", "<white>Economy: <amount>");

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public BlockActionsGUI(ConfigManager configManager, SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;

        configManager.registerLang(title);
        configManager.registerLang(addActionName);
        configManager.registerLang(typeItemDrop);
        configManager.registerLang(commandNameLine);
        configManager.registerLang(economyNameLine);
    }

    public void open(Player player, String block, BasicMine mine) {
        PaginatedGui gui = Gui.paginated()
                .title(MessageUtils.format(title, "block", block))
                .rows(2)
                .pageSize(9)
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW
                    || event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;
            mineManager.saveMineAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getBlockOptionsGUI().open(player, block, mine));
        });

        GuiUtils.fillRow(gui, 2, Material.WHITE_STAINED_GLASS_PANE);

        gui.setItem(2, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.BACK).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> {
                            mineManager.saveMineAsync(mine);
                            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getBlockOptionsGUI().open(player, block, mine));
                        }));

        gui.setItem(2, 3,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.PREVIOUS).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> gui.previous()));

        gui.setItem(2, 7,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.NEXT).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> gui.next()));

        gui.setItem(2, 9,
                ItemBuilder.from(Material.EMERALD)
                        .name(MessageUtils.format(addActionName).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> guiManager.getAddBlockActionGUI().open(player, block, mine)));

        for (IAction action : List.copyOf(mine.getActions(block))) {
            gui.addItem(buildItem(player, block, mine, action));
        }

        gui.open(player);
    }

    private GuiItem buildItem(Player player, String block, BasicMine mine, IAction action) {
        int chancePercent = (int) Math.round(action.getChance() * 100);
        Component chanceLore = MessageUtils.format(MenuCommonText.CHANCE_LORE, "percent", String.valueOf(chancePercent))
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE);
        Component leftClickLore = MessageUtils.format(MenuCommonText.LEFT_CLICK_EDIT).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        Component shiftRightClickLore = MessageUtils.format(MenuCommonText.SHIFT_RIGHT_CLICK_REMOVE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);

        return switch (action) {
            case ItemDropAction itemDrop -> ItemBuilder.from(itemDrop.getItemStack())
                    .lore(List.of(
                            MessageUtils.format(typeItemDrop).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                            MessageUtils.format(MenuCommonText.AMOUNT_LORE, "amount", String.valueOf(itemDrop.getAmount()))
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE),
                            chanceLore,
                            Component.empty(),
                            leftClickLore,
                            shiftRightClickLore
                    ))
                    .asGuiItem(event -> handleClick(player, block, mine, action, event.getClick(),
                            () -> guiManager.getEditItemDropActionGUI().open(player, block, mine, itemDrop)));
            case CommandAction commandAction -> ItemBuilder.from(Material.COMMAND_BLOCK)
                    .name(MessageUtils.format(commandNameLine, "command",
                                    commandAction.getCommandName().isEmpty() ? MessageUtils.plainFormat(MenuCommonText.NOT_SET) : commandAction.getCommandName())
                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE))
                    .lore(List.of(
                            chanceLore,
                            Component.empty(),
                            leftClickLore,
                            shiftRightClickLore
                    ))
                    .asGuiItem(event -> handleClick(player, block, mine, action, event.getClick(),
                            () -> guiManager.getEditCommandActionGUI().open(player, block, mine, commandAction)));
            case EconomyAction economyAction -> ItemBuilder.from(Material.GOLD_INGOT)
                    .name(MessageUtils.format(economyNameLine, "amount", String.valueOf(economyAction.getAmount()))
                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE))
                    .lore(List.of(
                            chanceLore,
                            Component.empty(),
                            leftClickLore,
                            shiftRightClickLore
                    ))
                    .asGuiItem(event -> handleClick(player, block, mine, action, event.getClick(),
                            () -> guiManager.getEditEconomyActionGUI().open(player, block, mine, economyAction)));
            default -> ItemBuilder.from(Material.PAPER)
                    .name(Component.text(action.name()).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                    .asGuiItem();
        };

    }

    private void handleClick(Player player, String block, BasicMine mine, IAction action, ClickType click, Runnable openEditor) {
        if (click == ClickType.SHIFT_RIGHT) {
            mine.removeAction(block, action);
            mineManager.saveMineAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> open(player, block, mine));
            return;
        }
        if (click.isRightClick()) return;
        openEditor.run();
    }
}
