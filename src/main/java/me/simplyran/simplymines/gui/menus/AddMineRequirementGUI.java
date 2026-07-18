package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.requirements.mine.impl.EfficiencyMineRequirement;
import me.simplyran.simplymines.requirements.mine.impl.PermissionMineRequirement;
import me.simplyran.simplymines.utils.GuiUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AddMineRequirementGUI {

    private final SimplyMines plugin;
    private final GuiManager guiManager;

    public AddMineRequirementGUI(SimplyMines plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    public void open(Player player, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Add Mine Requirement"))
                .disableAllInteractions()
                .create();

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                        .asGuiItem(event -> guiManager.getMineRequirementsGUI().open(player, mine)));

        boolean hasEfficiency = mine.getMineRequirement(EfficiencyMineRequirement.class) != null;
        boolean hasPermission = mine.getMineRequirement(PermissionMineRequirement.class) != null;

        gui.setItem(2, 3, hasEfficiency
                ? unavailable("Min Efficiency (Already Added)")
                : ItemBuilder.from(Material.GOLDEN_PICKAXE)
                .name(Component.text("Min Efficiency").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW))
                .lore(Component.text("Requires a minimum tool efficiency level").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY))
                .asGuiItem(event -> {
                    EfficiencyMineRequirement req = new EfficiencyMineRequirement(0);
                    req.setEnabled(true);
                    mine.addMineRequirement(req);
                    Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMineRequirementsGUI().open(player, mine));
                }));

        gui.setItem(2, 5, hasPermission
                ? unavailable("Permission (Already Added)")
                : ItemBuilder.from(Material.WRITABLE_BOOK)
                .name(Component.text("Permission").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW))
                .lore(Component.text("Requires a permission node").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY))
                .asGuiItem(event -> {
                    PermissionMineRequirement req = new PermissionMineRequirement("");
                    req.setEnabled(false); // no node set yet, don't lock anyone out
                    mine.addMineRequirement(req);
                    Bukkit.getScheduler().runTask(plugin, () -> guiManager.getPermissionRequirementGUI().open(player, mine));
                }));

        gui.open(player);
    }

    private GuiItem unavailable(String name) {
        return ItemBuilder.from(Material.BARRIER)
                .name(Component.text(name).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.RED))
                .asGuiItem();
    }
}