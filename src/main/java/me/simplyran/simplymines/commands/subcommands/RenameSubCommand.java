package me.simplyran.simplymines.commands.subcommands;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.objects.ConfigFactory;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class RenameSubCommand implements SubCommand {

    private final MineManager mineManager;
    private final SimplyMines plugin;

    private final ConfigData<String> missingMineName = ConfigFactory.newConfigData(
            "messages.missing-mine-name", "<red>You need to specify a mine name!");
    private final ConfigData<String> mineNotFound = ConfigFactory.newConfigData(
            "messages.mine-not-found", "<red>Mine <mine> not found!");

    public RenameSubCommand(@NotNull MineManager mineManager, @NotNull ConfigManager configManager, @NotNull SimplyMines plugin) {
        this.mineManager = mineManager;
        this.plugin = plugin;
        configManager.register(missingMineName);
        configManager.register(mineNotFound);
    }

    @Override
    public String getName() {
        return "rename";
    }

    @Override
    public String getPermission() {
        return "simplymines.rename";
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
        if (args.length < 3){
            //TODO maybe add in config, You need to specify new mine name.
            sender.sendMessage(MessageUtils.format(sender, missingMineName, "sub", getName(), "label", mainCommandName));
            return;
        }

        String oldMineName = args[1];
        String newMineName = args[2];




        BasicMine mine = mineManager.getMine(oldMineName);
        if (mine == null) {
            sender.sendMessage(MessageUtils.format(sender, mineNotFound, "mine", oldMineName));
            return;
        }
        if (mineManager.getMine(newMineName) != null){
            //TODO maybe also add to config
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Mine %s already exists!"
                    .formatted(newMineName)));
            return;
        }
        mine.setName(newMineName, mineManager, plugin);
        //TODO maybe add to config.
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Renamed Mine from %s to %s!"
                .formatted(oldMineName, newMineName)));

    }
}
