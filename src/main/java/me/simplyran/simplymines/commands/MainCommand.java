package me.simplyran.simplymines.commands;

import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MainCommand implements CommandExecutor {

    private final MineManager mineManager;
    private final GuiManager guiManager;

    public MainCommand(MineManager mineManager, GuiManager guiManager) {
        this.mineManager = mineManager;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        switch (args.length) {

            case 0: {
                if (sender instanceof Player player) {
                    guiManager.openMainGUI(player);
                }
                break;
            }

            case 1: {
                String arg1 = args[0];
                if (arg1.equalsIgnoreCase("reload")) {
                    mineManager.reloadMines();
                }
                break;
            }

        }

        return true;
    }

}