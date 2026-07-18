package me.simplyran.simplymines.commands.subcommands;

import it.unimi.dsi.fastutil.Pair;
import lombok.AllArgsConstructor;
import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.managers.SelectionManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.requirements.mine.impl.EfficiencyMineRequirement;
import me.simplyran.simplymines.requirements.mine.impl.PermissionMineRequirement;
import me.simplyran.simplymines.requirements.reset.impl.PercentResetRequirement;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class CreateSubCommand implements SubCommand {

    private final MineManager mineManager;
    private final ConfigManager configManager;
    private final SelectionManager selectionManager;
    private final GuiManager guiManager;
    private final WorkloadRunnable workloadRunnable;


    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getPermission() {
        return "simplymines.create";
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
            sender.sendMessage(configManager.getMessage("mine-already-exists", "%mine%", mineName));
            return;
        }
        Pair<Location, Location> corners = selectionManager.getCorners(player.getUniqueId());
        if (corners == null || corners.first() == null || corners.second() == null) {
            sender.sendMessage(configManager.getMessage("no-selection"));
            return;
        }
        BasicMine basicMine = new BasicMine(
                true,
                mineName,
                corners.first(),
                corners.second(),
                Map.of(),
                workloadRunnable,
                List.of(),
                false,
                false,
                false,
                1,
                false
        );

        basicMine.addResetRequirement(new TimeResetRequirement(basicMine, 30));

        PercentResetRequirement percentReq = new PercentResetRequirement(basicMine, 10.0);
        percentReq.setEnabled(false);
        basicMine.addResetRequirement(percentReq);

        EfficiencyMineRequirement efficiencyReq = new EfficiencyMineRequirement(0);
        efficiencyReq.setEnabled(false);
        basicMine.addMineRequirement(efficiencyReq);

        PermissionMineRequirement permissionMineRequirement = new PermissionMineRequirement("simplymines.mine." + mineName);
        permissionMineRequirement.setEnabled(false);
        basicMine.addMineRequirement(permissionMineRequirement);

        mineManager.addMine(basicMine);
        guiManager.getMineEditorGUI().open(player, mineName);

    }
}
