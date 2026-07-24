package me.simplyran.simplymines.commands.subcommands;

import it.unimi.dsi.fastutil.Pair;
import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.managers.SelectionManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.BoxedRegion;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ReassignSubCommand implements SubCommand {

    private final MineManager mineManager;
    private final SelectionManager selectionManager;

    private final ConfigData<String> missingMineName = ConfigFactory.newConfigData(
            "messages.missing-mine-name", "<red>You need to specify a mine name!");
    private final ConfigData<String> mineNotFound = ConfigFactory.newConfigData(
            "messages.mine-not-found", "<red>Mine <mine> not found!");
    private final ConfigData<String> noSelection = ConfigFactory.newConfigData(
            "messages.no-selection", "<red>No Selection found! Use a Wooden Hoe to select 2 corners.");
    private final ConfigData<String> mineMoved = ConfigFactory.newConfigData(
            "messages.mine-moved", "<green>Mine <mine> has been moved.");

    public ReassignSubCommand(@NotNull MineManager mineManager,
                              @NotNull ConfigManager configManager,
                              @NotNull SelectionManager selectionManager) {
        this.mineManager = mineManager;
        this.selectionManager = selectionManager;
        configManager.register(missingMineName);
        configManager.register(mineNotFound);
        configManager.register(noSelection);
        configManager.register(mineMoved);
    }

    @Override
    public String getName() {
        return "reassign";
    }

    @Override
    public String getPermission() {
        return "simplymines.move";
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

        Player player = (Player) sender;
        String mineName = args[1];

        BasicMine mine = mineManager.getMine(mineName);
        if (mine == null) {
            sender.sendMessage(MessageUtils.format(sender,
                    mineNotFound, "mine", mineName));
            return;
        }
        Pair<Location, Location> corners = selectionManager.getCorners(player.getUniqueId());
        if (corners == null || corners.first() == null || corners.second() == null) {
            sender.sendMessage(MessageUtils.format(sender, noSelection));
            return;
        }
        mine.setRegion(new BoxedRegion(corners.first().getWorld(), corners.first(), corners.second()));
        sender.sendMessage(MessageUtils.format(sender, mineMoved,
                "mine", mineName));
        mineManager.saveMineAsync(mine);
    }
}
