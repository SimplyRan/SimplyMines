package me.simplyran.simplymines;

import lombok.Getter;
import me.simplyran.simplymines.commands.MainCommand;
import me.simplyran.simplymines.commands.MainCommandTabComplete;
import me.simplyran.simplymines.listeners.SelectionListener;
import me.simplyran.simplymines.managers.*;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimplyMines extends JavaPlugin {

    private WorkloadRunnable workloadRunnable;
    private RunnableManager runnableManager;
    private MineManager mineManager;
    private GuiManager guiManager;
    private SelectionManager selectionManager;
    private ConfigManager configManager;

    @Getter private static boolean ITEMSADDER_LOADED = false;

    @Getter private static boolean NEXO_LOADED = false;


    private int workloadTaskID;
    private int runnableManagerTaskID;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        checkLoadedTextureManagers();

        //Creating WorkloadRunnable
        this.workloadRunnable = new WorkloadRunnable();

        //Creating MineManager - depending on workloadRunnable
        this.mineManager = new MineManager(this, workloadRunnable);

        //Creating GUIManager
        this.guiManager = new GuiManager(this, mineManager);

        //Creating ConfigManager
        this.configManager = new ConfigManager(this);

        //Creating RunnableManager - depending on mineManager
        this.runnableManager = new RunnableManager(this, mineManager, configManager);

        //Creating SelectingManager
        this.selectionManager = new SelectionManager();



        //Scheduling the workload runnable
        this.workloadTaskID = this.getServer().getScheduler()
                .scheduleSyncRepeatingTask(this, workloadRunnable, 0, 1);

        //Scheduling the runnable manager
        this.runnableManagerTaskID = this.getServer().getScheduler()
                .scheduleSyncRepeatingTask(this, runnableManager, 20, 20);


        registerListeners();
        registerCommands();
    }

    private void checkLoadedTextureManagers(){
        if (this.getServer().getPluginManager().getPlugin("ItemsAdder") != null) {
            ITEMSADDER_LOADED = true;
        }
        if (this.getServer().getPluginManager().getPlugin("Nexo") != null) {
            NEXO_LOADED = true;
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        //TODO Change to other class
        runnableManager.saveAllMines();

        getServer().getScheduler().cancelTask(workloadTaskID);
        getServer().getScheduler().cancelTask(runnableManagerTaskID);
    }

    private void registerListeners(){
        getServer().getPluginManager().registerEvents(
                new SelectionListener(selectionManager, configManager),
                this);


    }

    private void registerCommands(){
        this.getCommand("sm")
                .setExecutor(new MainCommand(mineManager,
                        guiManager,
                        workloadRunnable,
                        selectionManager,
                        configManager));
        this.getCommand("sm").setTabCompleter(new MainCommandTabComplete(mineManager));
    }

}
