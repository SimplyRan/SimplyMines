package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.buttons.ToggleButton;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.mine.impl.PermissionMineRequirement;
import me.simplyran.simplymines.utils.ChatInputManager;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class PermissionRequirementGUI {

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.requirements.permission-requirement.title", "Permission Requirement");
    private final ConfigData<String> setNodeName = ConfigFactory.newConfigData(
            "menus.requirements.permission-requirement.set-node-name", "<yellow>Set Permission Node");
    private final ConfigData<String> setNodeLore = ConfigFactory.newConfigData(
            "menus.requirements.permission-requirement.set-node-lore", "<gray>Click, then type it in chat");
    private final ConfigData<String> currentNodeName = ConfigFactory.newConfigData(
            "menus.requirements.permission-requirement.current-node-name", "<white>Current Node");
    private final ConfigData<String> currentNodeLore = ConfigFactory.newConfigData(
            "menus.requirements.permission-requirement.current-node-lore", "<white><node>");
    private final ConfigData<String> toggleLabel = ConfigFactory.newConfigData(
            "menus.requirements.permission-requirement.toggle-label", "Permission Requirement");

    private final ConfigData<String> promptNode = ConfigFactory.newConfigData(
            "messages.permission-requirement-prompt-node", "<yellow>Type the permission node in chat, or 'cancel'.");
    private final ConfigData<String> nodeSet = ConfigFactory.newConfigData(
            "messages.permission-requirement-node-set", "<green>Permission node set to <node>");

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;
    private final ConfigManager configManager;

    public PermissionRequirementGUI(ConfigManager configManager, SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;
        this.configManager = configManager;

        configManager.registerLang(title);
        configManager.registerLang(setNodeName);
        configManager.registerLang(setNodeLore);
        configManager.registerLang(currentNodeName);
        configManager.registerLang(currentNodeLore);
        configManager.registerLang(toggleLabel);
        configManager.registerLang(promptNode);
        configManager.registerLang(nodeSet);
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
                .title(MessageUtils.format(title))
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW
                    || event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;
            mineManager.saveMineAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMineRequirementsGUI().open(player, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.BACK).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        new ToggleButton(gui, 1, 3, MessageUtils.plainFormat(toggleLabel), requirement::isEnabled, requirement::setEnabled, null).render();
        renderDisplay(gui, requirement);

        gui.setItem(2, 5,
                ItemBuilder.from(Material.WRITABLE_BOOK)
                        .name(MessageUtils.format(setNodeName).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(MessageUtils.format(setNodeLore).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> {
                            event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                            player.sendMessage(MessageUtils.format(player, promptNode));

                            ChatInputManager.awaitInput(player, input -> {
                                if (!input.equalsIgnoreCase("cancel")) {
                                    requirement.setPermission(input.trim());
                                    player.sendMessage(MessageUtils.format(player, nodeSet, "node", input.trim()));
                                }
                                Bukkit.getScheduler().runTask(plugin, () -> open(player, mine));
                            });
                        }));

        gui.open(player);
    }

    private void renderDisplay(Gui gui, PermissionMineRequirement req) {
        String node = req.getPermission().isEmpty() ? MessageUtils.plainFormat(MenuCommonText.NOT_SET) : req.getPermission();
        gui.setItem(1, 5,
                ItemBuilder.from(Material.PAPER)
                        .name(MessageUtils.format(currentNodeName).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(MessageUtils.format(currentNodeLore, "node", node)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem());
    }

}
