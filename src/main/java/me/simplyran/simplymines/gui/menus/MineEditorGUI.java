package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.gui.buttons.ToggleButton;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.requirements.mine.impl.EfficiencyMineRequirement;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.ItemUtils;
import me.simplyran.simplymines.utils.MineSaver;
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
 * The per-mine editor menu — hub for direct toggles plus links into the more
 * specific settings menus (blocks, reset, warn, min efficiency).
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
        Gui mineGUI = Gui.gui()
                .title(Component.text("Editing " + mineName))
                .rows(6)
                .disableAllInteractions()
                .create();

        BasicMine mine = mineManager.getMine(mineName);
        if (mine == null) {
            player.closeInventory();
            return;
        }

        mineGUI.setItem(2, 5,
                ItemBuilder.from(Material.ENDER_PEARL)
                        .name(Component.text("Teleport To Mine")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.LIGHT_PURPLE))
                        .asGuiItem(event -> {
                            if (mine.getTeleportLocation() == null) return;
                            event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                            player.teleportAsync(mine.getTeleportLocation());
                        }));

        // Go back to main GUI, but only on a genuine player-initiated close
        mineGUI.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW
                    || event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMainMenuGUI().open(player));
            MineSaver.saveAsync(plugin, mine);
        });

        GuiUtils.fillBorder(mineGUI);

        mineGUI.setItem(6, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> guiManager.getMainMenuGUI().open(player)));

        // Info item
        mineGUI.setItem(3, 3,
                ItemBuilder.from(Material.WRITABLE_BOOK)
                        .name(Component.text(mineName + " Info")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .lore(buildMineInfoLore(mine))
                        .asGuiItem());

        // Edit blocks item
        List<Component> loreOfEditBlocks = new ArrayList<>();
        loreOfEditBlocks.add(Component.text("Materials: ")
                .color(NamedTextColor.BLUE)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        );

        for (Map.Entry<String, Double> material : mine.getMaterials()) {
            loreOfEditBlocks.add(Component.text("   " + material.getKey() + ": ")
                    .color(NamedTextColor.BLUE)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .append(Component.text((material.getValue() * 100) + "%")
                            .color(NamedTextColor.WHITE))
            );
        }

        mineGUI.setItem(3, 4,
                ItemBuilder.from(ItemUtils.getItemStackFromName(mine.getMainMaterial()))
                        .name(Component.text("Edit Blocks")
                                .color(NamedTextColor.YELLOW)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(loreOfEditBlocks)
                        .asGuiItem(event -> guiManager.getBlocksGUI().open(player, mine)));

        // Reset Settings hub (Timed + Percentage)
        mineGUI.setItem(3, 5,
                ItemBuilder.from(Material.CLOCK)
                        .name(Component.text("Reset Requirements").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW))
                        .lore(Component.text("Configure how/when this mine resets").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY))
                        .asGuiItem(event -> guiManager.getResetRequirementsGUI().open(player, mine)));


        // Warn Settings hub (Seconds + Distance)
        mineGUI.setItem(3, 6,
                ItemBuilder.from(Material.REDSTONE_TORCH)
                        .name(Component.text("Warn Settings")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .lore(Component.text("Configure warn seconds & warn distance")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.GRAY))
                        .asGuiItem(event -> guiManager.getWarnSettingsGUI().open(player, mine)));

        // Min Efficiency editor
        mineGUI.setItem(3, 7,
                ItemBuilder.from(Material.GOLDEN_PICKAXE)
                        .name(Component.text("Mine Requirements").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW))
                        .lore(Component.text("Configure who can mine here (tool/permission)").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY))
                        .asGuiItem(event -> guiManager.getMineRequirementsGUI().open(player, mine)));

        // Toggle buttons — each owns a distinct slot, no collisions
        new ToggleButton(mineGUI, 4, 3, "Mine Enabled",
                mine::isEnabled, mine::setEnabled, () -> MineSaver.saveAsync(plugin, mine)).render();

        new ToggleButton(mineGUI, 4, 4, "Warn Global",
                mine::isWarnGlobal, mine::setWarnGlobal, () -> MineSaver.saveAsync(plugin, mine)).render();

        new ToggleButton(mineGUI, 4, 5, "Warn Near",
                mine::isWarnNear, mine::setWarnNear, () -> MineSaver.saveAsync(plugin, mine)).render();

        new ToggleButton(mineGUI, 4, 6, "Teleport Players",
                mine::isTeleportPlayers, mine::setTeleportPlayers, () -> MineSaver.saveAsync(plugin, mine)).render();

        new ToggleButton(mineGUI, 4, 7, "Use Physics",
                mine::isUsePhysics, mine::setUsePhysics, () -> MineSaver.saveAsync(plugin, mine)).render();

        new ToggleButton(mineGUI, 5, 5, "Replace Mode",
                mine::isReplaceMode, mine::setReplaceMode, () -> MineSaver.saveAsync(plugin, mine)).render();


        mineGUI.open(player);
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


    private boolean minEfficiencyEnabled(BasicMine mine) {
        EfficiencyMineRequirement req = mine.getMineRequirement(EfficiencyMineRequirement.class);
        return req != null && req.isEnabled();
    }

    private String minEfficiencyLabel(BasicMine mine) {
        EfficiencyMineRequirement req = mine.getMineRequirement(EfficiencyMineRequirement.class);
        if (req == null || !req.isEnabled()) return "Disabled";
        return "Level " + req.getEfficiencyLevel() + " (Enabled)";
    }

}