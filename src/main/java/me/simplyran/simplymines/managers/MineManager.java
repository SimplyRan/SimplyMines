package me.simplyran.simplymines.managers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.objects.IMine;
import me.simplyran.simplymines.objects.impl.BasicMine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MineManager {

    @Getter
    private final List<IMine> mines;
    private final SimplyMines plugin;

    public MineManager(SimplyMines plugin){
        this.plugin = plugin;
        mines = new ArrayList<>();


        loadMines();
    }


    public void addMine(IMine mine){
        mines.add(mine);
    }


    public void loadMines() {
        File minesFolder = new File(plugin.getDataFolder(), "mines");
        if (!minesFolder.exists()) {
            minesFolder.mkdirs();
            return;
        }

        File[] files = minesFolder.listFiles((dir, fileName) -> fileName.endsWith(".json"));
        if (files == null || files.length == 0) return;

        Gson gson = new Gson();

        for (File file : files) {
            String mineName = file.getName().substring(0, file.getName().length() - ".json".length());

            try (FileReader reader = new FileReader(file)) {
                JsonObject json = gson.fromJson(reader, JsonObject.class);

                int resetTime = json.get("resetTime").getAsInt();

                String worldName = json.get("world").getAsString();
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().warning("Skipping mine '" + mineName + "': world '" + worldName + "' is not loaded.");
                    continue;
                }

                JsonObject c1 = json.getAsJsonObject("corner1");

                JsonObject c2 = json.getAsJsonObject("corner2");

                Location corner1 = new Location(world,
                        c1.get("x").getAsDouble(),
                        c1.get("y").getAsDouble(),
                        c1.get("z").getAsDouble());

                Location corner2 = new Location(world,
                        c2.get("x").getAsDouble(),
                        c2.get("y").getAsDouble(),
                        c2.get("z").getAsDouble());

                Map<Material, Double> materials = new HashMap<>();
                JsonObject materialsJson = json.getAsJsonObject("materials");
                for (Map.Entry<String, JsonElement> entry : materialsJson.entrySet()) {
                    Material material = Material.matchMaterial(entry.getKey());
                    if (material == null) {
                        plugin.getLogger().warning("Skipping unknown material '" + entry.getKey() + "' in mine '" + mineName + "'.");
                        continue;
                    }
                    materials.put(material, entry.getValue().getAsDouble());
                }

                BasicMine mine = new BasicMine(mineName, resetTime, corner1, corner2, materials, plugin.getWorkloadRunnable());
                mines.add(mine);

            } catch (IOException | JsonSyntaxException | NullPointerException e) {
                plugin.getLogger().warning("Failed to load mine from '" + file.getName() + "': " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + mines.size() + " mine(s).");
    }
    public void reloadMines(){
        mines.clear();
        loadMines();
    }









}
