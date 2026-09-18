package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.MenuCommonText;
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

public class MineEditorGUI {

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.mine-editor.title", "<yellow>Editing <mine>");
    private final ConfigData<String> infoButtonName = ConfigFactory.newConfigData(
            "menus.mine-editor.info-button-name", "<white><mine> Info");
    private final ConfigData<String> teleportButtonName = ConfigFactory.newConfigData(
            "menus.mine-editor.teleport-button-name", "<light_purple>Teleport To Mine");
    private final ConfigData<String> teleportButtonLore = ConfigFactory.newConfigData(
            "menus.mine-editor.teleport-button-lore", "<gray>Requires a teleport location to be set");
    private final ConfigData<String> settingsButtonName = ConfigFactory.newConfigData(
            "menus.mine-editor.settings-button-name", "<yellow>Mine Settings");
    private final ConfigData<String> settingsButtonLore = ConfigFactory.newConfigData(
            "menus.mine-editor.settings-button-lore", "<gray>All toggles: drops, pickup, physics, warnings...");
    private final ConfigData<String> editBlocksName = ConfigFactory.newConfigData(
            "menus.mine-editor.edit-blocks-name", "<yellow>Edit Blocks");
    private final ConfigData<String> resetRequirementsName = ConfigFactory.newConfigData(
            "menus.mine-editor.reset-requirements-name", "<yellow>Reset Requirements");
    private final ConfigData<String> resetRequirementsLore = ConfigFactory.newConfigData(
            "menus.mine-editor.reset-requirements-lore", "<gray>Configure how/when this mine resets");
    private final ConfigData<String> warnSettingsName = ConfigFactory.newConfigData(
            "menus.mine-editor.warn-settings-name", "<yellow>Warn Settings");
    private final ConfigData<String> warnSettingsLore = ConfigFactory.newConfigData(
            "menus.mine-editor.warn-settings-lore", "<gray>Configure warn seconds & warn distance");
    private final ConfigData<String> mineRequirementsName = ConfigFactory.newConfigData(
            "menus.mine-editor.mine-requirements-name", "<yellow>Mine Requirements");
    private final ConfigData<String> mineRequirementsLore = ConfigFactory.newConfigData(
            "menus.mine-editor.mine-requirements-lore", "<gray>Configure who can mine here (tool/permission)");
    private final ConfigData<String> infoWorld = ConfigFactory.newConfigData(
            "menus.mine-editor.info-lore.world", "<gray>▸ World: <yellow><world>");
    private final ConfigData<String> infoCorner1 = ConfigFactory.newConfigData(
            "menus.mine-editor.info-lore.corner-1", "<gold><bold>Corner 1");
    private final ConfigData<String> infoCorner2 = ConfigFactory.newConfigData(
            "menus.mine-editor.info-lore.corner-2", "<gold><bold>Corner 2");
    private final ConfigData<String> infoCoords = ConfigFactory.newConfigData(
            "menus.mine-editor.info-lore.coords", "<gray>  X: <white><x><gray>  Y: <white><y><gray>  Z: <white><z>");

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public MineEditorGUI(ConfigManager configManager, SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;

        configManager.registerLang(title);
        configManager.registerLang(infoButtonName);
        configManager.registerLang(teleportButtonName);
        configManager.registerLang(teleportButtonLore);
        configManager.registerLang(settingsButtonName);
        configManager.registerLang(settingsButtonLore);
        configManager.registerLang(editBlocksName);
        configManager.registerLang(resetRequirementsName);
        configManager.registerLang(resetRequirementsLore);
        configManager.registerLang(warnSettingsName);
        configManager.registerLang(warnSettingsLore);
        configManager.registerLang(mineRequirementsName);
        configManager.registerLang(mineRequirementsLore);
        configManager.registerLang(infoWorld);
        configManager.registerLang(infoCorner1);
        configManager.registerLang(infoCorner2);
        configManager.registerLang(infoCoords);
    }

