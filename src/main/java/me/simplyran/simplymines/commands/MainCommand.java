package me.simplyran.simplymines.commands;

import me.simplyran.simplymines.SimplyMines;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MainCommand implements CommandExecutor {

    private final SimplyMines plugin;

    public MainCommand(SimplyMines plugin){
        this.plugin = plugin;
    }



    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String
                             @NotNull [] args) {



        int argslen = args.length;

        switch (argslen){

            case 0: {
                if (sender instanceof Player player){
                    openMainGUI(player);
                    break;
                }

                break;
            }

            case 1:{
                String arg1 = args[0];
                if (arg1.equalsIgnoreCase("reload")){
                    plugin.getMineManager().reloadMines();
                }
                break;
            }

        }






        return true;
    }



    private void openMainGUI(Player player){

    }



}
