package me.simplyran.simplymines.gui.menus.blocks;

import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.actions.IAction;
import me.simplyran.simplymines.actions.impl.CommandAction;
import me.simplyran.simplymines.actions.impl.EconomyAction;
import me.simplyran.simplymines.actions.impl.ItemDropAction;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.Numbers;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.ItemUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Paginated list of a mine's blocks. Supports adding/editing chances (left click)
 * and removing a block entirely (shift right click).
 */
public class BlocksGUI extends Menu {

    private static final String PATH = "menus.blocks.blocks.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Edit Blocks");
    private final ConfigData<String> blockChancesLore = lang(PATH + "block-chances-lore", "<#ffd166>Block Chances: <percent>%");
    private final ConfigData<String> actionsCountLore = lang(PATH + "actions-count-lore", "<#8b9bb4>Actions: <amount>");
    private final ConfigData<String> actionLine = lang(PATH + "action-line", "<#8b9bb4>   <action> (<percent>%)");

    private final ConfigData<String> actionItemDropLabel = lang(PATH + "action-item-drop-label", "Item Drop: <amount>x <material>");
    private final ConfigData<String> actionCommandLabel = lang(PATH + "action-command-label", "Command: <command>");
    private final ConfigData<String> actionEconomyLabel = lang(PATH + "action-economy-label", "Economy: <amount>");

    public BlocksGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine) {
        PaginatedGui gui = createPaginated(6, MessageUtils.format(title),
                () -> services.guiManager().getMineEditorGUI().open(player, mine.getName()), null);

        gui.setDefaultClickAction(event -> {
            if (event.getClick().isShiftClick() && event.isRightClick()
                    && event.getClickedInventory() != null
                    && event.getClickedInventory().equals(gui.getInventory())) {
                ItemStack currentItem = event.getCurrentItem();
                if (currentItem != null && currentItem.getType() != Material.AIR) {
                    mine.removeBlock(ItemUtils.getIDFromItemStack(currentItem));
                    reopenLater(() -> open(player, mine));
                    services.mineManager().saveMineAsync(mine);
                }
            }
        });

        gui.setPlayerInventoryAction(event -> {
            if (event.getCurrentItem() == null) return;
            if (ItemUtils.isBlock(event.getCurrentItem())) {
                services.guiManager().getBlockOptionsGUI().open(player,
                        ItemUtils.getIDFromItemStack(event.getCurrentItem()), mine);
            }
        });

        for (Map.Entry<String, Double> material : mine.getMaterials()) {
            ItemStack item = ItemUtils.getItemStackFromName(material.getKey());

            Btn btn = Btn.of(item, item.displayName().color(NamedTextColor.YELLOW))
                    .lore(blockChancesLore, "percent", Numbers.percent(material.getValue()))
                    .lore(actionsCountLore, "amount", String.valueOf(mine.getActions(material.getKey()).size()));

            for (IAction action : mine.getActions(material.getKey())) {
                int chancePercent = (int) Math.round(action.getChance() * 100);
                btn.lore(actionLine, "action", actionLabel(action), "percent", String.valueOf(chancePercent));
            }

            gui.addItem(btn
                    .lore(MenuCommonText.LEFT_CLICK_EDIT)
                    .lore(MenuCommonText.SHIFT_RIGHT_CLICK_REMOVE)
                    .onClick(event -> {
                        if (event.getClick().isRightClick()) return;
                        services.guiManager().getBlockOptionsGUI().open(player, material.getKey(), mine);
                    })
                    .build());
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
