package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.gui.buttons.ToggleButton;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.mine.impl.EnchantmentMineRequirement;
import me.simplyran.simplymines.utils.EnchantmentUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Map;

public class MinEnchantmentsGUI extends Menu {

    private static final String PATH = "menus.requirements.min-enchantments.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Min Enchantments");
    private final ConfigData<String> toggleLabel = lang(PATH + "toggle-label", "Min Enchantments");
    private final ConfigData<String> addEnchantment = lang(PATH + "add-enchantment", "<#7bd88f>Add Enchantment");
    private final ConfigData<String> enchantmentName = lang(PATH + "enchantment-name", "<#ffd166><enchantment>");
    private final ConfigData<String> levelLore = lang(PATH + "level-lore", "<#8b9bb4>Required Level: <white><level>");
    private final ConfigData<String> leftClickIncrease = lang(PATH + "left-click-increase", "<#8b9bb4>Left click to increase level");
    private final ConfigData<String> rightClickDecrease = lang(PATH + "right-click-decrease", "<#8b9bb4>Right click to decrease level");

    public MinEnchantmentsGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine) {
        EnchantmentMineRequirement req = getOrCreate(mine);

        PaginatedGui gui = createPaginated(3, MessageUtils.format(title),
                () -> services.guiManager().getMineRequirementsGUI().open(player, mine),
                () -> services.mineManager().saveMineAsync(mine));

        new ToggleButton(gui, 3, 5, MessageUtils.format(toggleLabel), req::isEnabled, req::setEnabled, null).render();

        gui.setItem(3, 9, Btn.of(Material.EMERALD, addEnchantment)
                .onClick(event -> services.guiManager().getAddEnchantmentGUI().open(player, mine))
                .build());

        for (Map.Entry<String, Integer> entry : req.getEnchantments().entrySet()) {
            String key = entry.getKey();

            gui.addItem(Btn.of(Material.ENCHANTED_BOOK, enchantmentName,
                            "enchantment", EnchantmentUtils.displayName(key, entry.getValue()))
                    .lore(levelLore, "level", String.valueOf(entry.getValue()))
                    .lore(Component.empty())
                    .lore(leftClickIncrease)
                    .lore(rightClickDecrease)
                    .lore(MenuCommonText.SHIFT_RIGHT_CLICK_REMOVE)
                    .onClick(event -> {
                        Integer current = req.getEnchantments().get(key);
                        if (current == null) return;

                        ClickType click = event.getClick();
                        if (click == ClickType.SHIFT_RIGHT) {
                            req.removeEnchantment(key);
                        } else if (click == ClickType.LEFT || click == ClickType.SHIFT_LEFT) {
                            req.setLevel(key, current + 1);
                        } else if (click == ClickType.RIGHT) {
                            req.setLevel(key, current - 1);
                        } else {
                            return;
                        }
                        reopenLater(() -> open(player, mine));
                    })
                    .build());
        }

        gui.open(player);
    }

    private EnchantmentMineRequirement getOrCreate(BasicMine mine) {
        EnchantmentMineRequirement req = mine.getMineRequirement(EnchantmentMineRequirement.class);
        if (req == null) {
            req = new EnchantmentMineRequirement(services.configManager());
            req.setEnabled(false);
            mine.addMineRequirement(req);
        }
        return req;
    }
}
