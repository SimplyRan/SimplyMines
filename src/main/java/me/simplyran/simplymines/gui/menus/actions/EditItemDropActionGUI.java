package me.simplyran.simplymines.gui.menus.actions;

import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.actions.impl.ItemDropAction;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.gui.Numbers;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EditItemDropActionGUI extends Menu {

    private static final String PATH = "menus.actions.edit-item-drop.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Edit Item Drop");
    private final ConfigData<String> setHeldName = lang(PATH + "set-held-name", "<#ffd166>Set to Held Item");
    private final ConfigData<String> setHeldLore1 = lang(PATH + "set-held-lore-1", "<#8b9bb4>Click while holding an item");
    private final ConfigData<String> setHeldLore2 = lang(PATH + "set-held-lore-2", "<dark_gray>(or click an item in your inventory below)");

    private final ConfigData<String> mustHoldItem = lang("messages.action-edit-item-drop-must-hold", "<#ef6f6c>You must be holding an item.");

    public EditItemDropActionGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, String block, BasicMine mine, ItemDropAction action) {
        Runnable back = () -> services.guiManager().getBlockActionsGUI().open(player, block, mine);
        Runnable self = () -> open(player, block, mine, action);
        Gui gui = createGui(5, MessageUtils.format(title), back, () -> services.mineManager().saveMineAsync(mine));

        gui.setPlayerInventoryAction(event -> {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            setItem(gui, action, clicked);
        });

        gui.setItem(2, 3, Btn.of(Material.HOPPER, setHeldName)
                .lore(setHeldLore1)
                .lore(setHeldLore2)
                .onClick(event -> {
                    ItemStack held = player.getInventory().getItemInMainHand();
                    if (held.getType() == Material.AIR) {
                        player.sendMessage(MessageUtils.format(player, mustHoldItem));
                        return;
                    }
                    setItem(gui, action, held);
                })
                .build());

        gui.setItem(2, 7, Btn.of(Material.BARRIER, MenuCommonText.REMOVE_ACTION)
                .onClick(event -> {
                    mine.removeAction(block, action);
                    services.mineManager().saveMineAsync(mine);
                    reopenLater(back);
                })
                .build());

        renderDisplay(gui, action);
        adjusters(gui, 3, new int[]{1, 5, 10}, MenuCommonText.ADJUST_REMOVE_AMOUNT, MenuCommonText.ADJUST_ADD_AMOUNT,
                delta -> adjustAmount(gui, action, delta),
                typeValueButton(player, self, value -> adjustAmount(gui, action, (int) Math.round(value) - action.getAmount())));
        adjusters(gui, 4, new int[]{1, 5, 10}, MenuCommonText.ADJUST_REMOVE_PERCENT, MenuCommonText.ADJUST_ADD_PERCENT,
                delta -> adjustChance(gui, action, delta / 100.0),
                typeValueButton(player, self, value -> adjustChance(gui, action, value / 100.0 - action.getChance())));

        gui.open(player);
    }

    private void setItem(Gui gui, ItemDropAction action, ItemStack item) {
        action.setItemStack(item);
        renderDisplay(gui, action);
        gui.update();
    }

    private void adjustAmount(Gui gui, ItemDropAction action, int delta) {
        action.setAmount(action.getAmount() + delta);
        renderDisplay(gui, action);
        gui.update();
    }

    private void adjustChance(Gui gui, ItemDropAction action, double delta) {
        action.setChance(Math.clamp(action.getChance() + delta, 0, 1));
        renderDisplay(gui, action);
        gui.update();
    }

    private void renderDisplay(Gui gui, ItemDropAction action) {
        String chancePercent = Numbers.percent(action.getChance());
        gui.setItem(2, 5, Btn.of(action.getItemStack())
                .amount(action.getAmount())
                .lore(MessageUtils.format(MenuCommonText.AMOUNT_LORE, "amount", String.valueOf(action.getAmount()))
                        .colorIfAbsent(NamedTextColor.WHITE))
                .lore(MessageUtils.format(MenuCommonText.CHANCE_LORE, "percent", chancePercent)
                        .colorIfAbsent(NamedTextColor.WHITE))
                .build());
    }
}
