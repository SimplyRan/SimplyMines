package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.requirements.mine.impl.PermissionMineRequirement;
import me.simplyran.simplymines.utils.ChatInputManager;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.MineSaver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class PermissionRequirementGUI {

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;
    private final ConfigManager configManager;

    public PermissionRequirementGUI(ConfigManager configManager, SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;
        this.configManager = configManager;
    }

    public void open(Player player, BasicMine mine) {
        PermissionMineRequirement req = mine.getMineRequirement(PermissionMineRequirement.class);
        if (req == null) {
            req = new PermissionMineRequirement(configManager,"");
            req.setEnabled(false);
            mine.addMineRequirement(req);
        }
        final PermissionMineRequirement requirement = req;

        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Permission Requirement"))
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW
                    || event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;
            MineSaver.saveAsync(plugin, mineManager, mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMineRequirementsGUI().open(player, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        renderToggle(gui, requirement);
        renderDisplay(gui, requirement);

        gui.setItem(2, 5,
                ItemBuilder.from(Material.WRITABLE_BOOK)
                        .name(Component.text("Set Permission Node").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW))
                        .lore(Component.text("Click, then type it in chat").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY))
                        .asGuiItem(event -> {
                            event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                            player.sendMessage(Component.text("Type the permission node in chat, or 'cancel'.").color(NamedTextColor.YELLOW));

                            ChatInputManager.awaitInput(player, input -> {
                                if (!input.equalsIgnoreCase("cancel")) {
                                    requirement.setPermission(input.trim());
                                    player.sendMessage(Component.text("Permission node set to " + input.trim()).color(NamedTextColor.GREEN));
                                }
                                Bukkit.getScheduler().runTask(plugin, () -> open(player, mine));
                            });
                        }));

        gui.open(player);
    }

    private void renderDisplay(Gui gui, PermissionMineRequirement req) {
        gui.setItem(1, 5,
                ItemBuilder.from(Material.PAPER)
                        .name(Component.text("Current Node").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(Component.text(req.getPermission().isEmpty() ? "(not set)" : req.getPermission())
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem());
    }

    private void renderToggle(Gui gui, PermissionMineRequirement req) {
        boolean enabled = req.isEnabled();
        gui.setItem(1, 3,
                ItemBuilder.from(Material.LEVER)
                        .name(Component.text("Permission Requirement: ").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE)
                                .append(Component.text(enabled ? "Enabled" : "Disabled").color(enabled ? NamedTextColor.GREEN : NamedTextColor.RED)))
                        .lore(Component.text("Click to toggle").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY))
                        .asGuiItem(event -> {
                            req.setEnabled(!req.isEnabled());
                            renderToggle(gui, req);
                            gui.update();
                        }));
    }
}