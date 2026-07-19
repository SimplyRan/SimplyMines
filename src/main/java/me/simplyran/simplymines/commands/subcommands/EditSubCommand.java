package me.simplyran.simplymines.commands.subcommands;

import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.objects.ConfigFactory;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class EditSubCommand implements SubCommand {

    private final MineManager mineManager;
    private final GuiManager guiManager;

    private final ConfigData<String> missingMineName = ConfigFactory.newConfigData(
            "messages.missing-mine-name", "<red>You need to specify a mine name!");
    private final ConfigData<String> mineNotFound = ConfigFactory.newConfigData(
            "messages.mine-not-found", "<red>Mine <mine> not found!");

    public EditSubCommand(@NotNull MineManager mineManager, @NotNull ConfigManager configManager, @NotNull GuiManager guiManager) {
        this.mineManager = mineManager;
        this.guiManager = guiManager;
        configManager.register(missingMineName);
        configManager.register(mineNotFound);
    }

    @Override
    public String getName() {
        return "edit";
    }

    @Override
    public String getPermission() {
        return "simplymines.admin";
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

        String mineName = args[1];
        Player player = (Player) sender;

        BasicMine mine = mineManager.getMine(mineName);
        if (mine != null) {
            guiManager.getMineEditorGUI().open(player, mineName);
        } else {
            sender.sendMessage(MessageUtils.format(sender, mineNotFound, "mine", mineName));
        }

    }
}
