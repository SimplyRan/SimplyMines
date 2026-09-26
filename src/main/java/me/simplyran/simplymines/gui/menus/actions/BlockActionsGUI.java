package me.simplyran.simplymines.gui.menus.actions;

import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.actions.IAction;
import me.simplyran.simplymines.actions.impl.CommandAction;
import me.simplyran.simplymines.actions.impl.EconomyAction;
import me.simplyran.simplymines.actions.impl.ItemDropAction;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class BlockActionsGUI extends Menu {

    private static final String PATH = "menus.actions.block-actions.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Actions: <#ffd166><block>");
    private final ConfigData<String> addActionName = lang(PATH + "add-action-name", "<#7bd88f>Add Action");
    private final ConfigData<String> typeItemDrop = lang(PATH + "type-item-drop", "<#8b9bb4>Type: Item Drop");
    private final ConfigData<String> commandNameLine = lang(PATH + "command-name-line", "<white>Command: <command>");
    private final ConfigData<String> economyNameLine = lang(PATH + "economy-name-line", "<white>Economy: <amount>");

    public BlockActionsGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, String block, BasicMine mine) {
        PaginatedGui gui = createPaginated(2, MessageUtils.format(title, "block", block),
                () -> services.guiManager().getBlockOptionsGUI().open(player, block, mine),
                () -> services.mineManager().saveMineAsync(mine));

        gui.setItem(2, 9, Btn.of(Material.EMERALD, addActionName)
                .onClick(event -> services.guiManager().getAddBlockActionGUI().open(player, block, mine))
                .build());

        for (IAction action : List.copyOf(mine.getActions(block))) {
            gui.addItem(buildItem(player, block, mine, action));
        }

        gui.open(player);
    }

    private GuiItem buildItem(Player player, String block, BasicMine mine, IAction action) {
        int chancePercent = (int) Math.round(action.getChance() * 100);
        Component chanceLore = white(MessageUtils.format(MenuCommonText.CHANCE_LORE, "percent", String.valueOf(chancePercent)));
        Runnable remove = () -> {
            mine.removeAction(block, action);
            services.mineManager().saveMineAsync(mine);
            reopenLater(() -> open(player, block, mine));
        };

        return switch (action) {
            case ItemDropAction itemDrop -> editableItem(Btn.of(itemDrop.getItemStack()),
                    List.of(MessageUtils.format(typeItemDrop),
                            white(MessageUtils.format(MenuCommonText.AMOUNT_LORE, "amount", String.valueOf(itemDrop.getAmount()))),
                            chanceLore),
                    () -> services.guiManager().getEditItemDropActionGUI().open(player, block, mine, itemDrop),
                    remove);
            case CommandAction commandAction -> {
                String command = commandAction.getCommandName().isEmpty()
                        ? MessageUtils.plainFormat(MenuCommonText.NOT_SET)
                        : commandAction.getCommandName();
                yield editableItem(Btn.of(Material.COMMAND_BLOCK, commandNameLine, "command", command).color(NamedTextColor.WHITE),
                        List.of(chanceLore),
                        () -> services.guiManager().getEditCommandActionGUI().open(player, block, mine, commandAction),
                        remove);
            }
            case EconomyAction economyAction -> editableItem(
                    Btn.of(Material.GOLD_INGOT, economyNameLine, "amount", String.valueOf(economyAction.getAmount())).color(NamedTextColor.WHITE),
                    List.of(chanceLore),
                    () -> services.guiManager().getEditEconomyActionGUI().open(player, block, mine, economyAction),
                    remove);
            default -> Btn.of(Material.PAPER, Component.text(action.name())).color(NamedTextColor.WHITE).build();
        };
    }

    private Component white(Component component) {
        return component.colorIfAbsent(NamedTextColor.WHITE);
    }
}
