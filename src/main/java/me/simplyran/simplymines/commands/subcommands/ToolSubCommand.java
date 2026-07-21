package me.simplyran.simplymines.commands.subcommands;

import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.SelectionManager;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ToolSubCommand implements SubCommand {

    private final SelectionManager selectionManager;

    private final ConfigData<String> enabledTool = ConfigFactory.newConfigData(
            "messages.enabled-tool", "<green>Enabled selection tool.");
    private final ConfigData<String> disabledTool = ConfigFactory.newConfigData(
            "messages.disabled-tool", "<red>Disabled selection tool.");

    public ToolSubCommand(@NotNull SelectionManager selectionManager, @NotNull ConfigManager configManager) {
        this.selectionManager = selectionManager;
        configManager.register(enabledTool);
        configManager.register(disabledTool);
    }

    @Override
    public String getName() {
        return "tool";
    }

    @Override
    public String getPermission() {
        return "simplymines.tool";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }
    @Override
    public List<String> tabcomplete() {
        return List.of();
    }


    @Override
    public void preform(@NotNull CommandSender sender, @NonNull @NotNull String[] args, String mainCommandName) {
        Player player = (Player) sender;

        boolean isDisabled = selectionManager.isToolDisabled(player.getUniqueId());
        selectionManager.toggleTool(player.getUniqueId());
        if (isDisabled){
            sender.sendMessage(MessageUtils.format(sender, enabledTool));
        }
        else {
            sender.sendMessage(MessageUtils.format(sender, disabledTool));
        }
    }
}
