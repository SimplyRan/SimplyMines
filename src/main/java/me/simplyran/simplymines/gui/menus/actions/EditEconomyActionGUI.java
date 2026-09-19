package me.simplyran.simplymines.gui.menus.actions;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.actions.impl.EconomyAction;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.buttons.AdjustButton;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class EditEconomyActionGUI {

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.actions.edit-economy.title", "Edit Economy Action");
    private final ConfigData<String> amountDisplayName = ConfigFactory.newConfigData(
            "menus.actions.edit-economy.amount-display-name", "<white>Amount: <amount>");

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public EditEconomyActionGUI(ConfigManager configManager, SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;

        configManager.registerLang(title);
        configManager.registerLang(amountDisplayName);
    }

    public void open(Player player, String block, BasicMine mine, EconomyAction action) {
        Gui gui = Gui.gui()
                .rows(4)
                .title(MessageUtils.format(title))
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            mineManager.saveMineAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getBlockActionsGUI().open(player, block, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(4, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.BACK).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        gui.setItem(1, 7,
                ItemBuilder.from(Material.BARRIER)
                        .name(MessageUtils.format(MenuCommonText.REMOVE_ACTION).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> {
                            mine.removeAction(block, action);
                            mineManager.saveMineAsync(mine);
                            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getBlockActionsGUI().open(player, block, mine));
                        }));

        renderDisplay(gui, action);

        new AdjustButton(gui, 2, 2, Material.RED_DYE, 100, MenuCommonText.ADJUST_REMOVE_AMOUNT, NamedTextColor.RED,
                delta -> adjustAmount(gui, action, -delta)).render();
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 10, MenuCommonText.ADJUST_REMOVE_AMOUNT, NamedTextColor.RED,
                delta -> adjustAmount(gui, action, -delta)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, MenuCommonText.ADJUST_REMOVE_AMOUNT, NamedTextColor.RED,
                delta -> adjustAmount(gui, action, -delta)).render();

        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, MenuCommonText.ADJUST_ADD_AMOUNT, NamedTextColor.GREEN,
                delta -> adjustAmount(gui, action, delta)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 10, MenuCommonText.ADJUST_ADD_AMOUNT, NamedTextColor.GREEN,
                delta -> adjustAmount(gui, action, delta)).render();
        new AdjustButton(gui, 2, 8, Material.LIME_DYE, 100, MenuCommonText.ADJUST_ADD_AMOUNT, NamedTextColor.GREEN,
                delta -> adjustAmount(gui, action, delta)).render();

        new AdjustButton(gui, 3, 2, Material.RED_DYE, 10, MenuCommonText.ADJUST_REMOVE_PERCENT, NamedTextColor.RED,
                delta -> adjustChance(gui, action, -delta / 100.0)).render();
        new AdjustButton(gui, 3, 3, Material.RED_DYE, 5, MenuCommonText.ADJUST_REMOVE_PERCENT, NamedTextColor.RED,
                delta -> adjustChance(gui, action, -delta / 100.0)).render();
        new AdjustButton(gui, 3, 4, Material.RED_DYE, 1, MenuCommonText.ADJUST_REMOVE_PERCENT, NamedTextColor.RED,
                delta -> adjustChance(gui, action, -delta / 100.0)).render();

        new AdjustButton(gui, 3, 6, Material.LIME_DYE, 1, MenuCommonText.ADJUST_ADD_PERCENT, NamedTextColor.GREEN,
                delta -> adjustChance(gui, action, delta / 100.0)).render();
        new AdjustButton(gui, 3, 7, Material.LIME_DYE, 5, MenuCommonText.ADJUST_ADD_PERCENT, NamedTextColor.GREEN,
                delta -> adjustChance(gui, action, delta / 100.0)).render();
        new AdjustButton(gui, 3, 8, Material.LIME_DYE, 10, MenuCommonText.ADJUST_ADD_PERCENT, NamedTextColor.GREEN,
                delta -> adjustChance(gui, action, delta / 100.0)).render();

        gui.open(player);
    }

    private void adjustAmount(Gui gui, EconomyAction action, double delta) {
        action.setAmount(Math.max(0, action.getAmount() + delta));
        renderDisplay(gui, action);
        gui.update();
    }

    private void adjustChance(Gui gui, EconomyAction action, double delta) {
        action.setChance(action.getChance() + delta);
        renderDisplay(gui, action);
        gui.update();
    }

    private void renderDisplay(Gui gui, EconomyAction action) {
        int chancePercent = (int) Math.round(action.getChance() * 100);
        gui.setItem(1, 5,
                ItemBuilder.from(Material.GOLD_INGOT)
                        .name(MessageUtils.format(amountDisplayName, "amount", String.valueOf(action.getAmount()))
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(MessageUtils.format(MenuCommonText.CHANCE_LORE, "percent", String.valueOf(chancePercent))
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem());
    }
}
