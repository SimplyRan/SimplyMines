package me.simplyran.simplymines.commands;

import it.unimi.dsi.fastutil.Pair;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.managers.SelectionManager;
import me.simplyran.simplymines.objects.IMine;
import me.simplyran.simplymines.objects.impl.BasicMine;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class MainCommand implements CommandExecutor {

    private final MineManager mineManager;
    private final GuiManager guiManager;
    private final WorkloadRunnable workloadRunnable;
    private final SelectionManager selectionManager;
    private final ConfigManager configManager;

    public MainCommand(@NotNull MineManager mineManager,
                       @NotNull GuiManager guiManager,
                       @NotNull WorkloadRunnable workloadRunnable,
                       @NotNull SelectionManager selectionManager,
                       @NotNull ConfigManager configManager) {
        this.mineManager = mineManager;
        this.guiManager = guiManager;
        this.workloadRunnable = workloadRunnable;
        this.selectionManager = selectionManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String[] args) {

        switch (args.length) {

            case 0: {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(configManager.getMessage("only-players"));
                    break;
                }
                if (!sender.hasPermission("simplymines.admin")) {
                    sender.sendMessage(configManager.getMessage("no-permission"));
                    break;
                }
                guiManager.openMainGUI(player);
                break;
            }

            case 1: {
                String arg1 = args[0];
                if (arg1.equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("simplymines.reload")) {
                        sender.sendMessage(configManager.getMessage("no-permission-reload"));
                        break;
                    }
                    workloadRunnable.resetWorkloadDeque();
                    mineManager.reloadMines();
                    configManager.reloadConfig();
                    sender.sendMessage(configManager.getMessage("reloaded"));
                } else {
                    sender.sendMessage(configManager.getMessage("unknown-subcommand", "%input%", arg1));
                }
                break;
            }

            case 2: {
                String arg1 = args[0];
                String mineName = args[1];

                if (arg1.equalsIgnoreCase("reset")) {
                    if (!sender.hasPermission("simplymines.reset")) {
                        sender.sendMessage(configManager.getMessage("no-permission-reset"));
                        break;
                    }
                    IMine mine = mineManager.getMine(mineName);
                    if (mine != null) {
                        mine.reset();
                        sender.sendMessage(configManager.getMessage("mine-reset", "%mine%", mineName));
                    } else {
                        sender.sendMessage(configManager.getMessage("mine-not-found", "%mine%", mineName));
                    }

                } else if (arg1.equalsIgnoreCase("create")) {
                    if (!sender.hasPermission("simplymines.create")) {
                        sender.sendMessage(configManager.getMessage("no-permission-create"));
                        break;
                    }
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(configManager.getMessage("only-players-create"));
                        break;
                    }
                    IMine mine = mineManager.getMine(mineName);
                    if (mine != null) {
                        sender.sendMessage(configManager.getMessage("mine-already-exists", "%mine%", mineName));
                        break;
                    }
                    Pair<Location, Location> corners = selectionManager.getCorners(player.getUniqueId());
                    if (corners == null || corners.first() == null || corners.second() == null) {
                        sender.sendMessage(configManager.getMessage("no-selection"));
                        break;
                    }
                    BasicMine basicMine =
                            new BasicMine(true,
                                    mineName,
                                    30,
                                    corners.first(),
                                    corners.second(),
                                    Map.of(),
                                    workloadRunnable,
                                    List.of(),
                                    false,
                                    false,
                                    false,
                                    1,
                                    false);
                    mineManager.addMine(basicMine);
                    guiManager.openMineGUI(player, mineName);

                } else if (arg1.equalsIgnoreCase("delete")) {
                    if (!sender.hasPermission("simplymines.delete")) {
                        sender.sendMessage(configManager.getMessage("no-permission-delete"));
                        break;
                    }
                    IMine mine = mineManager.getMine(mineName);
                    if (mine == null) {
                        sender.sendMessage(configManager.getMessage("mine-not-found", "%mine%", mineName));
                    } else {
                        mineManager.deleteMine(mineName);
                        sender.sendMessage(configManager.getMessage("mine-deleted", "%mine%", mineName));
                    }

                } else if (arg1.equalsIgnoreCase("disable")) {
                    if (!sender.hasPermission("simplymines.disable")) {
                        sender.sendMessage(configManager.getMessage("no-permission-disable"));
                        break;
                    }
                    IMine mine = mineManager.getMine(mineName);
                    if (mine == null) {
                        sender.sendMessage(configManager.getMessage("mine-not-found", "%mine%", mineName));
                    } else {
                        mine.setEnabled(false);
                        sender.sendMessage(configManager.getMessage("mine-disabled", "%mine%", mineName));
                    }

                } else if (arg1.equalsIgnoreCase("enable")) {
                    if (!sender.hasPermission("simplymines.enable")) {
                        sender.sendMessage(configManager.getMessage("no-permission-enable"));
                        break;
                    }
                    IMine mine = mineManager.getMine(mineName);
                    if (mine == null) {
                        sender.sendMessage(configManager.getMessage("mine-not-found", "%mine%", mineName));
                    } else {
                        mine.setEnabled(true);
                        sender.sendMessage(configManager.getMessage("mine-enabled", "%mine%", mineName));
                    }

                } else {
                    sender.sendMessage(configManager.getMessage("unknown-subcommand", "%input%", arg1));
                }
                break;
            }

            default: {
                sender.sendMessage(configManager.getMessage("usage", "%label%", label));
                break;
            }
        }

        return true;
    }

}