package me.simplyran.simplymines.gui.menus.blocks;

import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.Numbers;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.ItemUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Presented after picking a block from {@link BlocksGUI}: choose whether to
 * edit its spawn chance or the actions that fire when it's mined.
 */
public class BlockOptionsGUI extends Menu {

    private static final String PATH = "menus.blocks.block-options.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Edit <#ffd166><block>");
    private final ConfigData<String> editSpawnChanceName = lang(PATH + "edit-spawn-chance-name", "<#ffd166>Edit Spawn Chance");
    private final ConfigData<String> editSpawnChanceLore = lang(PATH + "edit-spawn-chance-lore", "<#8b9bb4>Current: <percent>%");
    private final ConfigData<String> editActionsName = lang(PATH + "edit-actions-name", "<#ffd166>Edit Actions");
    private final ConfigData<String> editActionsLoreSingular = lang(PATH + "edit-actions-lore-singular", "<#8b9bb4><amount> action configured");
    private final ConfigData<String> editActionsLorePlural = lang(PATH + "edit-actions-lore-plural", "<#8b9bb4><amount> actions configured");

    public BlockOptionsGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, String block, BasicMine mine) {
        Gui gui = createGui(3, MessageUtils.format(title, "block", block),
                () -> services.guiManager().getBlocksGUI().open(player, mine), null);

        String percent = Numbers.percent(mine.getPercentage(block));
        gui.setItem(2, 4, Btn.of(ItemUtils.getItemStackFromName(block), MessageUtils.format(editSpawnChanceName))
                .lore(editSpawnChanceLore, "percent", percent)
                .onClick(event -> services.guiManager().getEditBlockGUI().open(player, block, mine))
                .build());

        int actionCount = mine.getActions(block).size();
        gui.setItem(2, 6, Btn.of(Material.CHEST, editActionsName)
                .lore(actionCount == 1 ? editActionsLoreSingular : editActionsLorePlural, "amount", String.valueOf(actionCount))
                .onClick(event -> services.guiManager().getBlockActionsGUI().open(player, block, mine))
                .build());

        gui.open(player);
    }
}
