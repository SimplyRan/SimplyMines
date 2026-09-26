package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.mine.impl.EnchantmentMineRequirement;
import me.simplyran.simplymines.utils.EnchantmentUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

public class AddEnchantmentGUI extends Menu {

    private static final String PATH = "menus.requirements.add-enchantment.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Add Enchantment");
    private final ConfigData<String> enchantmentName = lang(PATH + "enchantment-name", "<#ffd166><enchantment>");
    private final ConfigData<String> enchantmentLore = lang(PATH + "enchantment-lore", "<#8b9bb4>Click to require this enchantment");

    public AddEnchantmentGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine) {
        Runnable back = () -> services.guiManager().getMinEnchantmentsGUI().open(player, mine);

        EnchantmentMineRequirement requirement = mine.getMineRequirement(EnchantmentMineRequirement.class);
        if (requirement == null) {
            back.run();
            return;
        }

        PaginatedGui gui = createPaginated(6, MessageUtils.format(title), back, null);

        for (Enchantment enchantment : EnchantmentUtils.allEnchantments()) {
            String key = enchantment.getKey().asString();
            if (requirement.hasEnchantment(key)) continue;

            gui.addItem(Btn.of(Material.ENCHANTED_BOOK, enchantmentName, "enchantment", EnchantmentUtils.displayName(key))
                    .lore(enchantmentLore)
                    .onClick(event -> {
                        requirement.setLevel(key, 1);
                        reopenLater(back);
                    })
                    .build());
        }

        gui.open(player);
    }
}
