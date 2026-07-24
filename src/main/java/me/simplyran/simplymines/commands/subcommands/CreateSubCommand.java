package me.simplyran.simplymines.commands.subcommands;

import it.unimi.dsi.fastutil.Pair;
import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.factories.MineFactory;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.managers.SelectionManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.requirements.mine.impl.EfficiencyMineRequirement;
import me.simplyran.simplymines.requirements.mine.impl.PermissionMineRequirement;
import me.simplyran.simplymines.requirements.reset.impl.PercentResetRequirement;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
import me.simplyran.simplymines.utils.MessageUtils;
import me.simplyran.simplymines.utils.MineNameValidator;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class CreateSubCommand implements SubCommand {

    private final MineManager mineManager;
    private final ConfigManager configManager;
    private final SelectionManager selectionManager;
    private final GuiManager guiManager;
    private final WorkloadRunnable workloadRunnable;

    private final ConfigData<String> missingMineName = ConfigFactory.newConfigData(
            "messages.missing-mine-name", "<red>You need to specify a mine name!");
    private final ConfigData<String> mineAlreadyExists = ConfigFactory.newConfigData(
            "messages.mine-already-exists", "<red>Mine <mine> already exists.");
    private final ConfigData<String> noSelection = ConfigFactory.newConfigData(
            "messages.no-selection", "<red>No Selection found! Use a Wooden Hoe to select 2 corners.");

    public CreateSubCommand(@NotNull MineManager mineManager,
                            @NotNull ConfigManager configManager,
                            @NotNull SelectionManager selectionManager,
                            @NotNull GuiManager guiManager,
                            @NotNull WorkloadRunnable workloadRunnable) {
        this.mineManager = mineManager;
        this.configManager = configManager;
        this.selectionManager = selectionManager;
        this.guiManager = guiManager;
        this.workloadRunnable = workloadRunnable;
        configManager.register(missingMineName);
        configManager.register(mineAlreadyExists);
        configManager.register(noSelection);
    }

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
            sender.sendMessage(MessageUtils.format(sender, missingMineName, "sub", getName(), "label", mainCommandName));
            return;
        }

        String mineName = args[1];
        Player player = (Player) sender;

        if (!MineNameValidator.isValid(mineName)) {
            //TODO maybe add to config
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<red>Invalid mine name! Use only letters, numbers, - and _ (max 32 characters)."));
            return;
        }

        BasicMine mine = mineManager.getMine(mineName);
        if (mine != null) {
            sender.sendMessage(MessageUtils.format(sender, mineAlreadyExists, "mine", mineName));
            return;
        }
        Pair<Location, Location> corners = selectionManager.getCorners(player.getUniqueId());
        if (corners == null || corners.first() == null || corners.second() == null) {
            sender.sendMessage(MessageUtils.format(sender, noSelection));
            return;
        }
        BasicMine basicMine = MineFactory.createDefaultMin(mineName, corners, workloadRunnable);

        basicMine.addResetRequirement(new TimeResetRequirement(basicMine, 30));

        PercentResetRequirement percentReq = new PercentResetRequirement(basicMine, 10.0);
        percentReq.setEnabled(false);
        basicMine.addResetRequirement(percentReq);

        EfficiencyMineRequirement efficiencyReq = new EfficiencyMineRequirement(configManager,0);
        efficiencyReq.setEnabled(false);
        basicMine.addMineRequirement(efficiencyReq);

        PermissionMineRequirement permissionMineRequirement = new PermissionMineRequirement(configManager,"simplymines.mine." + mineName);
        permissionMineRequirement.setEnabled(false);
        basicMine.addMineRequirement(permissionMineRequirement);

        mineManager.addMine(basicMine);
        //Persist immediately - a crash before the editor GUI closes must not lose the mine.
        mineManager.saveMineAsync(basicMine);
        guiManager.getMineEditorGUI().open(player, mineName);

    }
}
