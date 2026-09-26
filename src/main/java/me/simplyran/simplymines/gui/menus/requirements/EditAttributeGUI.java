package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.gui.Numbers;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.mine.impl.AttributeMineRequirement;
import me.simplyran.simplymines.utils.AttributeUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Menu for adjusting the minimum value of a single attribute in a mine's attribute requirement.
 */
public class EditAttributeGUI extends Menu {

    private static final String PATH = "menus.requirements.edit-attribute.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Edit Attribute");
    private final ConfigData<String> adjustRemove = lang(PATH + "adjust-remove", "Remove <amount>");
    private final ConfigData<String> adjustAdd = lang(PATH + "adjust-add", "Add <amount>");
    private final ConfigData<String> displayName = lang(PATH + "display-name", "<#ffd166><attribute>: <white><value>");
    private final ConfigData<String> keyLore = lang(PATH + "key-lore", "<#8b9bb4>Key: <white><id>");
    private final ConfigData<String> heldLore = lang(PATH + "held-lore", "<#8b9bb4>Your held tool has: <white><value>");

    public EditAttributeGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine, String key) {
        Runnable back = () -> services.guiManager().getMinAttributesGUI().open(player, mine);

        AttributeMineRequirement req = mine.getMineRequirement(AttributeMineRequirement.class);
        if (req == null || !req.hasAttribute(key)) {
            back.run();
            return;
        }

        Gui gui = createGui(5, MessageUtils.format(title), back,
                () -> services.mineManager().saveMineAsync(mine));

        renderDisplay(gui, player, req, key);
        adjusters(gui, 3, new int[]{1, 5, 10}, adjustRemove, adjustAdd,
                delta -> adjust(gui, player, req, key, delta),
                typeValueButton(player, () -> open(player, mine, key),
                        value -> adjust(gui, player, req, key, value - req.getAttributes().get(key))));
        fineAdjusters(gui, 4, new double[]{0.1, 0.25, 0.5}, adjustRemove, adjustAdd,
                delta -> adjust(gui, player, req, key, delta), fineTuneLabel());

        gui.open(player);
    }

    private void adjust(Gui gui, Player player, AttributeMineRequirement req, String key, double delta) {
        req.setMinimum(key, req.getAttributes().get(key) + delta);
        renderDisplay(gui, player, req, key);
        gui.update();
    }

    private void renderDisplay(Gui gui, Player player, AttributeMineRequirement req, String key) {
        double held = AttributeUtils.valueOf(player.getInventory().getItemInMainHand(), key);
        boolean vanilla = AttributeUtils.vanilla(key) != null;

        gui.setItem(2, 5, Btn.of(vanilla ? Material.BLAZE_POWDER : Material.NETHER_STAR, displayName,
                        "attribute", AttributeUtils.displayName(key),
                        "value", Numbers.plain(req.getAttributes().get(key)))
                .lore(keyLore, "id", key)
                .lore(heldLore, "value", Numbers.plain(held))
                .build());
    }
}
