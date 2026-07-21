package me.simplyran.simplymines.commands.subcommands;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.utils.MessageUtils;
import me.simplyran.simplymines.utils.MineSaver;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class SaveSubCommand implements SubCommand {

    private final MineManager mineManager;
    private final SimplyMines plugin;

    private final ConfigData<String> missingMineName = ConfigFactory.newConfigData(
            "messages.missing-mine-name", "<red>You need to specify a mine name!");
    private final ConfigData<String> mineNotFound = ConfigFactory.newConfigData(
            "messages.mine-not-found", "<red>Mine <mine> not found!");

    public SaveSubCommand(@NotNull MineManager mineManager,
                          @NotNull ConfigManager configManager, @NotNull SimplyMines plugin) {
        this.mineManager = mineManager;
        this.plugin = plugin;
        configManager.register(missingMineName);
        configManager.register(mineNotFound);
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
        return mineManager.getMinesNames();
    }


    @Override
    public void preform(@NotNull CommandSender sender, @NonNull @NotNull String[] args, String mainCommandName) {

        if (args.length < 2) {
            sender.sendMessage(MessageUtils.format(sender, missingMineName, "sub", getName(), "label", mainCommandName));
            return;
        }

        String mineName = args[1];


        BasicMine mine = mineManager.getMine(mineName);
        if (mine == null) {
            sender.sendMessage(MessageUtils.format(sender, mineNotFound, "mine", mineName));
            return;
        }

        MineSaver.saveAsync(plugin, mine);
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Saved to disk " + mineName + "!"));
    }
}

