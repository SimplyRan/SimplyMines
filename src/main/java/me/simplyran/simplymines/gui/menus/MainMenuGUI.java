package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainMenuGUI {

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public MainMenuGUI(SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;
    }

    public void open(Player player) {
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

        mineManager.getMines().forEach(mine -> mainGUI.addItem(buildMineItem(player, mine)));

        mainGUI.open(player);
    }

    private GuiItem buildMineItem(Player player, BasicMine mine) {
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

        return ItemBuilder.from(ItemUtils.getItemStackFromName(mine.getMainMaterial()))
                .name(Component.text(mineName)
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .lore(lore)
                .asGuiItem(event -> guiManager.getMineEditorGUI().open(player, mineName));
    }
}