package me.simplyran.simplymines.gui.menus.blocks;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.buttons.AdjustButton;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Menu for adjusting a single block's drop chance within a mine.
 */
public class EditBlockGUI {

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.blocks.edit-block.title", "Edit Block Chances");
    private final ConfigData<String> blockPercentName = ConfigFactory.newConfigData(
            "menus.blocks.edit-block.block-percent-name", "Block Percent: <percent>%");
    private final ConfigData<String> allBlocksName = ConfigFactory.newConfigData(
            "menus.blocks.edit-block.all-blocks-name", "<aqua>All Blocks: ");
    private final ConfigData<String> allBlocksLore = ConfigFactory.newConfigData(
            "menus.blocks.edit-block.all-blocks-lore", "<gold>   <material>: <percent>%");

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public EditBlockGUI(ConfigManager configManager, SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;

        configManager.registerLang(title);
        configManager.registerLang(blockPercentName);
        configManager.registerLang(allBlocksName);
        configManager.registerLang(allBlocksLore);
    }

    public void open(Player player, String block, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .disableAllInteractions()
                .title(MessageUtils.format(title))
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            mineManager.saveMineAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getBlockOptionsGUI().open(player, block, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.BACK)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        renderBlockDisplay(gui, block, mine);
        renderAllBlocksLore(gui, mine);

        new AdjustButton(gui, 2, 2, Material.RED_DYE, 10, MenuCommonText.ADJUST_REMOVE_PERCENT, NamedTextColor.RED,
                delta -> adjust(gui, block, mine, -delta / 100.0)).render();
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, MenuCommonText.ADJUST_REMOVE_PERCENT, NamedTextColor.RED,
                delta -> adjust(gui, block, mine, -delta / 100.0)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, MenuCommonText.ADJUST_REMOVE_PERCENT, NamedTextColor.RED,
                delta -> adjust(gui, block, mine, -delta / 100.0)).render();

        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, MenuCommonText.ADJUST_ADD_PERCENT, NamedTextColor.GREEN,
                delta -> adjust(gui, block, mine, delta / 100.0)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, MenuCommonText.ADJUST_ADD_PERCENT, NamedTextColor.GREEN,
                delta -> adjust(gui, block, mine, delta / 100.0)).render();
        new AdjustButton(gui, 2, 8, Material.LIME_DYE, 10, MenuCommonText.ADJUST_ADD_PERCENT, NamedTextColor.GREEN,
                delta -> adjust(gui, block, mine, delta / 100.0)).render();

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
        double percent = mine.getPercentage(block);
        gui.setItem(2, 5,
                ItemBuilder.from(ItemUtils.getItemStackFromName(block))
                        .name(MessageUtils.format(blockPercentName, "percent", String.valueOf(Math.round(percent * 100)))
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem());
    }

    private void renderAllBlocksLore(Gui gui, BasicMine mine) {
        List<Component> lore = new ArrayList<>();
        for (Map.Entry<String, Double> materials : mine.getMaterials()) {
            lore.add(MessageUtils.format(allBlocksLore,
                            "material", materials.getKey(),
                            "percent", String.valueOf(Math.round(materials.getValue() * 100)))
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }

        gui.setItem(1, 5,
                ItemBuilder.from(Material.WRITABLE_BOOK)
                        .name(MessageUtils.format(allBlocksName)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(lore)
                        .asGuiItem());
    }
}
