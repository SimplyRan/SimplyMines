package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.requirements.reset.impl.PercentResetRequirement;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
import me.simplyran.simplymines.utils.GuiUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AddResetRequirementGUI {

    private final SimplyMines plugin;
    private final GuiManager guiManager;

    public AddResetRequirementGUI(SimplyMines plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    public void open(Player player, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Add Reset Requirement"))
                .disableAllInteractions()
                .create();

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                        .asGuiItem(event -> guiManager.getResetRequirementsGUI().open(player, mine)));

        boolean hasTime = mine.getResetRequirement(TimeResetRequirement.class) != null;
        boolean hasPercent = mine.getResetRequirement(PercentResetRequirement.class) != null;

        gui.setItem(2, 3, hasTime
                ? unavailable(Material.BARRIER, "Time Reset (Already Added)")
                : ItemBuilder.from(Material.CLOCK)
                .name(Component.text("Time Reset").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW))
                .lore(Component.text("Resets on a fixed timer").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY))
                .asGuiItem(event -> {
                    mine.addResetRequirement(new TimeResetRequirement(mine, 30));
                    Bukkit.getScheduler().runTask(plugin, () -> guiManager.getResetRequirementsGUI().open(player, mine));
                }));

        gui.setItem(2, 5, hasPercent
                ? unavailable(Material.BARRIER, "Percent Reset (Already Added)")
                : ItemBuilder.from(Material.REPEATER)
                .name(Component.text("Percent Reset").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW))
                .lore(Component.text("Resets once enough of the mine is broken").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY))
                .asGuiItem(event -> {
                    PercentResetRequirement req = new PercentResetRequirement(mine, 10.0);
                    req.setEnabled(true);
                    mine.addResetRequirement(req);
                    Bukkit.getScheduler().runTask(plugin, () -> guiManager.getResetRequirementsGUI().open(player, mine));
                }));

        gui.open(player);
    }

    private dev.triumphteam.gui.guis.GuiItem unavailable(Material material, String name) {
        return ItemBuilder.from(material)
                .name(Component.text(name).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.RED))
                .asGuiItem();
    }
}