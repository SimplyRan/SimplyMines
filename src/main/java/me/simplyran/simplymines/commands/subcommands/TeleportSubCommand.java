package me.simplyran.simplymines.commands.subcommands;

import me.simplyran.simplymines.commands.CommandText;
import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class TeleportSubCommand implements SubCommand {

    private final MineManager mineManager;

    private final ConfigData<String> noPermissionTeleport = ConfigFactory.newConfigData(
            "messages.no-permission-teleport", "<#ffd166>SimplyMines <dark_gray>» <#ef6f6c>You do not have permission to teleport to mines.");
    private final ConfigData<String> noTeleportLocation = ConfigFactory.newConfigData(
            "messages.no-teleport-location", "<#ffd166>SimplyMines <dark_gray>» <#ef6f6c>Mine <white><mine> <#ef6f6c>has no teleport point set.");
    private final ConfigData<String> mineTeleported = ConfigFactory.newConfigData(
            "messages.mine-teleported", "<#ffd166>SimplyMines <dark_gray>» <#7bd88f>Teleported to <white><mine><#7bd88f>.");

    public TeleportSubCommand(@NotNull MineManager mineManager, @NotNull ConfigManager configManager) {
        this.mineManager = mineManager;
        configManager.registerLang(noPermissionTeleport);
        configManager.registerLang(noTeleportLocation);
        configManager.registerLang(mineTeleported);
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
        return mineManager.getMinesNames().stream().toList();
    }


    @Override
    public void preform(@NotNull CommandSender sender, @NonNull @NotNull String[] args, String mainCommandName) {

        if (args.length < 2) {
            CommandText.sendUsage(sender, mainCommandName, getName());
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
            sender.sendMessage(MessageUtils.format(sender, CommandText.MINE_NOT_FOUND, "mine", mineName));
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
