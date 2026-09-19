package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.mine.impl.EnchantmentMineRequirement;
import me.simplyran.simplymines.utils.EnchantmentUtils;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class AddEnchantmentGUI {

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.requirements.add-enchantment.title", "Add Enchantment");
    private final ConfigData<String> enchantmentName = ConfigFactory.newConfigData(
            "menus.requirements.add-enchantment.enchantment-name", "<yellow><enchantment>");
    private final ConfigData<String> enchantmentLore = ConfigFactory.newConfigData(
            "menus.requirements.add-enchantment.enchantment-lore", "<gray>Click to require this enchantment");

    private final SimplyMines plugin;
    private final GuiManager guiManager;

    public AddEnchantmentGUI(ConfigManager configManager, SimplyMines plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;

        configManager.registerLang(title);
        configManager.registerLang(enchantmentName);
        configManager.registerLang(enchantmentLore);
    }

    public void open(Player player, BasicMine mine) {
        EnchantmentMineRequirement requirement = mine.getMineRequirement(EnchantmentMineRequirement.class);
        if (requirement == null) {
            guiManager.getMinEnchantmentsGUI().open(player, mine);
            return;
        }

        PaginatedGui gui = Gui.paginated()
                .rows(6)
                .title(MessageUtils.format(title))
                .pageSize(45)
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW
                    || event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMinEnchantmentsGUI().open(player, mine));
        });

        GuiUtils.fillRow(gui, 6, Material.WHITE_STAINED_GLASS_PANE);

        gui.setItem(6, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.BACK).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMinEnchantmentsGUI().open(player, mine))));

        gui.setItem(6, 3,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.PREVIOUS).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> gui.previous()));

        gui.setItem(6, 7,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.NEXT).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> gui.next()));

        for (Enchantment enchantment : EnchantmentUtils.allEnchantments()) {
            String key = enchantment.getKey().asString();
            if (requirement.hasEnchantment(key)) continue;

            gui.addItem(ItemBuilder.from(Material.ENCHANTED_BOOK)
                    .name(MessageUtils.format(enchantmentName, "enchantment", EnchantmentUtils.displayName(key))
                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                    .lore(MessageUtils.format(enchantmentLore).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                    .asGuiItem(event -> {
                        requirement.setLevel(key, 1);
                        Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMinEnchantmentsGUI().open(player, mine));
                    }));
        }

        gui.open(player);
    }
}
