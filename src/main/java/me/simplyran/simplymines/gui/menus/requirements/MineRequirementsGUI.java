package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.mine.IMineRequirement;
import me.simplyran.simplymines.requirements.mine.impl.AttributeMineRequirement;
import me.simplyran.simplymines.requirements.mine.impl.EfficiencyMineRequirement;
import me.simplyran.simplymines.requirements.mine.impl.EnchantmentMineRequirement;
import me.simplyran.simplymines.requirements.mine.impl.PermissionMineRequirement;
import me.simplyran.simplymines.gui.Numbers;
import me.simplyran.simplymines.utils.AttributeUtils;
import me.simplyran.simplymines.utils.EnchantmentUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Hub listing every IMineRequirement attached to a mine (things a player
 * must satisfy to mine here, e.g. tool efficiency or permission).
 */
public class MineRequirementsGUI extends Menu {

    private static final String PATH = "menus.requirements.mine-requirements.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Mine Requirements: <#ffd166><mine>");
    private final ConfigData<String> addRequirement = lang(PATH + "add-requirement", "<#7bd88f>Add Requirement");
    private final ConfigData<String> efficiencyEnabled = lang(PATH + "efficiency-enabled", "<white>Min Efficiency: <#7bd88f>Enabled");
    private final ConfigData<String> efficiencyDisabled = lang(PATH + "efficiency-disabled", "<white>Min Efficiency: <#ef6f6c>Disabled");
    private final ConfigData<String> efficiencyLore = lang(PATH + "efficiency-lore", "<#8b9bb4>Requires Level <white><level>");
    private final ConfigData<String> enchantmentsEnabled = lang(PATH + "enchantments-enabled", "<white>Min Enchantments: <#7bd88f>Enabled");
    private final ConfigData<String> enchantmentsDisabled = lang(PATH + "enchantments-disabled", "<white>Min Enchantments: <#ef6f6c>Disabled");
    private final ConfigData<String> enchantmentLine = lang(PATH + "enchantment-line", "<#8b9bb4>- <white><enchantment>");
    private final ConfigData<String> attributesEnabled = lang(PATH + "attributes-enabled", "<white>Min Attributes: <#7bd88f>Enabled");
    private final ConfigData<String> attributesDisabled = lang(PATH + "attributes-disabled", "<white>Min Attributes: <#ef6f6c>Disabled");
    private final ConfigData<String> attributeLine = lang(PATH + "attribute-line", "<#8b9bb4>- <white><attribute> <#8b9bb4>min <white><value>");
    private final ConfigData<String> permissionEnabled = lang(PATH + "permission-enabled", "<white>Permission: <#7bd88f>Enabled");
    private final ConfigData<String> permissionDisabled = lang(PATH + "permission-disabled", "<white>Permission: <#ef6f6c>Disabled");
    private final ConfigData<String> permissionLore = lang(PATH + "permission-lore", "<#8b9bb4>Node: <white><node>");
    private final ConfigData<String> unknownRequirement = lang(PATH + "unknown-requirement", "<white><type>");

    public MineRequirementsGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine) {
        PaginatedGui gui = createPaginated(2, MessageUtils.format(title, "mine", mine.getName()),
                () -> services.guiManager().getMineEditorGUI().open(player, mine.getName()),
                () -> services.mineManager().saveMineAsync(mine));

        gui.setItem(2, 9, Btn.of(Material.EMERALD, addRequirement)
                .onClick(event -> services.guiManager().getAddMineRequirementGUI().open(player, mine))
                .build());

        for (IMineRequirement requirement : mine.getMineRequirements()) {
            gui.addItem(buildItem(player, mine, requirement));
        }

        gui.open(player);
    }

    private GuiItem buildItem(Player player, BasicMine mine, IMineRequirement requirement) {
        if (requirement instanceof EfficiencyMineRequirement efficiency) {
            return editableItem(Btn.of(Material.GOLDEN_PICKAXE, efficiency.isEnabled() ? efficiencyEnabled : efficiencyDisabled),
                    List.of(MessageUtils.format(efficiencyLore, "level", String.valueOf(efficiency.getEfficiencyLevel()))),
                    () -> services.guiManager().getMinEfficiencyGUI().open(player, mine),
                    () -> remove(player, mine, efficiency));
        }

        if (requirement instanceof EnchantmentMineRequirement enchantments) {
            List<Component> lines = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : enchantments.getEnchantments().entrySet()) {
                lines.add(MessageUtils.format(enchantmentLine,
                        "enchantment", EnchantmentUtils.displayName(entry.getKey(), entry.getValue())));
            }
            return editableItem(Btn.of(Material.ENCHANTED_BOOK, enchantments.isEnabled() ? enchantmentsEnabled : enchantmentsDisabled),
                    lines,
                    () -> services.guiManager().getMinEnchantmentsGUI().open(player, mine),
                    () -> remove(player, mine, enchantments));
        }

        if (requirement instanceof AttributeMineRequirement attributes) {
            List<Component> lines = new ArrayList<>();
            for (Map.Entry<String, Double> entry : attributes.getAttributes().entrySet()) {
                lines.add(MessageUtils.format(attributeLine,
                        "attribute", AttributeUtils.displayName(entry.getKey()),
                        "value", Numbers.plain(entry.getValue())));
            }
            return editableItem(Btn.of(Material.BLAZE_POWDER, attributes.isEnabled() ? attributesEnabled : attributesDisabled),
                    lines,
                    () -> services.guiManager().getMinAttributesGUI().open(player, mine),
                    () -> remove(player, mine, attributes));
        }

        if (requirement instanceof PermissionMineRequirement permission) {
            String node = permission.getPermission();
            return editableItem(Btn.of(Material.WRITABLE_BOOK, permission.isEnabled() ? permissionEnabled : permissionDisabled),
                    List.of(MessageUtils.format(permissionLore, "node",
                            node.isEmpty() ? MessageUtils.plainFormat(MenuCommonText.NOT_SET) : node)),
                    () -> services.guiManager().getPermissionRequirementGUI().open(player, mine),
                    () -> remove(player, mine, permission));
        }

        return Btn.of(Material.PAPER, unknownRequirement, "type", requirement.getClass().getSimpleName()).build();
    }

    private void remove(Player player, BasicMine mine, IMineRequirement requirement) {
        mine.removeMineRequirement(requirement);
        reopenLater(() -> open(player, mine));
    }
}
