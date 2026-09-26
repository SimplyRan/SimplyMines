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
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class DisableSubCommand implements SubCommand {

    private final MineManager mineManager;

    private final ConfigData<String> mineDisabled = ConfigFactory.newConfigData(
            "messages.mine-disabled", "<#ffd166>SimplyMines <dark_gray>» <#ef6f6c>Disabled <white><mine><#ef6f6c>.");

    public DisableSubCommand(@NotNull MineManager mineManager, @NotNull ConfigManager configManager) {
        this.mineManager = mineManager;
        configManager.registerLang(mineDisabled);
    }

    @Override
    public String getName() {
        return "disable";
    }

    @Override
    public String getPermission() {
        return "simplymines.disable";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
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

        String mineName = args[1];


        BasicMine mine = mineManager.getMine(mineName);
        if (mine == null) {
            sender.sendMessage(MessageUtils.format(sender, CommandText.MINE_NOT_FOUND, "mine", mineName));
        } else {
            mine.setEnabled(false);
            sender.sendMessage(MessageUtils.format(sender, mineDisabled, "mine", mineName));
        }

    }
}
