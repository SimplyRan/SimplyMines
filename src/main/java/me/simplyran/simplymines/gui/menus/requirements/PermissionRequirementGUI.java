package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.gui.buttons.ToggleButton;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.mine.impl.PermissionMineRequirement;
import me.simplyran.simplymines.utils.ChatInputManager;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class PermissionRequirementGUI extends Menu {

    private static final String PATH = "menus.requirements.permission-requirement.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Permission Requirement");
    private final ConfigData<String> setNodeName = lang(PATH + "set-node-name", "<#ffd166>Set Permission Node");
    private final ConfigData<String> setNodeLore = lang(PATH + "set-node-lore", "<#8b9bb4>Click, then type it in chat");
    private final ConfigData<String> currentNodeName = lang(PATH + "current-node-name", "<#ffd166>Current Node");
    private final ConfigData<String> currentNodeLore = lang(PATH + "current-node-lore", "<white><node>");
    private final ConfigData<String> toggleLabel = lang(PATH + "toggle-label", "Permission Requirement");

    private final ConfigData<String> promptNode = lang("messages.permission-requirement-prompt-node", "<yellow>Type the permission node in chat, or 'cancel'.");
    private final ConfigData<String> nodeSet = lang("messages.permission-requirement-node-set", "<green>Permission node set to <node>");

    public PermissionRequirementGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine) {
        PermissionMineRequirement req = mine.getMineRequirement(PermissionMineRequirement.class);
        if (req == null) {
            req = new PermissionMineRequirement(services.configManager(), "");
            req.setEnabled(false);
            mine.addMineRequirement(req);
        }
        final PermissionMineRequirement requirement = req;

        Gui gui = createGui(5, MessageUtils.format(title),
                () -> services.guiManager().getMineRequirementsGUI().open(player, mine),
                () -> services.mineManager().saveMineAsync(mine));

        String node = requirement.getPermission().isEmpty()
                ? MessageUtils.plainFormat(MenuCommonText.NOT_SET)
                : requirement.getPermission();
        gui.setItem(2, 5, Btn.of(Material.PAPER, currentNodeName)
                .lore(currentNodeLore, "node", node)
                .build());

        gui.setItem(3, 5, Btn.of(Material.WRITABLE_BOOK, setNodeName)
                .lore(setNodeLore)
                .onClick(event -> {
                    player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                    player.sendMessage(MessageUtils.format(player, promptNode));

                    ChatInputManager.awaitInput(player, input -> {
                        if (!input.equalsIgnoreCase("cancel")) {
                            requirement.setPermission(input.trim());
                            player.sendMessage(MessageUtils.format(player, nodeSet, "node", input.trim()));
                        }
                        reopenLater(() -> open(player, mine));
                    });
                })
                .build());

        new ToggleButton(gui, 4, 5, MessageUtils.format(toggleLabel), requirement::isEnabled, requirement::setEnabled, null).render();

        gui.open(player);
    }
}
