package me.simplyran.simplymines.managers;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.objects.impl.BasicMine;
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
        if (mine == null) {
            player.closeInventory();
            return;
        }

        // Go back to main GUI, but only on a genuine player-initiated close
        mineGUI.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> JsonUtils.saveMine(plugin, mine));
        });


        GuiUtils.fillBorder(mineGUI);

        // Enable/disable toggle button
        updateMineButton(mineGUI, 2, 2, mine);

        // Info item
        mineGUI.setItem(3, 5,
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

        //Setting buttons
        boolean warnGlobal = mine.isWarnGlobal();
        boolean warnNear = mine.isWarnNear();
        boolean teleportPlayers = mine.isTeleportPlayers();

        Material itemMaterial = warnGlobal ? Material.LIME_DYE : Material.RED_DYE;
        mineGUI.setItem(3, 6,
                ItemBuilder.from(itemMaterial)
                        .name(Component.text("Warn Global: " + warnGlobal)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .asGuiItem(event -> toggleWarnGlobal(mineGUI, mine))
        );

        itemMaterial = warnNear ? Material.LIME_DYE : Material.RED_DYE;
        mineGUI.setItem(3, 7,
                ItemBuilder.from(itemMaterial)
                        .name(Component.text("Warn Near: " + warnNear)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .asGuiItem(event -> toggleWarnNear(mineGUI, mine))
        );

        itemMaterial = teleportPlayers ? Material.LIME_DYE : Material.RED_DYE;
        mineGUI.setItem(4, 5,
                ItemBuilder.from(itemMaterial)
                        .name(Component.text("Teleport Players: " + teleportPlayers)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .asGuiItem(event -> toggleTeleportPlayers(mineGUI, mine))
        );

        mineGUI.setItem(4, 6,
                ItemBuilder.from(Material.REDSTONE_TORCH)
                        .name(Component.text("Warn Seconds Editor")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .asGuiItem(event -> openWarnSecondsGUI(player, mine))
        );




        mineGUI.open(player);
    }

    public void toggleWarnNear(Gui gui, BasicMine mine){
        boolean newWarnNear = !mine.isWarnNear();
        mine.setWarnNear(newWarnNear);

        Material itemMaterial = newWarnNear ? Material.LIME_DYE : Material.RED_DYE;
        gui.setItem(3, 7,
                ItemBuilder.from(itemMaterial)
                        .name(Component.text("Warn Near: " + newWarnNear)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .asGuiItem(event -> toggleWarnNear(gui, mine))
        );

        gui.update();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> JsonUtils.saveMine(plugin, mine));
    }

    public void toggleWarnGlobal(Gui gui, BasicMine mine){
        boolean newWarnGlobal = !mine.isWarnGlobal();
        mine.setWarnGlobal(newWarnGlobal);

        Material itemMaterial = newWarnGlobal ? Material.LIME_DYE : Material.RED_DYE;
        gui.setItem(3, 6,
                ItemBuilder.from(itemMaterial)
                        .name(Component.text("Warn Global: " + newWarnGlobal)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .asGuiItem(event -> toggleWarnGlobal(gui, mine))
        );

        gui.update();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> JsonUtils.saveMine(plugin, mine));
    }

    public void toggleTeleportPlayers(Gui gui, BasicMine mine){
        boolean teleportPlayers = !mine.isTeleportPlayers();
        mine.setTeleportPlayers(teleportPlayers);

        Material itemMaterial = teleportPlayers ? Material.LIME_DYE : Material.RED_DYE;
        gui.setItem(4, 5,
                ItemBuilder.from(itemMaterial)
                        .name(Component.text("Teleport Players: " + teleportPlayers)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.YELLOW))
                        .asGuiItem(event -> toggleTeleportPlayers(gui, mine))
        );

        gui.update();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> JsonUtils.saveMine(plugin, mine));
    }


    public void openWarnSecondsGUI(Player player, BasicMine mine){
        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Warn Seconds"))
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            Bukkit.getScheduler().runTask(plugin, () -> openMineGUI(player, mine.getName()));
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> JsonUtils.saveMine(plugin, mine));
        });

        GuiUtils.fillBorder(gui);

        List<Integer> warnSec = mine.getWarnSeconds();

        int[] secondsOptions = {1, 2, 5, 10, 15, 30, 60};
        int col = 2;

        for (int seconds : secondsOptions) {
            final int slotCol = col; // capture for the closure below
            gui.setItem(2, slotCol, createWarnToggleItem(gui, warnSec, seconds, slotCol));
            col++;
        }

        gui.open(player);
    }

    private GuiItem createWarnToggleItem(Gui gui, List<Integer> warnSec, int seconds, int col) {
        boolean enabled = warnSec.contains(seconds);
        Material warnToggle = enabled ? Material.LIME_DYE : Material.RED_DYE;

        return ItemBuilder.from(warnToggle)
                .name(buildWarnName(seconds, enabled))
                .amount(seconds)
                .asGuiItem(event -> {
                    if (warnSec.contains(seconds)) {
                        warnSec.remove(Integer.valueOf(seconds));
                    } else {
                        warnSec.add(seconds);
                    }

                    gui.setItem(2, col, createWarnToggleItem(gui, warnSec, seconds, col));
                    gui.update();
                });
    }
    private Component buildWarnName(int seconds, boolean enabled) {
        return Component.text(seconds + " Warn Seconds: " + enabled)
                .color(enabled ? NamedTextColor.GREEN : NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
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

        gui.setDefaultClickAction(event -> {
            //If right click remove the block from the mine
            if (event.getClick().isRightClick()
                    && event.getClickedInventory() != null
                    && event.getClickedInventory().equals(gui.getInventory())){
                ItemStack currentItem = event.getCurrentItem();
                if (currentItem != null && currentItem.getType() != Material.AIR){
                    mine.removeBlock(ItemUtils.getIDFromItemStack(currentItem));

                    //Open the BlockGUI Again without the block
                    Bukkit.getScheduler().runTask(plugin, () -> openBlocksGUI(player, mine));

                    //Save the new blocks
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> JsonUtils.saveMine(plugin, mine));
                }

            }
        });

        gui.setPlayerInventoryAction(event ->
                openEditBlockGUI(player,
                        ItemUtils.getIDFromItemStack(event.getCurrentItem()), mine));

        GuiUtils.fillRow(gui, 6, Material.WHITE_STAINED_GLASS_PANE);

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
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))

                            .asGuiItem(event -> {
                                if (event.getClick().isRightClick()) return;
                                openEditBlockGUI(player, material.getKey(), mine);
                            })
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
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> JsonUtils.saveMine(plugin, mine));

            Bukkit.getScheduler().runTask(plugin, () -> openBlocksGUI(player, mine));
        });

        GuiUtils.fillBorder(gui);

        double percent = mine.getPercentage(block);


        gui.setItem(2, 5,
                ItemBuilder.from(ItemUtils.getItemStackFromName(block))
                        .name(Component.text("Block Percent: " + Math.round(percent * 100))
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem()
        );

        List<Component> lore = new ArrayList<>();
        for (Map.Entry<String, Double> materials : mine.getMaterials()){
            lore.add(Component.text("   " + materials.getKey() +
                    ": " + Math.round(materials.getValue() * 100))
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            );
        }

        gui.setItem(1, 5,
                ItemBuilder.from(Material.WRITABLE_BOOK)
                        .name(Component.text("All Blocks: ")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.AQUA))
                        .lore(lore)
                        .asGuiItem()
        );


        gui.setItem(2, 2, ItemBuilder.from(Material.RED_DYE)
                .amount(10)
                .name(Component.text("Remove 10%")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.RED))
                .asGuiItem(event -> removeChanceToBlock(gui, 0.1, block, mine)));

        gui.setItem(2, 3, ItemBuilder.from(Material.RED_DYE)
                .amount(5)
                .name(Component.text("Remove 5%")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.RED))
                .asGuiItem(event -> removeChanceToBlock(gui, 0.05, block, mine)));

        gui.setItem(2, 4, ItemBuilder.from(Material.RED_DYE)
                .amount(1)
                .name(Component.text("Remove 1%")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.RED))
                .asGuiItem(event -> removeChanceToBlock(gui, 0.01, block, mine)));




        gui.setItem(2, 8, ItemBuilder.from(Material.LIME_DYE)
                .amount(10)
                .name(Component.text("Add 10%")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.GREEN))
                .asGuiItem(event -> addChanceToBlock(gui, 0.1, block, mine)));

        gui.setItem(2, 7, ItemBuilder.from(Material.LIME_DYE)
                .amount(5)
                .name(Component.text("Add 5%")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.GREEN))
                .asGuiItem(event -> addChanceToBlock(gui, 0.05, block, mine)));

        gui.setItem(2, 6, ItemBuilder.from(Material.LIME_DYE)
                .amount(1)
                .name(Component.text("Add 1%")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.GREEN))
                .asGuiItem(event -> addChanceToBlock(gui, 0.01, block, mine)));



        gui.open(player);
    }



    private void removeChanceToBlock(Gui gui, double timeToRemove, String block, BasicMine mine) {
        double oldPercent = mine.getPercentage(block);
        double newPercent = Math.max(0, oldPercent - timeToRemove);
        mine.setPercentage(block, newPercent);
        refreshBlockEditItems(gui, block, newPercent, mine);
    }

    private void addChanceToBlock(Gui gui, double timeToAdd, String block, BasicMine mine) {
        double oldPercent = mine.getPercentage(block);
        double totalWithoutCurrent = mine.getTotalPercentage() - oldPercent;
        double newPercent = oldPercent + timeToAdd;

        if (totalWithoutCurrent + newPercent > 1.0) {
            newPercent = 1.0 - totalWithoutCurrent;
        }
        newPercent = Math.max(0, Math.min(1, newPercent));

        mine.setPercentage(block, newPercent);
        refreshBlockEditItems(gui, block, newPercent, mine);
    }

    private void refreshBlockEditItems(Gui gui, String block, double newPercent, BasicMine mine) {
        gui.setItem(2, 5,
                ItemBuilder.from(ItemUtils.getItemStackFromName(block))
                        .name(Component.text("Block Percent: " + Math.round(newPercent * 100))
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem()
        );

        List<Component> lore = new ArrayList<>();
        for (Map.Entry<String, Double> materials : mine.getMaterials()) {
            lore.add(Component.text("   " + materials.getKey() +
                            ": " + Math.round(materials.getValue() * 100))
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            );
        }

        gui.setItem(1, 5,
                ItemBuilder.from(Material.WRITABLE_BOOK)
                        .name(Component.text("All Blocks: ")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.AQUA))
                        .lore(lore)
                        .asGuiItem()
        );

        gui.update();
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
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> JsonUtils.saveMine(plugin, mine));


            Bukkit.getScheduler().runTask(plugin, () -> openMineGUI(player, mine.getName()));
        });

        GuiUtils.fillBorder(gui);


        gui.setItem(2, 2, ItemBuilder.from(Material.RED_DYE)
                .amount(10)
                .name(Component.text("Remove 10 seconds from Reset Time")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.RED))
                .asGuiItem(event -> removeTimeButton(gui, mine, 10)));

        gui.setItem(2, 3, ItemBuilder.from(Material.RED_DYE)
                .amount(5)
                .name(Component.text("Remove 5 seconds from Reset Time")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.RED))
                .asGuiItem(event -> removeTimeButton(gui, mine, 5)));

        gui.setItem(2, 4, ItemBuilder.from(Material.RED_DYE)
                .amount(1)
                .name(Component.text("Remove 1 second from Reset Time")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.RED))
                .asGuiItem(event -> removeTimeButton(gui, mine, 1)));




        gui.setItem(2, 8, ItemBuilder.from(Material.LIME_DYE)
                .amount(10)
                .name(Component.text("Add 10 seconds to Reset Time")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.GREEN))
                .asGuiItem(event -> addTimeButton(gui, mine, 10)));

        gui.setItem(2, 7, ItemBuilder.from(Material.LIME_DYE)
                .amount(5)
                .name(Component.text("Add 5 seconds to Reset Time")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.GREEN))
                .asGuiItem(event -> addTimeButton(gui, mine, 5)));

        gui.setItem(2, 6, ItemBuilder.from(Material.LIME_DYE)
                .amount(1)
                .name(Component.text("Add 1 seconds to Reset Time")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.GREEN))
                .asGuiItem(event -> addTimeButton(gui, mine, 1)));


        gui.setItem(2, 5,
                ItemBuilder.from(Material.CLOCK)
                        .name(Component.text("Reset Time")
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(Component.text(mine.getResetTime()+ "s")
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
                ItemBuilder.from(Material.CLOCK)
                        .name(Component.text("Reset Time")
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(Component.text(mine.getResetTime()+ "s")
                                .color(NamedTextColor.WHITE)
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
                ItemBuilder.from(Material.CLOCK)
                        .name(Component.text("Reset Time")
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(Component.text(mine.getResetTime() + "s")
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem()
        );

        gui.update();
    }


}