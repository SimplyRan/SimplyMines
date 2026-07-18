package me.simplyran.simplymines.commands.subcommands;

import lombok.AllArgsConstructor;
import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

@AllArgsConstructor
public class TeleportSubCommand implements SubCommand {

    private final MineManager mineManager;
    private final ConfigManager configManager;

    @Override
    public String getName() {
        return "teleport";
    }

    @Override
    public String getPermission() {
        return "simplymines.teleport";
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

        if (!player.hasPermission(getPermission()+ "." + mineName)) {
            //TODO Add to config no perms to teleport to mine - maybe.
            sender.sendMessage(configManager.getMessage("no-permission"));
            return;
        }


        BasicMine mine = mineManager.getMine(mineName);
        if (mine == null) {
            sender.sendMessage(configManager.getMessage("mine-not-found", "%mine%", mineName));
            return;
        }
        Location teleportLocation = mine.getTeleportLocation();
        if (teleportLocation == null) {
            sender.sendMessage(configManager.getMessage("no-teleport-location", "%mine%", mineName));
            return;
        }
        player.teleport(teleportLocation);
        sender.sendMessage(configManager.getMessage("mine-teleported", "%mine%", mineName));

    }
}