    public void open(Player player, String mineName) {
        BasicMine mine = mineManager.getMine(mineName);
        if (mine == null) {
            player.closeInventory();
            return;
        }

        Gui gui = Gui.gui()
                .title(MessageUtils.format(title, "mine", mineName))
                .rows(3)
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW
                    || event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMainMenuGUI().open(player));
            mineManager.saveMineAsync(mine);
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.BACK)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> guiManager.getMainMenuGUI().open(player)));

        gui.setItem(2, 3,
                ItemBuilder.from(Material.WRITABLE_BOOK)
                        .name(MessageUtils.format(infoButtonName, "mine", mineName)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(buildMineInfoLore(mine))
                        .asGuiItem());

        gui.setItem(2, 5,
                ItemBuilder.from(Material.ENDER_PEARL)
                        .name(MessageUtils.format(teleportButtonName)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(List.of(MessageUtils.format(teleportButtonLore)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)))
                        .asGuiItem(event -> {
                            if (mine.getTeleportLocation() == null) return;
                            event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                            player.teleportAsync(mine.getTeleportLocation());
                        }));

        gui.setItem(2, 7,
                ItemBuilder.from(Material.COMPARATOR)
                        .name(MessageUtils.format(settingsButtonName)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(List.of(MessageUtils.format(settingsButtonLore)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)))
                        .asGuiItem(event -> guiManager.getMineSettingsGUI().open(player, mine)));

        gui.setItem(2, 2,
                ItemBuilder.from(ItemUtils.getItemStackFromName(mine.getMainMaterial()))
                        .name(MessageUtils.format(editBlocksName)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(buildBlocksLore(mine))
                        .asGuiItem(event -> guiManager.getBlocksGUI().open(player, mine)));

        gui.setItem(2, 4,
                ItemBuilder.from(Material.CLOCK)
                        .name(MessageUtils.format(resetRequirementsName)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(List.of(MessageUtils.format(resetRequirementsLore)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)))
                        .asGuiItem(event -> guiManager.getResetRequirementsGUI().open(player, mine)));

        gui.setItem(2, 6,
                ItemBuilder.from(Material.REDSTONE_TORCH)
                        .name(MessageUtils.format(warnSettingsName)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(List.of(MessageUtils.format(warnSettingsLore)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)))
                        .asGuiItem(event -> guiManager.getWarnSettingsGUI().open(player, mine)));

        gui.setItem(2, 8,
                ItemBuilder.from(Material.GOLDEN_PICKAXE)
                        .name(MessageUtils.format(mineRequirementsName)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(List.of(MessageUtils.format(mineRequirementsLore)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)))
                        .asGuiItem(event -> guiManager.getMineRequirementsGUI().open(player, mine)));

        gui.open(player);
    }

    private List<Component> buildBlocksLore(BasicMine mine) {
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtils.format(MenuCommonText.MATERIALS_HEADER)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));

        for (Map.Entry<String, Double> material : mine.getMaterials()) {
            lore.add(MessageUtils.format(MenuCommonText.MATERIAL_LINE,
                            "material", material.getKey(),
                            "percent", String.valueOf(material.getValue() * 100))
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }
        return lore;
    }

    private List<Component> buildMineInfoLore(BasicMine mine) {
        return List.of(
                MessageUtils.format(infoWorld, "world", mine.getRegion().getWorld().getName())
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),

                Component.empty(),

                MessageUtils.format(infoCorner1)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                MessageUtils.format(infoCoords,
                                "x", String.valueOf(mine.getRegion().getMaxX()),
                                "y", String.valueOf(mine.getRegion().getMaxY()),
                                "z", String.valueOf(mine.getRegion().getMaxZ()))
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),

                Component.empty(),

                MessageUtils.format(infoCorner2)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                MessageUtils.format(infoCoords,
                                "x", String.valueOf(mine.getRegion().getMinX()),
                                "y", String.valueOf(mine.getRegion().getMinY()),
                                "z", String.valueOf(mine.getRegion().getMinZ()))
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        );
    }
}
