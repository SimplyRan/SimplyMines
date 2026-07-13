package me.simplyran.simplymines.commands;

import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
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
                if (sender.hasPermission("simplymine.admin")
                        &&
                        sender instanceof Player player) {
                    guiManager.openMainGUI(player);
                }
                break;
            }

            case 1: {
                String arg1 = args[0];
                if (arg1.equalsIgnoreCase("reload")) {
                    workloadRunnable.resetWorkloadDeque();
                    mineManager.reloadMines();
                }
                break;
            }

        }

        return true;
    }

}