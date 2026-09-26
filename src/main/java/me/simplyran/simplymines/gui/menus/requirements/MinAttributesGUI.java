package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.gui.Numbers;
import me.simplyran.simplymines.gui.buttons.ToggleButton;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.mine.impl.AttributeMineRequirement;
import me.simplyran.simplymines.utils.AttributeUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class MinAttributesGUI extends Menu {

    private static final String PATH = "menus.requirements.min-attributes.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Min Attributes");
    private final ConfigData<String> toggleLabel = lang(PATH + "toggle-label", "Min Attributes");
    private final ConfigData<String> addAttribute = lang(PATH + "add-attribute", "<#7bd88f>Add Attribute");
    private final ConfigData<String> attributeName = lang(PATH + "attribute-name", "<#ffd166><attribute>");
    private final ConfigData<String> minimumLore = lang(PATH + "minimum-lore", "<#8b9bb4>Required Minimum: <white><value>");
    private final ConfigData<String> keyLore = lang(PATH + "key-lore", "<#8b9bb4>Key: <white><id>");

    public MinAttributesGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine) {
        AttributeMineRequirement req = getOrCreate(mine);

        PaginatedGui gui = createPaginated(3, MessageUtils.format(title),
                () -> services.guiManager().getMineRequirementsGUI().open(player, mine),
                () -> services.mineManager().saveMineAsync(mine));

        new ToggleButton(gui, 3, 5, MessageUtils.format(toggleLabel), req::isEnabled, req::setEnabled, null).render();

        gui.setItem(3, 9, Btn.of(Material.EMERALD, addAttribute)
                .onClick(event -> services.guiManager().getAddAttributeGUI().open(player, mine))
                .build());

        for (Map.Entry<String, Double> entry : req.getAttributes().entrySet()) {
            String key = entry.getKey();

            gui.addItem(editableItem(
                    Btn.of(AttributeUtils.vanilla(key) != null ? Material.BLAZE_POWDER : Material.NETHER_STAR,
                            attributeName, "attribute", AttributeUtils.displayName(key)),
                    List.<Component>of(
                            MessageUtils.format(minimumLore, "value", Numbers.plain(entry.getValue())),
                            MessageUtils.format(keyLore, "id", key)),
                    () -> services.guiManager().getEditAttributeGUI().open(player, mine, key),
                    () -> {
                        req.removeAttribute(key);
                        reopenLater(() -> open(player, mine));
                    }));
        }

        gui.open(player);
    }

    private AttributeMineRequirement getOrCreate(BasicMine mine) {
        AttributeMineRequirement req = mine.getMineRequirement(AttributeMineRequirement.class);
        if (req == null) {
            req = new AttributeMineRequirement(services.configManager());
            req.setEnabled(false);
            mine.addMineRequirement(req);
        }
        return req;
    }
}
