package me.simplyran.simplymines.gui.menus.actions;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.actions.impl.ItemDropAction;
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
import org.bukkit.inventory.ItemStack;

public class EditItemDropActionGUI {

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.actions.edit-item-drop.title", "Edit Item Drop");
    private final ConfigData<String> setHeldName = ConfigFactory.newConfigData(
            "menus.actions.edit-item-drop.set-held-name", "<yellow>Set to Held Item");
    private final ConfigData<String> setHeldLore1 = ConfigFactory.newConfigData(
            "menus.actions.edit-item-drop.set-held-lore-1", "<gray>Click while holding an item");
    private final ConfigData<String> setHeldLore2 = ConfigFactory.newConfigData(
            "menus.actions.edit-item-drop.set-held-lore-2", "<dark_gray>(or click an item in your inventory below)");

    private final ConfigData<String> mustHoldItem = ConfigFactory.newConfigData(
            "messages.action-edit-item-drop-must-hold", "<red>You must be holding an item.");

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public EditItemDropActionGUI(ConfigManager configManager, SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;

        configManager.registerLang(title);
        configManager.registerLang(setHeldName);
        configManager.registerLang(setHeldLore1);
        configManager.registerLang(setHeldLore2);
        configManager.registerLang(mustHoldItem);
    }

    public void open(Player player, String block, BasicMine mine, ItemDropAction action) {
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

        gui.setPlayerInventoryAction(event -> {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            action.setItemStack(clicked);
            renderDisplay(gui, action);
            gui.update();
        });

        gui.setItem(4, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.BACK).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        gui.setItem(1, 3,
                ItemBuilder.from(Material.HOPPER)
                        .name(MessageUtils.format(setHeldName).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(MessageUtils.format(setHeldLore1).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                                MessageUtils.format(setHeldLore2).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> {
                            ItemStack held = player.getInventory().getItemInMainHand();
                            if (held.getType() == Material.AIR) {
                                player.sendMessage(MessageUtils.format(player, mustHoldItem));
                                return;
                            }
                            action.setItemStack(held);
                            renderDisplay(gui, action);
                            gui.update();
                        }));

        gui.setItem(1, 7,
                ItemBuilder.from(Material.BARRIER)
                        .name(MessageUtils.format(MenuCommonText.REMOVE_ACTION).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> {
                            mine.removeAction(block, action);
                            mineManager.saveMineAsync(mine);
                            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getBlockActionsGUI().open(player, block, mine));
                        }));

        renderDisplay(gui, action);

        new AdjustButton(gui, 2, 2, Material.RED_DYE, 10, MenuCommonText.ADJUST_REMOVE_AMOUNT, NamedTextColor.RED,
                delta -> adjustAmount(gui, action, -delta)).render();
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, MenuCommonText.ADJUST_REMOVE_AMOUNT, NamedTextColor.RED,
                delta -> adjustAmount(gui, action, -delta)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, MenuCommonText.ADJUST_REMOVE_AMOUNT, NamedTextColor.RED,
                delta -> adjustAmount(gui, action, -delta)).render();

        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, MenuCommonText.ADJUST_ADD_AMOUNT, NamedTextColor.GREEN,
                delta -> adjustAmount(gui, action, delta)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, MenuCommonText.ADJUST_ADD_AMOUNT, NamedTextColor.GREEN,
                delta -> adjustAmount(gui, action, delta)).render();
        new AdjustButton(gui, 2, 8, Material.LIME_DYE, 10, MenuCommonText.ADJUST_ADD_AMOUNT, NamedTextColor.GREEN,
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

    private void adjustAmount(Gui gui, ItemDropAction action, int delta) {
        action.setAmount(action.getAmount() + delta);
        renderDisplay(gui, action);
        gui.update();
    }

    private void adjustChance(Gui gui, ItemDropAction action, double delta) {
        action.setChance(action.getChance() + delta);
        renderDisplay(gui, action);
        gui.update();
    }

    private void renderDisplay(Gui gui, ItemDropAction action) {
        int chancePercent = (int) Math.round(action.getChance() * 100);
        gui.setItem(1, 5,
                ItemBuilder.from(action.getItemStack())
                        .amount(Math.clamp(action.getAmount(), 1, 64))
                        .lore(MessageUtils.format(MenuCommonText.AMOUNT_LORE, "amount", String.valueOf(action.getAmount()))
                                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE),
                                MessageUtils.format(MenuCommonText.CHANCE_LORE, "percent", String.valueOf(chancePercent))
                                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem());
    }
}
