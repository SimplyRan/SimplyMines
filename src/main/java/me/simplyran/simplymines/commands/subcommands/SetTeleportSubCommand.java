package me.simplyran.simplymines.commands.subcommands;

import me.simplyran.simplymines.commands.CommandText;
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

    private final ConfigData<String> teleportSet = ConfigFactory.newConfigData(
            "messages.teleport-set", "<#ffd166>SimplyMines <dark_gray>» <#7bd88f>Teleport point for <white><mine> <#7bd88f>set to your position.");

    public SetTeleportSubCommand(@NotNull MineManager mineManager, @NotNull ConfigManager configManager) {
        this.mineManager = mineManager;
        configManager.registerLang(teleportSet);
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


        BasicMine mine = mineManager.getMine(mineName);
        if (mine == null) {
            sender.sendMessage(MessageUtils.format(sender, CommandText.MINE_NOT_FOUND, "mine", mineName));
            return;
        }
        mine.setTeleportLocation(player.getLocation());
        sender.sendMessage(MessageUtils.format(sender, teleportSet, "mine", mineName));

    }
}
