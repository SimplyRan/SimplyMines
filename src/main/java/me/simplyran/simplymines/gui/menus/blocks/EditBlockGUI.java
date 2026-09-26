package me.simplyran.simplymines.gui.menus.blocks;

import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.Numbers;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.ItemUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Menu for adjusting a single block's drop chance within a mine.
 */
public class EditBlockGUI extends Menu {

    private static final String PATH = "menus.blocks.edit-block.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Edit Block Chances");
    private final ConfigData<String> blockPercentName = lang(PATH + "block-percent-name", "<#ffd166>Block Percent: <white><percent>%");
    private final ConfigData<String> allBlocksName = lang(PATH + "all-blocks-name", "<#ffd166>All Blocks");
    private final ConfigData<String> allBlocksLore = lang(PATH + "all-blocks-lore", "<#8b9bb4>   <material>: <white><percent>%");

    public EditBlockGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, String block, BasicMine mine) {
        Gui gui = createGui(5, MessageUtils.format(title),
                () -> services.guiManager().getBlockOptionsGUI().open(player, block, mine),
                () -> services.mineManager().saveMineAsync(mine));

        renderBlockDisplay(gui, block, mine);
        renderAllBlocksLore(gui, mine);
        adjusters(gui, 3, new int[]{1, 5, 10}, MenuCommonText.ADJUST_REMOVE_PERCENT, MenuCommonText.ADJUST_ADD_PERCENT,
                delta -> adjust(gui, block, mine, delta / 100.0),
                typeValueButton(player, () -> open(player, block, mine),
                        value -> adjust(gui, block, mine, value / 100.0 - mine.getPercentage(block))));
        fineAdjusters(gui, 4, new double[]{0.1, 0.25, 0.5}, MenuCommonText.ADJUST_REMOVE_PERCENT, MenuCommonText.ADJUST_ADD_PERCENT,
                delta -> adjust(gui, block, mine, delta / 100.0), fineTuneLabel());

        gui.open(player);
    }

    private void adjust(Gui gui, String block, BasicMine mine, double delta) {
        double oldPercent = mine.getPercentage(block);
        double newPercent;

        if (delta < 0) {
            newPercent = Math.max(0, oldPercent + delta);
        } else {
            double totalWithoutCurrent = mine.getTotalPercentage() - oldPercent;
            newPercent = oldPercent + delta;
            if (totalWithoutCurrent + newPercent > 1.0) {
                newPercent = 1.0 - totalWithoutCurrent;
            }
            newPercent = Math.clamp(newPercent, 0, 1);
        }

        mine.setPercentage(block, newPercent);
        renderBlockDisplay(gui, block, mine);
        renderAllBlocksLore(gui, mine);
        gui.update();
    }

    private void renderBlockDisplay(Gui gui, String block, BasicMine mine) {
        String percent = Numbers.percent(mine.getPercentage(block));
        gui.setItem(2, 5, Btn.of(ItemUtils.getItemStackFromName(block),
                        MessageUtils.format(blockPercentName, "percent", percent))
                .build());
    }

    private void renderAllBlocksLore(Gui gui, BasicMine mine) {
        Btn btn = Btn.of(Material.WRITABLE_BOOK, allBlocksName);
        for (Map.Entry<String, Double> material : mine.getMaterials()) {
            btn.lore(allBlocksLore,
                    "material", material.getKey(),
                    "percent", Numbers.percent(material.getValue()));
        }
        gui.setItem(2, 7, btn.build());
    }
}
