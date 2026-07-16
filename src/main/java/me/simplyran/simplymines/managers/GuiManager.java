package me.simplyran.simplymines.managers;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.gui.AdjustButton;
import me.simplyran.simplymines.gui.ToggleButton;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.ItemUtils;
import me.simplyran.simplymines.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GuiManager {

    private final SimplyMines plugin;
    private final MineManager mineManager;

    private static final int[] WARN_SECOND_OPTIONS = {1, 2, 5, 10, 15, 30, 60};

    public GuiManager(SimplyMines plugin, MineManager mineManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
    }

    private void saveAsync(BasicMine mine) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> JsonUtils.saveMine(plugin, mine));
    }

    // ---------------------------------------------------------------------
    // Main GUI
    // ---------------------------------------------------------------------

    public void openMainGUI(Player player) {
        PaginatedGui mainGUI = Gui.paginated()
                .title(Component.text("Select Mine"))
                .rows(6)
                .pageSize(45)
                .disableAllInteractions()
                .create();

        GuiUtils.fillRow(mainGUI, 6, Material.WHITE_STAINED_GLASS_PANE);

        mainGUI.setItem(6, 3,
                ItemBuilder.from(Material.ARROW).name(Component.text("Previous")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> mainGUI.previous()));

        mainGUI.setItem(6, 7,
                ItemBuilder.from(Material.ARROW).name(Component.text("Next")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> mainGUI.next()));

        mineManager.getMines().forEach(mine -> {
            String mineName = mine.getName();
            List<Component> lore = new ArrayList<>();

            lore.add(Component.text("Mine Enabled: ")
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .append(Component.text(mine.isEnabled())
                            .color(mine.isEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED))
            );

            lore.add(Component.text("Reset Time: ")
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .append(Component.text(mine.getResetTime())
                            .color(NamedTextColor.WHITE))
            );

            lore.add(Component.text("Warn Global: ")
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .append(Component.text(mine.isWarnGlobal())
                            .color(mine.isWarnGlobal() ? NamedTextColor.GREEN : NamedTextColor.RED))
            );

            lore.add(Component.text("Warn Near: ")
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .append(Component.text(mine.isWarnNear())
                            .color(mine.isWarnNear() ? NamedTextColor.GREEN : NamedTextColor.RED))
            );

            lore.add(Component.text("Warn Distance: ")
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .append(Component.text(mine.getWarnDistance())
                            .color(NamedTextColor.WHITE))
            );

            lore.add(Component.text("Teleport Players: ")
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .append(Component.text(mine.isTeleportPlayers())
                            .color(mine.isTeleportPlayers() ? NamedTextColor.GREEN : NamedTextColor.RED))
            );

            lore.add(Component.text("Reset At Percentage: ")
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .append(Component.text(mine.isResetAtPercentageEnabled()
                                    ? mine.getResetAtPercentage() + "%"
                                    : "Disabled")
                            .color(mine.isResetAtPercentageEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED))
            );

            lore.add(Component.text("Min Efficiency: ")
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .append(Component.text(mine.isMinEfficiencyEnabled()
                                    ? "Level " + mine.getMinEfficiency()
                                    : "Disabled")
                            .color(mine.isMinEfficiencyEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED))
            );

            lore.add(Component.text("Materials: ")
                    .color(NamedTextColor.BLUE)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            );

            for (Map.Entry<String, Double> material : mine.getMaterials()) {
                lore.add(Component.text("   " + material.getKey() + ": ")
                        .color(NamedTextColor.BLUE)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .append(Component.text((material.getValue() * 100) + "%")
                                .color(NamedTextColor.WHITE))
                );
            }

            GuiItem guiItem = ItemBuilder.from(ItemUtils.getItemStackFromName(mine.getMainMaterial()))
                    .name(Component.text(mineName)
                            .color(NamedTextColor.YELLOW)
                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                    .lore(lore)
                    .asGuiItem(event -> openMineGUI(player, mineName));

            mainGUI.addItem(guiItem);
        });

        mainGUI.open(player);
    }

    // ---------------------------------------------------------------------
    // Mine editor GUI
    // ---------------------------------------------------------------------

    public void openMineGUI(Player player, String mineName) {
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
            Bukkit.getScheduler().runTask(plugin, () -> openMainGUI(player));
            saveAsync(mine);
        });

        GuiUtils.fillBorder(mineGUI);



        mineGUI.setItem(6, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> openMainGUI(player)));

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
                        .asGuiItem(event -> openBlocksGUI(player, mine)));

        // Reset Settings hub (Timed + Percentage)
        mineGUI.setItem(3, 5,
                ItemBuilder.from(Material.CLOCK)
                        .name(Component.text("Reset Settings")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .lore(Component.text("Configure timed & percentage-based reset")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.GRAY))
                        .asGuiItem(event -> openResetSettingsGUI(player, mine)));

        // Warn Settings hub (Seconds + Distance)
        mineGUI.setItem(3, 6,
                ItemBuilder.from(Material.REDSTONE_TORCH)
                        .name(Component.text("Warn Settings")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .lore(Component.text("Configure warn seconds & warn distance")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.GRAY))
                        .asGuiItem(event -> openWarnSettingsGUI(player, mine)));

        // Min Efficiency editor
        mineGUI.setItem(3, 7,
                ItemBuilder.from(Material.GOLDEN_PICKAXE)
                        .name(Component.text("Min Efficiency")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .lore(Component.text(mine.isMinEfficiencyEnabled()
                                        ? "Level " + mine.getMinEfficiency() + " (Enabled)"
                                        : "Disabled")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(mine.isMinEfficiencyEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED))
                        .asGuiItem(event -> openChangeMinEfficiencyGUI(player, mine)));

        // Toggle buttons — each owns a distinct slot, no collisions
        new ToggleButton(mineGUI, 4, 3, "Mine Enabled",
                mine::isEnabled, mine::setEnabled, () -> saveAsync(mine)).render();

        new ToggleButton(mineGUI, 4, 4, "Warn Global",
                mine::isWarnGlobal, mine::setWarnGlobal, () -> saveAsync(mine)).render();

        new ToggleButton(mineGUI, 4, 5, "Warn Near",
                mine::isWarnNear, mine::setWarnNear, () -> saveAsync(mine)).render();

        new ToggleButton(mineGUI, 4, 6, "Teleport Players",
                mine::isTeleportPlayers, mine::setTeleportPlayers, () -> saveAsync(mine)).render();

        new ToggleButton(mineGUI, 4, 7, "Use Physics",
                mine::isUsePhysics, mine::setUsePhysics, () -> saveAsync(mine)).render();

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

    // ---------------------------------------------------------------------
    // Reset settings hub (Timed + Percentage)
    // ---------------------------------------------------------------------

    public void openResetSettingsGUI(Player player, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Reset Settings"))
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            saveAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> openMineGUI(player, mine.getName()));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        gui.setItem(2, 4,
                ItemBuilder.from(Material.CLOCK)
                        .name(Component.text("Reset Time (Timed)")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .lore(Component.text(mine.getResetTime() + "s")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> openChangeResetTimeGUI(player, mine)));

        gui.setItem(2, 6,
                ItemBuilder.from(Material.REPEATER)
                        .name(Component.text("Reset At Percentage")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .lore(Component.text(mine.isResetAtPercentageEnabled()
                                        ? mine.getResetAtPercentage() + "% left (Enabled)"
                                        : "Disabled")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(mine.isResetAtPercentageEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED))
                        .asGuiItem(event -> openChangeResetPercentageGUI(player, mine)));

        gui.open(player);
    }

    // ---------------------------------------------------------------------
    // Warn settings hub (Seconds + Distance)
    // ---------------------------------------------------------------------

    public void openWarnSettingsGUI(Player player, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Warn Settings"))
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            saveAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> openMineGUI(player, mine.getName()));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        gui.setItem(2, 4,
                ItemBuilder.from(Material.REDSTONE_TORCH)
                        .name(Component.text("Warn Seconds")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .asGuiItem(event -> openWarnSecondsGUI(player, mine)));

        gui.setItem(2, 6,
                ItemBuilder.from(Material.SPYGLASS)
                        .name(Component.text("Warn Distance")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .lore(Component.text(mine.getWarnDistance() + " Blocks")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> openChangeWarnDistanceGUI(player, mine)));

        gui.open(player);
    }

    // ---------------------------------------------------------------------
    // Warn distance GUI
    // ---------------------------------------------------------------------

    public void openChangeWarnDistanceGUI(Player player, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Change Warn Distance"))
                .disableAllInteractions()
                .create();

        // Go back to warn settings hub, but only on a genuine player-initiated close
        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            saveAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> openWarnSettingsGUI(player, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        renderWarnDistanceDisplay(gui, mine);

        // Remove buttons
        new AdjustButton(gui, 2, 2, Material.RED_DYE, 10, "Remove 10 blocks from Warn Distance", NamedTextColor.RED,
                delta -> adjustWarnDistance(gui, mine, -delta)).render();
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, "Remove 5 blocks from Warn Distance", NamedTextColor.RED,
                delta -> adjustWarnDistance(gui, mine, -delta)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, "Remove 1 block from Warn Distance", NamedTextColor.RED,
                delta -> adjustWarnDistance(gui, mine, -delta)).render();

        // Add buttons
        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, "Add 1 block to Warn Distance", NamedTextColor.GREEN,
                delta -> adjustWarnDistance(gui, mine, delta)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, "Add 5 blocks to Warn Distance", NamedTextColor.GREEN,
                delta -> adjustWarnDistance(gui, mine, delta)).render();
        new AdjustButton(gui, 2, 8, Material.LIME_DYE, 10, "Add 10 blocks to Warn Distance", NamedTextColor.GREEN,
                delta -> adjustWarnDistance(gui, mine, delta)).render();

        gui.open(player);
    }

    private void adjustWarnDistance(Gui gui, BasicMine mine, int delta) {
        int newWarnDistance = Math.max(0, mine.getWarnDistance() + delta);
        mine.setWarnDistance(newWarnDistance);
        renderWarnDistanceDisplay(gui, mine);
        gui.update();
    }

    private void renderWarnDistanceDisplay(Gui gui, BasicMine mine) {
        gui.setItem(2, 5,
                ItemBuilder.from(Material.COMPASS)
                        .name(Component.text("Warn Distance")
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(Component.text(mine.getWarnDistance() + " Blocks")
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem());
    }

    // ---------------------------------------------------------------------
    // Warn seconds GUI
    // ---------------------------------------------------------------------

    public void openWarnSecondsGUI(Player player, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Warn Seconds"))
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            saveAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> openWarnSettingsGUI(player, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        List<Integer> warnSec = mine.getWarnSeconds();

        int col = 2;
        for (int seconds : WARN_SECOND_OPTIONS) {
            new ToggleButton(gui, 2, col, seconds + " Warn Seconds",
                    () -> warnSec.contains(seconds),
                    enabled -> {
                        if (enabled) {
                            if (!warnSec.contains(seconds)) warnSec.add(seconds);
                        } else {
                            warnSec.remove(Integer.valueOf(seconds));
                        }
                    },
                    null
            ).render();
            col++;
        }

        gui.open(player);
    }

    // ---------------------------------------------------------------------
    // Blocks GUI
    // ---------------------------------------------------------------------

    public void openBlocksGUI(Player player, BasicMine mine) {
        PaginatedGui gui = Gui.paginated()
                .rows(6)
                .title(Component.text("Edit Blocks"))
                .disableAllInteractions()
                .create();

        // Go back to mine GUI, but only on a genuine player-initiated close
        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            Bukkit.getScheduler().runTask(plugin, () -> openMineGUI(player, mine.getName()));
        });

        gui.setDefaultClickAction(event -> {
            // Shift Right click removes the block from the mine
            if (event.getClick().isShiftClick() && event.isRightClick()
                    && event.getClickedInventory() != null
                    && event.getClickedInventory().equals(gui.getInventory())) {
                ItemStack currentItem = event.getCurrentItem();
                if (currentItem != null
                        && currentItem.getType() != Material.AIR) {
                    mine.removeBlock(ItemUtils.getIDFromItemStack(currentItem));
                    Bukkit.getScheduler().runTask(plugin, () -> openBlocksGUI(player, mine));
                    saveAsync(mine);
                }
            }
        });

        gui.setPlayerInventoryAction(event -> {
                    if (event.getCurrentItem() == null) return;
                    if (ItemUtils.isBlock(event.getCurrentItem())){
                        openEditBlockGUI(player,
                                ItemUtils.getIDFromItemStack(event.getCurrentItem()), mine);
                    }
                }
        );

        GuiUtils.fillRow(gui, 6, Material.WHITE_STAINED_GLASS_PANE);

        gui.setItem(6, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        gui.setItem(6, 3,
                ItemBuilder.from(Material.ARROW).name(Component.text("Previous")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> gui.previous()));

        gui.setItem(6, 7,
                ItemBuilder.from(Material.ARROW).name(Component.text("Next")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> gui.next()));

        for (Map.Entry<String, Double> material : mine.getMaterials()) {
            ItemStack itemNow = ItemUtils.getItemStackFromName(material.getKey());
            gui.addItem(
                    ItemBuilder.from(itemNow)
                            .name(itemNow.displayName()
                                    .color(NamedTextColor.YELLOW)
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                            .lore(Component.text("Block Chances: " + material.getValue() * 100 + "%")
                                            .color(NamedTextColor.YELLOW)
                                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                                    Component.text("Shift Right Click To Remove")
                                            .color(NamedTextColor.RED)
                                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                            .asGuiItem(event -> {
                                if (event.getClick().isRightClick()) return;
                                openEditBlockGUI(player, material.getKey(), mine);
                            })
            );
        }

        gui.open(player);
    }

    // ---------------------------------------------------------------------
    // Edit block chances GUI
    // ---------------------------------------------------------------------

    public void openEditBlockGUI(Player player, String block, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .disableAllInteractions()
                .title(Component.text("Edit Block Chances"))
                .create();

        // Go back to blocks GUI, but only on a genuine player-initiated close
        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            saveAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> openBlocksGUI(player, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        renderBlockDisplay(gui, block, mine);
        renderAllBlocksLore(gui, mine);

        // Remove buttons
        new AdjustButton(gui, 2, 2, Material.RED_DYE, 10, "Remove 10%", NamedTextColor.RED,
                delta -> adjustBlockChance(gui, block, mine, -delta / 100.0)).render();
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, "Remove 5%", NamedTextColor.RED,
                delta -> adjustBlockChance(gui, block, mine, -delta / 100.0)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, "Remove 1%", NamedTextColor.RED,
                delta -> adjustBlockChance(gui, block, mine, -delta / 100.0)).render();

        // Add buttons
        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, "Add 1%", NamedTextColor.GREEN,
                delta -> adjustBlockChance(gui, block, mine, delta / 100.0)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, "Add 5%", NamedTextColor.GREEN,
                delta -> adjustBlockChance(gui, block, mine, delta / 100.0)).render();
        new AdjustButton(gui, 2, 8, Material.LIME_DYE, 10, "Add 10%", NamedTextColor.GREEN,
                delta -> adjustBlockChance(gui, block, mine, delta / 100.0)).render();

        gui.open(player);
    }

    /** Adds (or subtracts, if negative) `delta` fraction to a block's chance, clamped sanely. */
    private void adjustBlockChance(Gui gui, String block, BasicMine mine, double delta) {
        double oldPercent = mine.getPercentage(block);
        double newPercent;

        if (delta < 0) {
            newPercent = Math.max(0, oldPercent + delta);
        } else {
            double totalWithoutCurrent = mine.getTotalPercentage() - oldPercent;
            newPercent = oldPercent + delta;
            if (totalWithoutCurrent + newPercent > 1.0) {
                newPercent = 1.0 - totalWithoutCurrent;
            }
            newPercent = Math.max(0, Math.min(1, newPercent));
        }

        mine.setPercentage(block, newPercent);
        renderBlockDisplay(gui, block, mine);
        renderAllBlocksLore(gui, mine);
        gui.update();
    }

    private void renderBlockDisplay(Gui gui, String block, BasicMine mine) {
        double percent = mine.getPercentage(block);
        gui.setItem(2, 5,
                ItemBuilder.from(ItemUtils.getItemStackFromName(block))
                        .name(Component.text("Block Percent: " + Math.round(percent * 100) + "%")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem());
    }

    private void renderAllBlocksLore(Gui gui, BasicMine mine) {
        List<Component> lore = new ArrayList<>();
        for (Map.Entry<String, Double> materials : mine.getMaterials()) {
            lore.add(Component.text("   " + materials.getKey() +
                            ": " + Math.round(materials.getValue() * 100) + "%")
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }

        gui.setItem(1, 5,
                ItemBuilder.from(Material.WRITABLE_BOOK)
                        .name(Component.text("All Blocks: ")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.AQUA))
                        .lore(lore)
                        .asGuiItem());
    }

    // ---------------------------------------------------------------------
    // Reset time GUI
    // ---------------------------------------------------------------------

    public void openChangeResetTimeGUI(Player player, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Change Reset Time"))
                .disableAllInteractions()
                .create();

        // Go back to reset settings hub, but only on a genuine player-initiated close
        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            saveAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> openResetSettingsGUI(player, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        renderResetTimeDisplay(gui, mine);

        // Remove buttons
        new AdjustButton(gui, 2, 2, Material.RED_DYE, 10, "Remove 10 seconds from Reset Time", NamedTextColor.RED,
                delta -> adjustResetTime(gui, mine, -delta)).render();
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, "Remove 5 seconds from Reset Time", NamedTextColor.RED,
                delta -> adjustResetTime(gui, mine, -delta)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, "Remove 1 second from Reset Time", NamedTextColor.RED,
                delta -> adjustResetTime(gui, mine, -delta)).render();

        // Add buttons
        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, "Add 1 second to Reset Time", NamedTextColor.GREEN,
                delta -> adjustResetTime(gui, mine, delta)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, "Add 5 seconds to Reset Time", NamedTextColor.GREEN,
                delta -> adjustResetTime(gui, mine, delta)).render();
        new AdjustButton(gui, 2, 8, Material.LIME_DYE, 10, "Add 10 seconds to Reset Time", NamedTextColor.GREEN,
                delta -> adjustResetTime(gui, mine, delta)).render();

        gui.open(player);
    }

    private void adjustResetTime(Gui gui, BasicMine mine, int delta) {
        int newResetTime = Math.max(1, mine.getResetTime() + delta);
        mine.setResetTime(newResetTime);
        renderResetTimeDisplay(gui, mine);
        gui.update();
    }

    private void renderResetTimeDisplay(Gui gui, BasicMine mine) {
        gui.setItem(2, 5,
                ItemBuilder.from(Material.CLOCK)
                        .name(Component.text("Reset Time")
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(Component.text(mine.getResetTime() + "s")
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem());
    }

    // ---------------------------------------------------------------------
    // Reset-at-percentage GUI
    // ---------------------------------------------------------------------

    public void openChangeResetPercentageGUI(Player player, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Reset At Percentage"))
                .disableAllInteractions()
                .create();

        // Go back to reset settings hub, but only on a genuine player-initiated close
        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            saveAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> openResetSettingsGUI(player, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        renderResetPercentageToggle(gui, mine);
        renderResetPercentageDisplay(gui, mine);

        // Remove buttons
        new AdjustButton(gui, 2, 2, Material.RED_DYE, 10, "Remove 10% from Reset Threshold", NamedTextColor.RED,
                delta -> adjustResetPercentage(gui, mine, -delta)).render();
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, "Remove 5% from Reset Threshold", NamedTextColor.RED,
                delta -> adjustResetPercentage(gui, mine, -delta)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, "Remove 1% from Reset Threshold", NamedTextColor.RED,
                delta -> adjustResetPercentage(gui, mine, -delta)).render();

        // Add buttons
        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, "Add 1% to Reset Threshold", NamedTextColor.GREEN,
                delta -> adjustResetPercentage(gui, mine, delta)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, "Add 5% to Reset Threshold", NamedTextColor.GREEN,
                delta -> adjustResetPercentage(gui, mine, delta)).render();
        new AdjustButton(gui, 2, 8, Material.LIME_DYE, 10, "Add 10% to Reset Threshold", NamedTextColor.GREEN,
                delta -> adjustResetPercentage(gui, mine, delta)).render();

        gui.open(player);
    }

    private void adjustResetPercentage(Gui gui, BasicMine mine, double delta) {
        double newValue = mine.getResetAtPercentage() + delta;
        newValue = Math.max(0, Math.min(100, newValue));
        mine.setResetAtPercentage(newValue);
        renderResetPercentageDisplay(gui, mine);
        gui.update();
    }

    private void renderResetPercentageDisplay(Gui gui, BasicMine mine) {
        gui.setItem(2, 5,
                ItemBuilder.from(Material.COMPARATOR)
                        .name(Component.text("Reset Threshold")
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(Component.text(mine.getResetAtPercentage() + "% left")
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem());
    }

    private void renderResetPercentageToggle(Gui gui, BasicMine mine) {
        boolean enabled = mine.isResetAtPercentageEnabled();
        gui.setItem(1, 5,
                ItemBuilder.from(Material.REPEATER)
                        .name(Component.text("Reset At Percentage: ")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE)
                                .append(Component.text(enabled ? "Enabled" : "Disabled")
                                        .color(enabled ? NamedTextColor.GREEN : NamedTextColor.RED)))
                        .lore(Component.text("Click to toggle")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.GRAY))
                        .asGuiItem(event -> {
                            mine.setResetAtPercentageEnabled(!mine.isResetAtPercentageEnabled());
                            renderResetPercentageToggle(gui, mine);
                            gui.update();
                        }));
    }

    // ---------------------------------------------------------------------
    // Min efficiency GUI
    // ---------------------------------------------------------------------

    public void openChangeMinEfficiencyGUI(Player player, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Min Efficiency"))
                .disableAllInteractions()
                .create();

        // Go back to mine GUI, but only on a genuine player-initiated close
        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            saveAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> openMineGUI(player, mine.getName()));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        renderMinEfficiencyToggle(gui, mine);
        renderMinEfficiencyDisplay(gui, mine);

        // Remove buttons
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, "Remove 5 Levels", NamedTextColor.RED,
                delta -> adjustMinEfficiency(gui, mine, -delta)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, "Remove 1 Level", NamedTextColor.RED,
                delta -> adjustMinEfficiency(gui, mine, -delta)).render();

        // Add buttons
        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, "Add 1 Level", NamedTextColor.GREEN,
                delta -> adjustMinEfficiency(gui, mine, delta)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, "Add 5 Levels", NamedTextColor.GREEN,
                delta -> adjustMinEfficiency(gui, mine, delta)).render();

        gui.open(player);
    }

    private void adjustMinEfficiency(Gui gui, BasicMine mine, int delta) {
        int newValue = Math.max(0, mine.getMinEfficiency() + delta);
        mine.setMinEfficiency(newValue);
        renderMinEfficiencyDisplay(gui, mine);
        gui.update();
    }

    private void renderMinEfficiencyDisplay(Gui gui, BasicMine mine) {
        gui.setItem(2, 5,
                ItemBuilder.from(Material.GOLDEN_PICKAXE)
                        .name(Component.text("Required Efficiency Level")
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(Component.text("Level " + mine.getMinEfficiency())
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem());
    }

    private void renderMinEfficiencyToggle(Gui gui, BasicMine mine) {
        boolean enabled = mine.isMinEfficiencyEnabled();
        gui.setItem(1, 5,
                ItemBuilder.from(Material.ENCHANTED_BOOK)
                        .name(Component.text("Min Efficiency: ")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE)
                                .append(Component.text(enabled ? "Enabled" : "Disabled")
                                        .color(enabled ? NamedTextColor.GREEN : NamedTextColor.RED)))
                        .lore(Component.text("Click to toggle")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.GRAY))
                        .asGuiItem(event -> {
                            mine.setMinEfficiencyEnabled(!mine.isMinEfficiencyEnabled());
                            renderMinEfficiencyToggle(gui, mine);
                            gui.update();
                        }));
    }
}