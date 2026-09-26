package me.simplyran.simplymines.gui.menus.actions;

import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.actions.impl.EconomyAction;
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

public class EditEconomyActionGUI extends Menu {

    private static final String PATH = "menus.actions.edit-economy.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Edit Economy Action");
    private final ConfigData<String> amountDisplayName = lang(PATH + "amount-display-name", "<#ffd166>Amount: <white><amount>");

    public EditEconomyActionGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, String block, BasicMine mine, EconomyAction action) {
        Runnable back = () -> services.guiManager().getBlockActionsGUI().open(player, block, mine);
        Runnable self = () -> open(player, block, mine, action);
        Gui gui = createGui(5, MessageUtils.format(title), back, () -> services.mineManager().saveMineAsync(mine));

        gui.setItem(2, 7, Btn.of(Material.BARRIER, MenuCommonText.REMOVE_ACTION)
                .onClick(event -> {
                    mine.removeAction(block, action);
                    services.mineManager().saveMineAsync(mine);
                    reopenLater(back);
                })
                .build());

        renderDisplay(gui, action);
        adjusters(gui, 3, new int[]{1, 10, 100}, MenuCommonText.ADJUST_REMOVE_AMOUNT, MenuCommonText.ADJUST_ADD_AMOUNT,
                delta -> adjustAmount(gui, action, delta),
                typeValueButton(player, self, value -> adjustAmount(gui, action, value - action.getAmount())));
        adjusters(gui, 4, new int[]{1, 5, 10}, MenuCommonText.ADJUST_REMOVE_PERCENT, MenuCommonText.ADJUST_ADD_PERCENT,
                delta -> adjustChance(gui, action, delta / 100.0),
                typeValueButton(player, self, value -> adjustChance(gui, action, value / 100.0 - action.getChance())));

        gui.open(player);
    }

    private void adjustAmount(Gui gui, EconomyAction action, double delta) {
        action.setAmount(Math.max(0, action.getAmount() + delta));
        renderDisplay(gui, action);
        gui.update();
    }

    private void adjustChance(Gui gui, EconomyAction action, double delta) {
        action.setChance(Math.clamp(action.getChance() + delta, 0, 1));
        renderDisplay(gui, action);
        gui.update();
    }

    private void renderDisplay(Gui gui, EconomyAction action) {
        String chancePercent = Numbers.percent(action.getChance());
        gui.setItem(2, 5, Btn.of(Material.GOLD_INGOT, amountDisplayName, "amount", String.valueOf(action.getAmount()))
                .lore(MessageUtils.format(MenuCommonText.CHANCE_LORE, "percent", chancePercent)
                        .colorIfAbsent(NamedTextColor.WHITE))
                .build());
    }
}
