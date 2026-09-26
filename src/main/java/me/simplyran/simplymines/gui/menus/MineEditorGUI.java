package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.Numbers;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.BoxedRegion;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.ItemUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MineEditorGUI extends Menu {

    private static final String PATH = "menus.mine-editor.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Editing <#ffd166><mine>");
    private final ConfigData<String> infoButtonName = lang(PATH + "info-button-name", "<#ffd166><mine>");
    private final ConfigData<String> teleportButtonName = lang(PATH + "teleport-button-name", "<#c792ea>Teleport To Mine");
    private final ConfigData<String> teleportButtonLore = lang(PATH + "teleport-button-lore", "<#8b9bb4>Goes to the teleport point, or the mine corner if none is set");
    private final ConfigData<String> settingsButtonName = lang(PATH + "settings-button-name", "<#ffd166>Mine Settings");
    private final ConfigData<String> settingsButtonLore = lang(PATH + "settings-button-lore", "<#8b9bb4>Drops, pickup, physics and more");
    private final ConfigData<String> editBlocksName = lang(PATH + "edit-blocks-name", "<#ffd166>Edit Blocks");
    private final ConfigData<String> resetRequirementsName = lang(PATH + "reset-requirements-name", "<#ffd166>Reset Requirements");
    private final ConfigData<String> resetRequirementsLore = lang(PATH + "reset-requirements-lore", "<#8b9bb4>Choose how and when this mine resets");
    private final ConfigData<String> warnSettingsName = lang(PATH + "warn-settings-name", "<#ffd166>Warn Settings");
    private final ConfigData<String> warnSettingsLore = lang(PATH + "warn-settings-lore", "<#8b9bb4>Warning times and distance");
    private final ConfigData<String> mineRequirementsName = lang(PATH + "mine-requirements-name", "<#ffd166>Mine Requirements");
    private final ConfigData<String> mineRequirementsLore = lang(PATH + "mine-requirements-lore", "<#8b9bb4>Who can mine here (tool or permission)");
    private final ConfigData<String> infoWorld = lang(PATH + "info-lore.world", "<#8b9bb4>World: <white><world>");
    private final ConfigData<String> infoBlocks = lang(PATH + "info-lore.blocks", "<#8b9bb4>Size: <white><blocks> blocks");
    private final ConfigData<String> infoCorner1 = lang(PATH + "info-lore.corner-1", "<#ffd166>Corner 1");
    private final ConfigData<String> infoCorner2 = lang(PATH + "info-lore.corner-2", "<#ffd166>Corner 2");
    private final ConfigData<String> infoCoords = lang(PATH + "info-lore.coords", "<#8b9bb4>X <white><x>  <#8b9bb4>Y <white><y>  <#8b9bb4>Z <white><z>");

    public MineEditorGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, String mineName) {
        BasicMine mine = services.mineManager().getMine(mineName);
        if (mine == null) {
            player.closeInventory();
            return;
        }

        GuiManager guiManager = services.guiManager();
        Gui gui = createGui(5, MessageUtils.format(title, "mine", mineName),
                () -> guiManager.getMainMenuGUI().open(player),
                () -> services.mineManager().saveMineAsync(mine));

        gui.setItem(2, 5, buildInfo(mine, mineName));

        gui.setItem(3, 3, Btn.of(ItemUtils.getItemStackFromName(mine.getMainMaterial()), MessageUtils.format(editBlocksName))
                .lore(buildBlocksLore(mine))
                .onClick(event -> guiManager.getBlocksGUI().open(player, mine))
                .build());

        gui.setItem(3, 5, Btn.of(Material.COMPARATOR, settingsButtonName)
                .lore(settingsButtonLore)
                .onClick(event -> guiManager.getMineSettingsGUI().open(player, mine))
                .build());

        gui.setItem(3, 7, Btn.of(Material.REDSTONE_TORCH, warnSettingsName)
                .lore(warnSettingsLore)
                .onClick(event -> guiManager.getWarnSettingsGUI().open(player, mine))
                .build());

        gui.setItem(4, 3, Btn.of(Material.CLOCK, resetRequirementsName)
                .lore(resetRequirementsLore)
                .onClick(event -> guiManager.getResetRequirementsGUI().open(player, mine))
                .build());

        gui.setItem(4, 5, Btn.of(Material.ENDER_PEARL, teleportButtonName)
                .lore(teleportButtonLore)
                .onClick(event -> teleport(player, mine))
                .build());

        gui.setItem(4, 7, Btn.of(Material.GOLDEN_PICKAXE, mineRequirementsName)
                .lore(mineRequirementsLore)
                .onClick(event -> guiManager.getMineRequirementsGUI().open(player, mine))
                .build());

        gui.open(player);
    }

    private void teleport(Player player, BasicMine mine) {
        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);

        // No teleport location set: fall back to the mine's corner.
        if (mine.getTeleportLocation() == null) {
            BoxedRegion region = mine.getRegion();
            player.teleport(new Location(region.getWorld(), region.getMaxX(), region.getMaxY(), region.getMaxZ()));
            return;
        }
        player.teleportAsync(mine.getTeleportLocation());
    }

    private GuiItem buildInfo(BasicMine mine, String mineName) {
        BoxedRegion region = mine.getRegion();
        return Btn.of(Material.MAP, infoButtonName, "mine", mineName)
                .lore(infoWorld, "world", region.getWorld().getName())
                .lore(infoBlocks, "blocks", String.format("%,d", region.getBlockCount()))
                .lore(Component.empty())
                .lore(infoCorner1)
                .lore(infoCoords,
                        "x", String.valueOf(region.getMaxX()),
                        "y", String.valueOf(region.getMaxY()),
                        "z", String.valueOf(region.getMaxZ()))
                .lore(Component.empty())
                .lore(infoCorner2)
                .lore(infoCoords,
                        "x", String.valueOf(region.getMinX()),
                        "y", String.valueOf(region.getMinY()),
                        "z", String.valueOf(region.getMinZ()))
                .build();
    }

    private List<Component> buildBlocksLore(BasicMine mine) {
        List<Component> lines = new ArrayList<>();
        lines.add(MessageUtils.format(MenuCommonText.MATERIALS_HEADER));
        for (Map.Entry<String, Double> material : mine.getMaterials()) {
            lines.add(MessageUtils.format(MenuCommonText.MATERIAL_LINE,
                    "material", material.getKey(),
                    "percent", Numbers.percent(material.getValue())));
        }
        return lines;
    }
}
