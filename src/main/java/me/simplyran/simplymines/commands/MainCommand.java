package me.simplyran.simplymines.commands;

import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.IMine;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MainCommand implements CommandExecutor {

    private final MineManager mineManager;
    private final GuiManager guiManager;
    private final WorkloadRunnable workloadRunnable;

    public MainCommand(@NotNull MineManager mineManager,
                       @NotNull GuiManager guiManager,
                       @NotNull WorkloadRunnable workloadRunnable) {
        this.mineManager = mineManager;
        this.guiManager = guiManager;
        this.workloadRunnable = workloadRunnable;
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

            }

        }

        return true;
    }

}