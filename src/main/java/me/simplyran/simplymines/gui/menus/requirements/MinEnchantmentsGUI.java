package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.buttons.ToggleButton;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.mine.impl.EnchantmentMineRequirement;
import me.simplyran.simplymines.utils.EnchantmentUtils;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.List;
import java.util.Map;

public class MinEnchantmentsGUI {

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.requirements.min-enchantments.title", "Min Enchantments");
    private final ConfigData<String> toggleLabel = ConfigFactory.newConfigData(
            "menus.requirements.min-enchantments.toggle-label", "Min Enchantments");
    private final ConfigData<String> addEnchantment = ConfigFactory.newConfigData(
            "menus.requirements.min-enchantments.add-enchantment", "<green>Add Enchantment");
    private final ConfigData<String> enchantmentName = ConfigFactory.newConfigData(
            "menus.requirements.min-enchantments.enchantment-name", "<yellow><enchantment>");
    private final ConfigData<String> levelLore = ConfigFactory.newConfigData(
            "menus.requirements.min-enchantments.level-lore", "<gray>Required Level: <white><level>");
    private final ConfigData<String> leftClickIncrease = ConfigFactory.newConfigData(
            "menus.requirements.min-enchantments.left-click-increase", "<gray>Left click to increase level");
    private final ConfigData<String> rightClickDecrease = ConfigFactory.newConfigData(
            "menus.requirements.min-enchantments.right-click-decrease", "<gray>Right click to decrease level");

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;
    private final ConfigManager configManager;

    public MinEnchantmentsGUI(ConfigManager configManager,
                              SimplyMines plugin,
                              MineManager mineManager,
                              GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;
        this.configManager = configManager;

        configManager.registerLang(title);
        configManager.registerLang(toggleLabel);
        configManager.registerLang(addEnchantment);
        configManager.registerLang(enchantmentName);
        configManager.registerLang(levelLore);
        configManager.registerLang(leftClickIncrease);
        configManager.registerLang(rightClickDecrease);
    }

    public void open(Player player, BasicMine mine) {
        EnchantmentMineRequirement req = getOrCreate(mine);

        PaginatedGui gui = Gui.paginated()
                .title(MessageUtils.format(title))
                .rows(3)
                .pageSize(18)
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW
                    || event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;
            mineManager.saveMineAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMineRequirementsGUI().open(player, mine));
        });

        GuiUtils.fillRow(gui, 3, Material.WHITE_STAINED_GLASS_PANE);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.BACK).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> {
                            mineManager.saveMineAsync(mine);
                            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMineRequirementsGUI().open(player, mine));
                        }));

        gui.setItem(3, 3,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.PREVIOUS).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> gui.previous()));

        new ToggleButton(gui, 3, 5, MessageUtils.plainFormat(toggleLabel), req::isEnabled, req::setEnabled, null).render();

        gui.setItem(3, 7,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.NEXT).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> gui.next()));

        gui.setItem(3, 9,
                ItemBuilder.from(Material.EMERALD)
                        .name(MessageUtils.format(addEnchantment).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> guiManager.getAddEnchantmentGUI().open(player, mine)));

        for (Map.Entry<String, Integer> entry : req.getEnchantments().entrySet()) {
            String key = entry.getKey();
            int level = entry.getValue();

            gui.addItem(ItemBuilder.from(Material.ENCHANTED_BOOK)
                    .name(MessageUtils.format(enchantmentName, "enchantment", EnchantmentUtils.displayName(key, level))
                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                    .lore(List.of(
                            MessageUtils.format(levelLore, "level", String.valueOf(level))
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                            Component.empty(),
                            MessageUtils.format(leftClickIncrease).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                            MessageUtils.format(rightClickDecrease).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                            MessageUtils.format(MenuCommonText.SHIFT_RIGHT_CLICK_REMOVE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    ))
                    .asGuiItem(event -> {
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
                        Bukkit.getScheduler().runTask(plugin, () -> open(player, mine));
                    }));
        }

        gui.open(player);
    }

    private EnchantmentMineRequirement getOrCreate(BasicMine mine) {
        EnchantmentMineRequirement req = mine.getMineRequirement(EnchantmentMineRequirement.class);
        if (req == null) {
            req = new EnchantmentMineRequirement(configManager);
            req.setEnabled(false);
            mine.addMineRequirement(req);
        }
        return req;
    }
}
