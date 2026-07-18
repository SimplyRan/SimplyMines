package me.simplyran.simplymines;

import lombok.Getter;
import me.simplyran.simplymines.api.SimplyMinesAPI;
import me.simplyran.simplymines.bstats.Metrics;
import me.simplyran.simplymines.commands.MainCommand;
import me.simplyran.simplymines.commands.MainCommandTabComplete;
import me.simplyran.simplymines.listeners.BlockBreakListener;
import me.simplyran.simplymines.listeners.ChatInputListener;
import me.simplyran.simplymines.listeners.SelectionListener;
import me.simplyran.simplymines.managers.*;
import me.simplyran.simplymines.placeholders.MinePlaceholder;
import me.simplyran.simplymines.requirements.mine.MineRequirementRegistry;
import me.simplyran.simplymines.requirements.mine.impl.EfficiencyMineRequirement;
import me.simplyran.simplymines.requirements.mine.impl.PermissionMineRequirement;
import me.simplyran.simplymines.requirements.reset.ResetRequirementRegistry;
import me.simplyran.simplymines.requirements.reset.impl.PercentResetRequirement;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
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

    @Getter private static boolean CRAFTENGINE_LOADED = false;


    private int workloadTaskID;
    private int runnableManagerTaskID;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        checkLoadedTextureManagers();
        //Loading first requirements
        loadMineRequirements();
        loadResetRequirements();


        //Creating WorkloadRunnable
        this.workloadRunnable = new WorkloadRunnable();


        //Creating ConfigManager
        this.configManager = new ConfigManager(this);

        //Creating MineManager - depending on workloadRunnable
        this.mineManager = new MineManager(this, workloadRunnable, configManager);


        //Creating GUIManager
        this.guiManager = new GuiManager(configManager,this, mineManager);


        //Creating RunnableManager - depending on mineManager
        this.runnableManager = new RunnableManager(this, mineManager, configManager);

        //Creating SelectingManager
        this.selectionManager = new SelectionManager();

        //Register the API
        new SimplyMinesAPI(mineManager, selectionManager, configManager);




        //Scheduling the workload runnable
        this.workloadTaskID = this.getServer().getScheduler()
                .scheduleSyncRepeatingTask(this, workloadRunnable, 0, 1);

        //Scheduling the runnable manager
        this.runnableManagerTaskID = this.getServer().getScheduler()
                .scheduleSyncRepeatingTask(this, runnableManager, 20, 20);



        loadPlaceholders();
        registerListeners();
        registerCommands();

        registerBStats();
    }

    private void loadPlaceholders(){
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new MinePlaceholder(mineManager).register();
        }
    }

    private void checkLoadedTextureManagers(){
        if (this.getServer().getPluginManager().getPlugin("ItemsAdder") != null) {
            ITEMSADDER_LOADED = true;
        }
        if (this.getServer().getPluginManager().getPlugin("Nexo") != null) {
            NEXO_LOADED = true;
        }
        if (this.getServer().getPluginManager().getPlugin("CraftEngine") != null) {
            CRAFTENGINE_LOADED = true;
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Saving All Mines...");
        //TODO Change to other class
        runnableManager.saveAllMines();

        getServer().getScheduler().cancelTask(workloadTaskID);
        getServer().getScheduler().cancelTask(runnableManagerTaskID);
    }

    private void registerBStats(){
        int pluginId = 32650;
        new Metrics(this, pluginId);
    }

    private void loadMineRequirements(){
        MineRequirementRegistry.register(
                EfficiencyMineRequirement.NAME,
                EfficiencyMineRequirement::deserialize
        );

        MineRequirementRegistry.register(
                PermissionMineRequirement.NAME,
                PermissionMineRequirement::deserialize
        );
    }

    private void loadResetRequirements(){
        ResetRequirementRegistry.register(
                TimeResetRequirement.NAME,
                TimeResetRequirement::deserialize
        );
        ResetRequirementRegistry.register(
                PercentResetRequirement.NAME,
                PercentResetRequirement::deserialize
        );
    }



    private void registerListeners(){
        getServer().getPluginManager().registerEvents(
                new ChatInputListener(this), this);

        getServer().getPluginManager().registerEvents(
                new SelectionListener(selectionManager, configManager),
                this);

        getServer().getPluginManager().registerEvents(
                new BlockBreakListener(mineManager, configManager, this),
                this
        );
    }

    private void registerCommands(){

        MainCommand mainCommand = new MainCommand(mineManager,
                guiManager,
                workloadRunnable,
                selectionManager,
                configManager,
                this);

        PluginCommand simplyminesCommand = this.getCommand("sm");
        if (simplyminesCommand == null){
            getLogger().severe("No main command named 'sm' found in plugin.yml, could not load commands!");
            return;
        }
        simplyminesCommand.setExecutor(mainCommand);
        simplyminesCommand.setTabCompleter(new MainCommandTabComplete(mainCommand.getSubCommands()));
    }

}
