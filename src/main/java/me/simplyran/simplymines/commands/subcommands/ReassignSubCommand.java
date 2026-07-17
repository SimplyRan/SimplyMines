package me.simplyran.simplymines.commands.subcommands;

import it.unimi.dsi.fastutil.Pair;
import lombok.AllArgsConstructor;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.managers.SelectionManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.BoxedRegion;
import me.simplyran.simplymines.utils.JsonUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

@AllArgsConstructor
public class ReassignSubCommand implements SubCommand {

    private final MineManager mineManager;
    private final ConfigManager configManager;
    private final SelectionManager selectionManager;
    private final SimplyMines plugin;



    @Override
    public String getName() {
        return "reassign";
    }

    @Override
    public String getPermission() {
        return "simplymines.move";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public List<String> tabcomplete() {
        return mineManager.getMinesNames();
    }


    @Override
    public void preform(@NotNull CommandSender sender, @NonNull @NotNull String[] args, String mainCommandName) {

        if (args.length < 2) {
            sender.sendMessage(configManager.getMessage("missing-mine-name", "%sub%", getName(), "%label%", mainCommandName));
            return;
        }

        Player player = (Player) sender;
        String mineName = args[1];

        BasicMine mine = mineManager.getMine(mineName);
        if (mine == null) {
            sender.sendMessage(configManager.getMessage("mine-not-found", "%mine%", mineName));
            return;
        }
        Pair<Location, Location> corners = selectionManager.getCorners(player.getUniqueId());
        if (corners == null || corners.first() == null || corners.second() == null) {
            sender.sendMessage(configManager.getMessage("no-selection"));
            return;
        }
        mine.setRegion(new BoxedRegion(corners.first().getWorld(), corners.first(), corners.second()));
        sender.sendMessage(configManager.getMessage("mine-moved", "%mine%", mineName));
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> JsonUtils.saveMine(plugin, mine));
    }
}
