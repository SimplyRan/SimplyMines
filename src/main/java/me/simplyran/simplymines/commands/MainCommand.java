package me.simplyran.simplymines.commands;

import it.unimi.dsi.fastutil.Pair;
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

    public MainCommand(@NotNull MineManager mineManager,
                       @NotNull GuiManager guiManager,
                       @NotNull WorkloadRunnable workloadRunnable,
                       @NotNull SelectionManager selectionManager) {
        this.mineManager = mineManager;
        this.guiManager = guiManager;
        this.workloadRunnable = workloadRunnable;
        this.selectionManager = selectionManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String[] args) {

        switch (args.length) {

            case 0: {
                if (sender.hasPermission("simplymine.admin") &&
                        sender instanceof Player player) {
                    guiManager.openMainGUI(player);
                }
                break;
            }

            case 1: {
                String arg1 = args[0];
                if (sender.hasPermission("simplymine.reload")){
                    if (arg1.equalsIgnoreCase("reload")) {
                        workloadRunnable.resetWorkloadDeque();
                        mineManager.reloadMines();
                        //TODO Change to config
                        sender.sendMessage("Mines and Config have been reloaded!");
                    }
                }
                break;
            }

            case 2:{
                String arg1 = args[0];
                if (arg1.equalsIgnoreCase("reset") && sender.hasPermission("simplymines.reset")){
                    String mineName = args[1];
                    IMine mine = mineManager.getMine(mineName);
                    if (mine != null){
                        mine.reset();
                        //TODO Change to config
                        sender.sendMessage("Mines %s have been reset!".formatted(mineName));
                    }
                    else {
                        //TODO Change to config
                        sender.sendMessage("Mine %s have not found!".formatted(mineName));
                    }
                }

                if (arg1.equalsIgnoreCase("create")
                        && sender.hasPermission("simplymines.reset")
                        && sender instanceof Player player){
                    String mineName = args[1];
                    IMine mine = mineManager.getMine(mineName);
                    if (mine != null){
                        //TODO Change to config
                        sender.sendMessage("Mine %s already exists.".formatted(mineName));
                    }
                    else {
                        Pair<Location, Location> corners = selectionManager.getCorners(player.getUniqueId());
                        if (corners != null
                                && corners.first() != null
                                && corners.second() != null){
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
                        }
                        else {
                            //TODO Change to config
                            sender.sendMessage("No Selection found! use Wooden Hoe to select 2 corners");
                        }

                    }


                }

            }

        }

        return true;
    }

}