package me.simplyran.simplymines.commands.subcommands;

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

public class EnableSubCommand implements SubCommand {

    private final MineManager mineManager;

    private final ConfigData<String> missingMineName = ConfigFactory.newConfigData(
            "messages.missing-mine-name", "<red>You need to specify a mine name!");
    private final ConfigData<String> mineNotFound = ConfigFactory.newConfigData(
            "messages.mine-not-found", "<red>Mine <mine> not found!");
    private final ConfigData<String> mineEnabled = ConfigFactory.newConfigData(
            "messages.mine-enabled", "<green>Enabled <mine>.");

    public EnableSubCommand(@NotNull MineManager mineManager, @NotNull ConfigManager configManager) {
        this.mineManager = mineManager;
        configManager.register(missingMineName);
        configManager.register(mineNotFound);
        configManager.register(mineEnabled);
    }

    @Override
    public String getName() {
        return "enable";
    }

    @Override
    public String getPermission() {
        return "simplymines.enable";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public List<String> tabcomplete() {
        return mineManager.getMinesNames();
    }


    @Override
    public void preform(@NotNull CommandSender sender, @NonNull @NotNull String[] args, String mainCommandName) {
        if (args.length < 2) {
            sender.sendMessage(MessageUtils.format(sender, missingMineName,
                    "sub", getName(),
                    "label", mainCommandName));
            return;
        }

        String mineName = args[1];
        BasicMine mine = mineManager.getMine(mineName);
        if (mine == null) {
            sender.sendMessage(MessageUtils.format(sender,
                    mineNotFound, "mine", mineName));
        } else {
            mine.setEnabled(true);
            sender.sendMessage(MessageUtils.format(sender,
                    mineEnabled, "mine", mineName));
        }

    }
}
