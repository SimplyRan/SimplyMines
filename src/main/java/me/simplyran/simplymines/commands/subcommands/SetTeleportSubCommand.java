package me.simplyran.simplymines.commands.subcommands;

import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class SetTeleportSubCommand implements SubCommand {

    private final MineManager mineManager;

    private final ConfigData<String> missingMineName = ConfigFactory.newConfigData(
            "messages.missing-mine-name", "<red>You need to specify a mine name!");
    private final ConfigData<String> mineNotFound = ConfigFactory.newConfigData(
            "messages.mine-not-found", "<red>Mine <mine> not found!");
    private final ConfigData<String> teleportSet = ConfigFactory.newConfigData(
            "messages.teleport-set", "<green>Teleport location for <mine> has been set to your current position.");

    public SetTeleportSubCommand(@NotNull MineManager mineManager, @NotNull ConfigManager configManager) {
        this.mineManager = mineManager;
        configManager.register(missingMineName);
        configManager.register(mineNotFound);
        configManager.register(teleportSet);
    }

    @Override
    public String getName() {
        return "setteleport";
    }

    @Override
    public String getPermission() {
        return "simplymines.setteleport";
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


        BasicMine mine = mineManager.getMine(mineName);
        if (mine == null) {
            sender.sendMessage(MessageUtils.format(sender, mineNotFound, "mine", mineName));
            return;
        }
        mine.setTeleportLocation(player.getLocation());
        sender.sendMessage(MessageUtils.format(sender, teleportSet, "mine", mineName));

    }
}
