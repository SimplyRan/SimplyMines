package me.simplyran.simplymines.commands.subcommands;

import me.simplyran.simplymines.commands.CommandText;
import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.utils.MessageUtils;
import me.simplyran.simplymines.utils.MineNameValidator;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class RenameSubCommand implements SubCommand {

    private final ConfigData<String> mineRenamed = ConfigFactory.newConfigData(
            "messages.mine-renamed", "<#ffd166>SimplyMines <dark_gray>» <#7bd88f>Renamed <white><old> <#7bd88f>to <white><new><#7bd88f>.");

    private final MineManager mineManager;


    public RenameSubCommand(@NotNull MineManager mineManager,
                            @NotNull ConfigManager configManager) {
        this.mineManager = mineManager;
        configManager.registerLang(mineRenamed);
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
        return mineManager.getMinesNames().stream().toList();
    }

    @Override
    public void preform(@NotNull CommandSender sender, @NonNull @NotNull String[] args, String mainCommandName) {

        if (args.length < 2) {
            CommandText.sendUsage(sender, mainCommandName, getName());
            return;
        }
        if (args.length < 3){
            //TODO maybe add in config, You need to specify new mine name.
            CommandText.sendUsage(sender, mainCommandName, getName());
            return;
        }

        String oldMineName = args[1];
        String newMineName = args[2];

        if (!MineNameValidator.isValid(newMineName)) {
        sender.sendMessage(MessageUtils.format(sender, CommandText.INVALID_MINE_NAME));
            return;
        }

        BasicMine mine = mineManager.getMine(oldMineName);
        if (mine == null) {
            sender.sendMessage(MessageUtils.format(sender, CommandText.MINE_NOT_FOUND, "mine", oldMineName));
            return;
        }
        if (mineManager.getMine(newMineName) != null){
            sender.sendMessage(MessageUtils.format(sender, CommandText.MINE_ALREADY_EXISTS, "mine", newMineName));
            return;
        }
        mine.setName(newMineName, mineManager);
        sender.sendMessage(MessageUtils.format(sender, mineRenamed, "old", oldMineName, "new", newMineName));

    }
}
