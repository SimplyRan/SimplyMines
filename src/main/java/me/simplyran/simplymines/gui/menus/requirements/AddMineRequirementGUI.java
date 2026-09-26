package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.mine.impl.AttributeMineRequirement;
import me.simplyran.simplymines.requirements.mine.impl.EfficiencyMineRequirement;
import me.simplyran.simplymines.requirements.mine.impl.EnchantmentMineRequirement;
import me.simplyran.simplymines.requirements.mine.impl.PermissionMineRequirement;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AddMineRequirementGUI extends Menu {

    private static final String PATH = "menus.requirements.add-mine-requirement.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Add Mine Requirement");
    private final ConfigData<String> minEfficiencyUnavailable = lang(PATH + "min-efficiency-unavailable", "<#ef6f6c>Min Efficiency (Already Added)");
    private final ConfigData<String> minEfficiencyName = lang(PATH + "min-efficiency-name", "<#ffd166>Min Efficiency");
    private final ConfigData<String> minEfficiencyLore = lang(PATH + "min-efficiency-lore", "<#8b9bb4>Requires a minimum tool efficiency level");
    private final ConfigData<String> minEnchantmentsUnavailable = lang(PATH + "min-enchantments-unavailable", "<#ef6f6c>Min Enchantments (Already Added)");
    private final ConfigData<String> minEnchantmentsName = lang(PATH + "min-enchantments-name", "<#ffd166>Min Enchantments");
    private final ConfigData<String> minEnchantmentsLore = lang(PATH + "min-enchantments-lore", "<#8b9bb4>Requires several enchantments at set levels");
    private final ConfigData<String> minAttributesUnavailable = lang(PATH + "min-attributes-unavailable", "<#ef6f6c>Min Attributes (Already Added)");
    private final ConfigData<String> minAttributesName = lang(PATH + "min-attributes-name", "<#ffd166>Min Attributes");
    private final ConfigData<String> minAttributesLore = lang(PATH + "min-attributes-lore", "<#8b9bb4>Requires tool attributes at set values");
    private final ConfigData<String> permissionUnavailable = lang(PATH + "permission-unavailable", "<#ef6f6c>Permission (Already Added)");
    private final ConfigData<String> permissionName = lang(PATH + "permission-name", "<#ffd166>Permission");
    private final ConfigData<String> permissionLore = lang(PATH + "permission-lore", "<#8b9bb4>Requires a permission node");

    public AddMineRequirementGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine) {
        Gui gui = createGui(3, MessageUtils.format(title),
                () -> services.guiManager().getMineRequirementsGUI().open(player, mine), null);

        boolean hasEfficiency = mine.getMineRequirement(EfficiencyMineRequirement.class) != null;
        boolean hasEnchantments = mine.getMineRequirement(EnchantmentMineRequirement.class) != null;
        boolean hasAttributes = mine.getMineRequirement(AttributeMineRequirement.class) != null;
        boolean hasPermission = mine.getMineRequirement(PermissionMineRequirement.class) != null;

        gui.setItem(2, 2, hasEfficiency
                ? unavailable(minEfficiencyUnavailable)
                : Btn.of(Material.GOLDEN_PICKAXE, minEfficiencyName)
                .lore(minEfficiencyLore)
                .onClick(event -> {
                    EfficiencyMineRequirement req = new EfficiencyMineRequirement(services.configManager(), 0);
                    req.setEnabled(true);
                    mine.addMineRequirement(req);
                    reopenLater(() -> services.guiManager().getMineRequirementsGUI().open(player, mine));
                })
                .build());

        gui.setItem(2, 4, hasEnchantments
                ? unavailable(minEnchantmentsUnavailable)
                : Btn.of(Material.ENCHANTED_BOOK, minEnchantmentsName)
                .lore(minEnchantmentsLore)
                .onClick(event -> {
                    EnchantmentMineRequirement req = new EnchantmentMineRequirement(services.configManager());
                    req.setEnabled(true);
                    mine.addMineRequirement(req);
                    reopenLater(() -> services.guiManager().getMinEnchantmentsGUI().open(player, mine));
                })
                .build());

        gui.setItem(2, 6, hasAttributes
                ? unavailable(minAttributesUnavailable)
                : Btn.of(Material.BLAZE_POWDER, minAttributesName)
                .lore(minAttributesLore)
                .onClick(event -> {
                    AttributeMineRequirement req = new AttributeMineRequirement(services.configManager());
                    req.setEnabled(true);
                    mine.addMineRequirement(req);
                    reopenLater(() -> services.guiManager().getMinAttributesGUI().open(player, mine));
                })
                .build());

        gui.setItem(2, 8, hasPermission
                ? unavailable(permissionUnavailable)
                : Btn.of(Material.WRITABLE_BOOK, permissionName)
                .lore(permissionLore)
                .onClick(event -> {
                    PermissionMineRequirement req = new PermissionMineRequirement(services.configManager(), "");
                    req.setEnabled(false);
                    mine.addMineRequirement(req);
                    reopenLater(() -> services.guiManager().getPermissionRequirementGUI().open(player, mine));
                })
                .build());

        gui.open(player);
    }

    private GuiItem unavailable(ConfigData<String> name) {
        return Btn.of(Material.BARRIER, name).build();
    }
}
