package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.mine.impl.AttributeMineRequirement;
import me.simplyran.simplymines.utils.AttributeUtils;
import me.simplyran.simplymines.utils.ChatInputManager;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Map;

/**
 * Picks what to require: a vanilla attribute from the list, a custom key typed in chat,
 * or every custom value found on the tool in the player's hand.
 */
public class AddAttributeGUI extends Menu {

    private static final String PATH = "menus.requirements.add-attribute.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Add Attribute");
    private final ConfigData<String> attributeName = lang(PATH + "attribute-name", "<#ffd166><attribute>");
    private final ConfigData<String> attributeLore = lang(PATH + "attribute-lore", "<#8b9bb4>Click to require this attribute");
    private final ConfigData<String> customName = lang(PATH + "custom-name", "<#ffd166>Custom Attribute");
    private final ConfigData<String> customLore = lang(PATH + "custom-lore", "<#8b9bb4>Type a key from another plugin, e.g. myplugin:speed");
    private final ConfigData<String> detectName = lang(PATH + "detect-name", "<#ffd166>Detect From Held Tool");
    private final ConfigData<String> detectLore = lang(PATH + "detect-lore", "<#8b9bb4>Adds every custom value on the tool in your hand");

    private final ConfigData<String> promptKey = lang("messages.attribute-requirement-prompt-key",
            "<#ffd166>Type the attribute key as namespace:key (e.g. myplugin:speed), or 'cancel'.");
    private final ConfigData<String> invalidKey = lang("messages.attribute-requirement-invalid-key",
            "<#ef6f6c>That is not a valid key. Use the form namespace:key with lowercase letters, numbers, _ - . /");
    private final ConfigData<String> keyAdded = lang("messages.attribute-requirement-key-added",
            "<#7bd88f>Added <attribute>. Set its minimum in the next menu.");
    private final ConfigData<String> detectNone = lang("messages.attribute-requirement-detect-none",
            "<#ef6f6c>Hold a tool that has custom attribute values first.");
    private final ConfigData<String> detected = lang("messages.attribute-requirement-detected",
            "<#7bd88f>Added <count> attribute(s) from your held tool.");

    public AddAttributeGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine) {
        Runnable back = () -> services.guiManager().getMinAttributesGUI().open(player, mine);

        AttributeMineRequirement requirement = mine.getMineRequirement(AttributeMineRequirement.class);
        if (requirement == null) {
            back.run();
            return;
        }

        PaginatedGui gui = createPaginated(6, MessageUtils.format(title), back, null);

        gui.setItem(6, 4, Btn.of(Material.NAME_TAG, customName)
                .lore(customLore)
                .onClick(event -> promptCustomKey(player, mine, requirement))
                .build());
        gui.setItem(6, 6, Btn.of(Material.SPYGLASS, detectName)
                .lore(detectLore)
                .onClick(event -> detectFromHeldTool(player, mine, requirement))
                .build());

        for (Attribute attribute : AttributeUtils.allVanilla()) {
            String key = attribute.getKey().asString();
            if (requirement.hasAttribute(key)) continue;

            gui.addItem(Btn.of(Material.BLAZE_POWDER, attributeName, "attribute", AttributeUtils.displayName(key))
                    .lore(attributeLore)
                    .onClick(event -> {
                        requirement.setMinimum(key, 1);
                        reopenLater(() -> services.guiManager().getEditAttributeGUI().open(player, mine, key));
                    })
                    .build());
        }

        gui.open(player);
    }

    private void promptCustomKey(Player player, BasicMine mine, AttributeMineRequirement requirement) {
        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
        player.sendMessage(MessageUtils.format(player, promptKey));

        ChatInputManager.awaitInput(player, input -> {
            if (input.equalsIgnoreCase("cancel")) {
                reopenLater(() -> open(player, mine));
                return;
            }

            NamespacedKey key = AttributeUtils.parseKey(input);
            if (key == null) {
                player.sendMessage(MessageUtils.format(player, invalidKey));
                reopenLater(() -> open(player, mine));
                return;
            }

            String id = key.asString();
            if (!requirement.hasAttribute(id)) requirement.setMinimum(id, 1);
            player.sendMessage(MessageUtils.format(player, keyAdded, "attribute", AttributeUtils.displayName(id)));
            reopenLater(() -> services.guiManager().getEditAttributeGUI().open(player, mine, id));
        });
    }

    private void detectFromHeldTool(Player player, BasicMine mine, AttributeMineRequirement requirement) {
        Map<String, Double> found = AttributeUtils.customValues(player.getInventory().getItemInMainHand());

        int added = 0;
        for (Map.Entry<String, Double> entry : found.entrySet()) {
            if (requirement.hasAttribute(entry.getKey())) continue;
            requirement.setMinimum(entry.getKey(), entry.getValue());
            added++;
        }

        if (added == 0) {
            player.sendMessage(MessageUtils.format(player, detectNone));
            return;
        }

        player.sendMessage(MessageUtils.format(player, detected, "count", String.valueOf(added)));
        reopenLater(() -> services.guiManager().getMinAttributesGUI().open(player, mine));
    }
}
