package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.BoxedRegion;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.ItemUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.List;

/** Lists every mine the player may teleport to; clicking one teleports them. */
public class MineTeleportGUI extends Menu {

    public static final String PERMISSION = "simplymines.teleport";

    private static final String PATH = "menus.mine-teleport.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Mine Teleport");
    private final ConfigData<String> world = lang(PATH + "mine-item.world", "<#8b9bb4>World: <white><world>");
    private final ConfigData<String> location = lang(PATH + "mine-item.location", "<#8b9bb4>Location: <white><x>, <y>, <z>");
    private final ConfigData<String> locationCorner = lang(PATH + "mine-item.location-corner", "<#8b9bb4>Location: <white><x>, <y>, <z> <dark_gray>(above mine)");
    private final ConfigData<String> size = lang(PATH + "mine-item.size", "<#8b9bb4>Size: <white><blocks> blocks");
    private final ConfigData<String> click = lang(PATH + "mine-item.click", "<#7bd88f>Click to teleport");

    public MineTeleportGUI(MenuServices services) {
        super(services);
    }

    /** Mines this player has the per-mine teleport permission for. */
    public List<BasicMine> teleportableMines(Player player) {
        return services.mineManager().getMines().stream()
                .filter(mine -> player.hasPermission(PERMISSION + "." + mine.getName()))
                .toList();
    }

    /** Opens the menu; returns false (and opens nothing) when the player can teleport to no mine. */
    public boolean open(Player player) {
        List<BasicMine> mines = teleportableMines(player);
        if (mines.isEmpty()) return false;

        PaginatedGui gui = createPaginated(6, MessageUtils.format(title), null, null);
        mines.stream().map(mine -> buildMineItem(player, mine)).filter(java.util.Objects::nonNull).forEach(gui::addItem);
        gui.open(player);
        return true;
    }

    private GuiItem buildMineItem(Player player, BasicMine mine) {
        BoxedRegion region = mine.getRegion();
        Location target = mine.getTeleportTarget();
        if (target == null) return null;

        Btn btn = Btn.of(ItemUtils.getItemStackFromName(mine.getMainMaterial()),
                        Component.text(mine.getName(), TextColor.color(0xffd166)))
                .lore(world, "world", target.getWorld().getName())
                .lore(mine.getTeleportLocation() != null ? location : locationCorner,
                        "x", String.valueOf(target.getBlockX()),
                        "y", String.valueOf(target.getBlockY()),
                        "z", String.valueOf(target.getBlockZ()))
                .lore(size, "blocks", String.format("%,d", region.getBlockCount()))
                .lore(Component.empty())
                .lore(click);

        return btn.onClick(event -> {
            player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
            Location current = mine.getTeleportTarget();
            if (current == null || !player.hasPermission(PERMISSION + "." + mine.getName())) return;
            player.teleportAsync(current);
        }).build();
    }

}
