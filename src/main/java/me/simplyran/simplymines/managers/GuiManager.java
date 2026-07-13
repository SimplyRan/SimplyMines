package me.simplyran.simplymines.managers;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.objects.impl.BasicMine;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class GuiManager {

    private final SimplyMines plugin;
    private final MineManager mineManager;

    public GuiManager(SimplyMines plugin, MineManager mineManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
    }

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

            GuiItem guiItem = ItemBuilder.from(ItemUtils.getItemStackFromName(mine.getMainMaterial()))
                    .name(Component.text(mineName)
                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                    .asGuiItem(event -> openMineGUI(player, mineName));

            mainGUI.addItem(guiItem);
        });

        mainGUI.open(player);
    }

    public void openMineGUI(Player player, String mineName) {
        Gui mineGUI = Gui.gui()
                .title(Component.text("Editing " + mineName))
                .rows(6)
                .disableAllInteractions()
                .create();

        BasicMine mine = mineManager.getMine(mineName);
        if (mine == null) return;

        GuiUtils.fillBorder(mineGUI);

        // Enable/disable toggle button
        updateMineButton(mineGUI, 2, 2, mine);

        // Info item
        mineGUI.setItem(3, 6,
                ItemBuilder.from(Material.WRITABLE_BOOK)
                        .name(Component.text(mineName + " Info")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE)
                        )
                        .lore(List.of(
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
                        ))
                        .asGuiItem()
        );

        mineGUI.setItem(4, 3,
                ItemBuilder.from(Material.REPEATER)
                        .name(Component.text("Edit Reset Time")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .lore(Component.text(mine.getResetTime())
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> openChangeResetTimeGUI(player, mine)));

        mineGUI.setItem(3, 3, ItemBuilder.from(
                        ItemUtils.getItemStackFromName(mine.getMainMaterial()))
                .name(Component.text("Edit Blocks")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .asGuiItem(event -> openBlocksGUI(player, mine))
        );

        mineGUI.open(player);
    }

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

        GuiUtils.fillRow(gui, 6, Material.WHITE_STAINED_GLASS_PANE);

        gui.setItem(6, 3,
                ItemBuilder.from(Material.ARROW).name(Component.text("Previous")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> gui.previous()));

        gui.setItem(6, 7,
                ItemBuilder.from(Material.ARROW).name(Component.text("Next")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> gui.next()));

        Map<String, Double> materials = mine.getMaterials();
        for (Map.Entry<String, Double> material : materials.entrySet()) {
            ItemStack itemNow = ItemUtils.getItemStackFromName(material.getKey());
            gui.addItem(
                    ItemBuilder.from(itemNow)
                            .asGuiItem(event -> openEditBlockGUI(player, material.getKey(), mine))
            );
        }

        gui.open(player);
    }

    public void openEditBlockGUI(Player player, String block, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .disableAllInteractions()
                .title(Component.text("Edit Block Chances"))
                .create();

        // Go back to blocks GUI, but only on a genuine player-initiated close
        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            Bukkit.getScheduler().runTask(plugin, () -> openBlocksGUI(player, mine));
        });

        GuiUtils.fillBorder(gui);

        double percent = mine.getMaterials().get(block);

        gui.setItem(2, 5,
                ItemBuilder.from(ItemUtils.getItemStackFromName(block))
                        .name(Component.text("Block Percent: " + percent * 100))
                        .asGuiItem()
        );

        gui.open(player);
    }

    public void openChangeResetTimeGUI(Player player, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Change Reset Time"))
                .disableAllInteractions()
                .create();

        // Go back to mine GUI, but only on a genuine player-initiated close
        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            Bukkit.getScheduler().runTask(plugin, () -> openMineGUI(player, mine.getName()));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(2, 1, ItemBuilder.from(Material.RED_CONCRETE)
                .amount(25)
                .name(Component.text("Remove 25 seconds from Reset Time")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.RED))
                .asGuiItem(event -> removeTimeButton(gui, mine, 25)));

        gui.setItem(2, 2, ItemBuilder.from(Material.RED_CONCRETE)
                .amount(20)
                .name(Component.text("Remove 20 seconds from Reset Time")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.RED))
                .asGuiItem(event -> removeTimeButton(gui, mine, 20)));

        gui.setItem(2, 3, ItemBuilder.from(Material.RED_CONCRETE)
                .amount(5)
                .name(Component.text("Remove 5 seconds from Reset Time")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.RED))
                .asGuiItem(event -> removeTimeButton(gui, mine, 5)));

        gui.setItem(2, 4, ItemBuilder.from(Material.RED_CONCRETE)
                .amount(1)
                .name(Component.text("Remove 1 second from Reset Time")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.RED))
                .asGuiItem(event -> removeTimeButton(gui, mine, 1)));

        gui.setItem(2, 9, ItemBuilder.from(Material.GREEN_CONCRETE)
                .amount(25)
                .name(Component.text("Add 25 seconds to Reset Time")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.GREEN))
                .asGuiItem(event -> addTimeButton(gui, mine, 25)));

        gui.setItem(2, 8, ItemBuilder.from(Material.GREEN_CONCRETE)
                .amount(20)
                .name(Component.text("Add 20 seconds to Reset Time")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.GREEN))
                .asGuiItem(event -> addTimeButton(gui, mine, 20)));

        gui.setItem(2, 7, ItemBuilder.from(Material.GREEN_CONCRETE)
                .amount(5)
                .name(Component.text("Add 5 seconds to Reset Time")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.GREEN))
                .asGuiItem(event -> addTimeButton(gui, mine, 5)));

        gui.setItem(2, 6, ItemBuilder.from(Material.GREEN_CONCRETE)
                .amount(1)
                .name(Component.text("Add 1 second to Reset Time")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.GREEN))
                .asGuiItem(event -> addTimeButton(gui, mine, 1)));

        gui.setItem(2, 5,
                ItemBuilder.from(Material.REDSTONE)
                        .name(Component.text("Reset Time")
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(Component.text(mine.getResetTime())
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem()
        );

        gui.open(player);
    }

    private void updateMineButton(Gui mineGUI, int row, int col, BasicMine mine) {
        Material buttonMaterial = mine.isEnabled() ? Material.LIME_DYE : Material.RED_DYE;

        mineGUI.setItem(row, col, ItemBuilder.from(buttonMaterial)
                .name(Component.text("Mine Enabled: " + mine.isEnabled())
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .asGuiItem(event -> {
                    mine.setEnabled(!mine.isEnabled());
                    updateMineButton(mineGUI, row, col, mine);
                    mineGUI.update();
                }));
    }

    private void addTimeButton(Gui gui, BasicMine mine, int time) {
        mine.setResetTime(mine.getResetTime() + time);
        gui.setItem(2, 5,
                ItemBuilder.from(Material.REDSTONE)
                        .name(Component.text("Reset Time")
                                .color(NamedTextColor.GREEN)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(Component.text(mine.getResetTime())
                                .color(NamedTextColor.GREEN)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem()
        );

        gui.update();
    }

    private void removeTimeButton(Gui gui, BasicMine mine, int time) {
        int oldResetTime = mine.getResetTime();
        int newResetTime = oldResetTime - time > 0 ? oldResetTime - time : 1;
        mine.setResetTime(newResetTime);
        gui.setItem(2, 5,
                ItemBuilder.from(Material.REDSTONE)
                        .name(Component.text("Reset Time")
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(Component.text(mine.getResetTime())
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem()
        );

        gui.update();
    }

}