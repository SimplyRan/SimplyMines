package me.simplyran.simplymines.commands.subcommands;

import lombok.AllArgsConstructor;
import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

@AllArgsConstructor
public class EditSubCommand implements SubCommand {

    private final MineManager mineManager;
    private final ConfigManager configManager;
    private final GuiManager guiManager;


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
            sender.sendMessage(configManager.getMessage("missing-mine-name", "%sub%", getName(), "%label%", mainCommandName));
            return;
        }

        String mineName = args[1];
        Player player = (Player) sender;

        BasicMine mine = mineManager.getMine(mineName);
        if (mine != null) {
            guiManager.getMineEditorGUI().open(player, mineName);
        } else {
            sender.sendMessage(configManager.getMessage("mine-not-found", "%mine%", mineName));
        }

    }
}
