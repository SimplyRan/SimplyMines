package me.simplyran.simplymines.gui;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.buttons.AdjustButton;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.ChatInputManager;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

/** Base for menus: registers lang entries on declaration and wires Back/close navigation once. */
public abstract class Menu {

    protected final MenuServices services;

    protected Menu(MenuServices services) {
        this.services = services;
    }

    protected ConfigData<String> lang(String path, String defaultValue) {
        ConfigData<String> data = ConfigFactory.newConfigData(path, defaultValue);
        services.configManager().registerLang(data);
        return data;
    }

    /**
     * Bordered menu with a Back arrow in the bottom-left corner.
     * {@code onClose} runs on Back and on a manual close; {@code onBack} then opens the parent.
     */
    protected Gui createGui(int rows, Component title, Runnable onBack, Runnable onClose) {
        Gui gui = Gui.gui()
                .rows(rows)
                .title(title)
                .disableAllInteractions()
                .create();

        GuiUtils.fillBorder(gui, Material.GRAY_STAINED_GLASS_PANE);
        wireNavigation(gui, rows, onBack, onClose);
        return gui;
    }

    /**
     * Paginated menu whose bottom row holds Back (col 1), Previous (col 3) and Next (col 7);
     * everything above it is the page. A null {@code onBack} makes it a root menu with no Back button.
     */
    protected PaginatedGui createPaginated(int rows, Component title, Runnable onBack, Runnable onClose) {
        PaginatedGui gui = Gui.paginated()
                .rows(rows)
                .title(title)
                .pageSize((rows - 1) * 9)
                .disableAllInteractions()
                .create();

        GuiUtils.fillRow(gui, rows, Material.GRAY_STAINED_GLASS_PANE);
        wireNavigation(gui, rows, onBack, onClose);

        gui.setItem(rows, 3, Btn.of(Material.ARROW, MenuCommonText.PREVIOUS)
                .onClick(event -> gui.previous())
                .build());
        gui.setItem(rows, 7, Btn.of(Material.ARROW, MenuCommonText.NEXT)
                .onClick(event -> gui.next())
                .build());
        return gui;
    }

    /**
     * One row of +/- buttons around the centre column: removers left (largest step outermost),
     * adders right. {@code middle} fills the centre slot and may be null.
     */
    protected void adjusters(Gui gui, int row, int[] steps, ConfigData<String> removeLabel, ConfigData<String> addLabel,
                             IntConsumer onDelta, GuiItem middle) {
        double[] wide = Arrays.stream(steps).asDoubleStream().toArray();
        adjustRow(gui, row, wide, removeLabel, addLabel,
                delta -> onDelta.accept((int) delta), middle);
    }

    /** Same layout for the small steps. */
    protected void fineAdjusters(Gui gui, int row, double[] steps, ConfigData<String> removeLabel, ConfigData<String> addLabel,
                                 DoubleConsumer onDelta, GuiItem middle) {
        adjustRow(gui, row, steps, removeLabel, addLabel, onDelta, middle);
    }

    private void adjustRow(Gui gui, int row, double[] steps,
                           ConfigData<String> removeLabel, ConfigData<String> addLabel,
                           DoubleConsumer onDelta, GuiItem middle) {
        for (int i = 0; i < steps.length; i++) {
            new AdjustButton(gui, row, 4 - i, Heads.minus(), steps[i], removeLabel, Palette.DANGER.color(),
                    amount -> onDelta.accept(-amount)).render();
            new AdjustButton(gui, row, 6 + i, Heads.plus(), steps[i], addLabel, Palette.SUCCESS.color(),
                    onDelta).render();
        }
        if (middle != null) gui.setItem(row, 5, middle);
    }

    /** Centre-slot button: prompts for a number in chat, applies it via {@code setter}, then reopens the menu. */
    protected GuiItem typeValueButton(Player player, Runnable reopen, DoubleConsumer setter) {
        return Btn.of(Material.NAME_TAG, MenuCommonText.TYPE_VALUE_NAME)
                .lore(MenuCommonText.TYPE_VALUE_LORE)
                .onClick(event -> {
                    player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                    player.sendMessage(MessageUtils.format(player, MenuCommonText.TYPE_VALUE_PROMPT));

                    ChatInputManager.awaitInput(player, input -> {
                        Double value = parseNumber(input);
                        if (value == null && !input.equalsIgnoreCase("cancel")) {
                            player.sendMessage(MessageUtils.format(player, MenuCommonText.TYPE_VALUE_INVALID));
                        }
                        reopenLater(() -> {
                            if (value != null) setter.accept(value);
                            reopen.run();
                        });
                    });
                })
                .build();
    }

    protected GuiItem fineTuneLabel() {
        return Btn.of(Material.AMETHYST_SHARD, MenuCommonText.FINE_TUNE_NAME)
                .lore(MenuCommonText.FINE_TUNE_LORE)
                .build();
    }

    private static Double parseNumber(String input) {
        try {
            double value = Double.parseDouble(input.trim().replace(',', '.').replace("%", ""));
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** List entry: left click edits, shift-right click removes; both hints are appended to the lore. */
    protected GuiItem editableItem(Btn btn, List<Component> details, Runnable onEdit, Runnable onRemove) {
        return btn.lore(details)
                .lore(Component.empty())
                .lore(MenuCommonText.LEFT_CLICK_EDIT)
                .lore(MenuCommonText.SHIFT_RIGHT_CLICK_REMOVE)
                .onClick(event -> {
                    ClickType click = event.getClick();
                    if (click == ClickType.SHIFT_RIGHT) onRemove.run();
                    else if (!click.isRightClick()) onEdit.run();
                })
                .build();
    }

    protected void reopenLater(Runnable action) {
        Bukkit.getScheduler().runTask(services.plugin(), action);
    }

    private void wireNavigation(BaseGui gui, int rows, Runnable onBack, Runnable onClose) {
        if (onBack == null) return;

        // OPEN_NEW is us opening another menu; PLUGIN is a deliberate close (teleport, chat prompt) that must not bounce back.
        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW
                    || event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;
            if (onClose != null) onClose.run();
            reopenLater(onBack);
        });

        gui.setItem(rows, 1, Btn.of(Material.ARROW, MenuCommonText.BACK)
                .color(NamedTextColor.WHITE)
                .onClick(event -> {
                    if (onClose != null) onClose.run();
                    onBack.run();
                })
                .build());
    }
}
