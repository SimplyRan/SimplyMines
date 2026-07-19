package me.simplyran.simplymines.commands.subcommands;

import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.objects.ConfigFactory;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class TeleportSubCommand implements SubCommand {

    private final MineManager mineManager;

    private final ConfigData<String> missingMineName = ConfigFactory.newConfigData(
            "messages.missing-mine-name", "<red>You need to specify a mine name!");
    private final ConfigData<String> noPermissionTeleport = ConfigFactory.newConfigData(
            "messages.no-permission-teleport", "<red>You do not have permission to teleport to mines.");
    private final ConfigData<String> mineNotFound = ConfigFactory.newConfigData(
            "messages.mine-not-found", "<red>Mine <mine> not found!");
    private final ConfigData<String> noTeleportLocation = ConfigFactory.newConfigData(
            "messages.no-teleport-location", "<red>Mine <mine> does not have a teleport location set.");
    private final ConfigData<String> mineTeleported = ConfigFactory.newConfigData(
            "messages.mine-teleported", "<green>Teleported to <mine>.");

    public TeleportSubCommand(@NotNull MineManager mineManager, @NotNull ConfigManager configManager) {
        this.mineManager = mineManager;
        configManager.register(missingMineName);
        configManager.register(noPermissionTeleport);
        configManager.register(mineNotFound);
        configManager.register(noTeleportLocation);
        configManager.register(mineTeleported);
    }

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
            sender.sendMessage(MessageUtils.format(sender, missingMineName, "sub", getName(), "label", mainCommandName));
            return;
        }

        Player player = (Player) sender;
        String mineName = args[1];

        if (!player.hasPermission(getPermission()+ "." + mineName)) {
            sender.sendMessage(MessageUtils.format(sender, noPermissionTeleport));
            return;
        }


        BasicMine mine = mineManager.getMine(mineName);
        if (mine == null) {
            sender.sendMessage(MessageUtils.format(sender, mineNotFound, "mine", mineName));
            return;
        }
        Location teleportLocation = mine.getTeleportLocation();
        if (teleportLocation == null) {
            sender.sendMessage(MessageUtils.format(sender, noTeleportLocation, "mine", mineName));
            return;
        }
        player.teleport(teleportLocation);
        sender.sendMessage(MessageUtils.format(sender, mineTeleported, "mine", mineName));

    }
}
