package me.simplyran.simplymines.commands.subcommands;

import lombok.AllArgsConstructor;
import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.SelectionManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

@AllArgsConstructor
public class ToolSubCommand implements SubCommand {

    private final SelectionManager selectionManager;
    private final ConfigManager configManager;


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
            sender.sendMessage(configManager.getMessage("enabled-tool"));
        }
        else {
            sender.sendMessage(configManager.getMessage("disabled-tool"));
        }
    }
}
