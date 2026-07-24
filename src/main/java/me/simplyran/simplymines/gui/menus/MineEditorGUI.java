package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.ItemUtils;
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

/**
 * The per-mine editor hub. Pure navigation: every group of options lives in
 * its own sub-menu (settings, blocks, requirements, warnings).
 */
public class MineEditorGUI {

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public MineEditorGUI(SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;
    }

    public void open(Player player, String mineName) {
        BasicMine mine = mineManager.getMine(mineName);
        if (mine == null) {
            player.closeInventory();
            return;
        }

        Gui gui = Gui.gui()
                .title(Component.text("Editing " + mineName))
                .rows(4)
                .disableAllInteractions()
                .create();

        // Go back to main GUI, but only on a genuine player-initiated close
        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW
                    || event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMainMenuGUI().open(player));
            mineManager.saveMineAsync(mine);
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(4, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> guiManager.getMainMenuGUI().open(player)));

        gui.setItem(2, 3,
                ItemBuilder.from(Material.WRITABLE_BOOK)
                        .name(Component.text(mineName + " Info")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .lore(buildMineInfoLore(mine))
                        .asGuiItem());

        gui.setItem(2, 5,
                ItemBuilder.from(Material.ENDER_PEARL)
                        .name(Component.text("Teleport To Mine")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.LIGHT_PURPLE))
                        .lore(hubLore("Requires a teleport location to be set"))
                        .asGuiItem(event -> {
                            if (mine.getTeleportLocation() == null) return;
                            event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                            player.teleportAsync(mine.getTeleportLocation());
                        }));

        gui.setItem(2, 7,
                ItemBuilder.from(Material.COMPARATOR)
                        .name(Component.text("Mine Settings")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .lore(hubLore("All toggles: drops, pickup, physics, warnings..."))
                        .asGuiItem(event -> guiManager.getMineSettingsGUI().open(player, mine)));

        gui.setItem(3, 2,
                ItemBuilder.from(ItemUtils.getItemStackFromName(mine.getMainMaterial()))
                        .name(Component.text("Edit Blocks")
                                .color(NamedTextColor.YELLOW)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(buildBlocksLore(mine))
                        .asGuiItem(event -> guiManager.getBlocksGUI().open(player, mine)));

        gui.setItem(3, 4,
                ItemBuilder.from(Material.CLOCK)
                        .name(Component.text("Reset Requirements")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .lore(hubLore("Configure how/when this mine resets"))
                        .asGuiItem(event -> guiManager.getResetRequirementsGUI().open(player, mine)));

        gui.setItem(3, 6,
                ItemBuilder.from(Material.REDSTONE_TORCH)
                        .name(Component.text("Warn Settings")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .lore(hubLore("Configure warn seconds & warn distance"))
                        .asGuiItem(event -> guiManager.getWarnSettingsGUI().open(player, mine)));

        gui.setItem(3, 8,
                ItemBuilder.from(Material.GOLDEN_PICKAXE)
                        .name(Component.text("Mine Requirements")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .lore(hubLore("Configure who can mine here (tool/permission)"))
                        .asGuiItem(event -> guiManager.getMineRequirementsGUI().open(player, mine)));

        gui.open(player);
    }

    private List<Component> hubLore(String text) {
        return List.of(Component.text(text)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .color(NamedTextColor.GRAY));
    }

    private List<Component> buildBlocksLore(BasicMine mine) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Materials: ")
                .color(NamedTextColor.BLUE)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));

        for (Map.Entry<String, Double> material : mine.getMaterials()) {
            lore.add(Component.text("   " + material.getKey() + ": ")
                    .color(NamedTextColor.BLUE)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .append(Component.text((material.getValue() * 100) + "%")
                            .color(NamedTextColor.WHITE)));
        }
        return lore;
    }

    private List<Component> buildMineInfoLore(BasicMine mine) {
        return List.of(
                Component.text("▸ World: ")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.GRAY)
                        .append(Component.text(mine.getRegion().getWorld().getName())
                                .color(NamedTextColor.YELLOW)),

                Component.empty(),

                Component.text("Corner 1")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .decorate(TextDecoration.BOLD)
                        .color(NamedTextColor.GOLD),
                Component.text("  X: ")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.GRAY)
                        .append(Component.text(mine.getRegion().getMaxX()).color(NamedTextColor.WHITE))
                        .append(Component.text("  Y: ").color(NamedTextColor.GRAY))
                        .append(Component.text(mine.getRegion().getMaxY()).color(NamedTextColor.WHITE))
                        .append(Component.text("  Z: ").color(NamedTextColor.GRAY))
                        .append(Component.text(mine.getRegion().getMaxZ()).color(NamedTextColor.WHITE)),

                Component.empty(),

                Component.text("Corner 2")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .decorate(TextDecoration.BOLD)
                        .color(NamedTextColor.GOLD),
                Component.text("  X: ")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.GRAY)
                        .append(Component.text(mine.getRegion().getMinX()).color(NamedTextColor.WHITE))
                        .append(Component.text("  Y: ").color(NamedTextColor.GRAY))
                        .append(Component.text(mine.getRegion().getMinY()).color(NamedTextColor.WHITE))
                        .append(Component.text("  Z: ").color(NamedTextColor.GRAY))
                        .append(Component.text(mine.getRegion().getMinZ()).color(NamedTextColor.WHITE))
        );
    }
}
