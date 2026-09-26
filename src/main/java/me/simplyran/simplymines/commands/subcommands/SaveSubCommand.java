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

public class SaveSubCommand implements SubCommand {

    private final ConfigData<String> mineSaved = ConfigFactory.newConfigData(
            "messages.mine-saved", "<#ffd166>SimplyMines <dark_gray>» <#7bd88f>Saved <white><mine> <#7bd88f>to disk.");

    private final MineManager mineManager;


    public SaveSubCommand(@NotNull MineManager mineManager,
                          @NotNull ConfigManager configManager) {
        this.mineManager = mineManager;
        configManager.registerLang(mineSaved);
    }

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public String getPermission() {
        return "simplymines.save";
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
            return;
        }

        mineManager.saveMineAsync(mine);
        sender.sendMessage(MessageUtils.format(sender, mineSaved, "mine", mineName));
    }
}

