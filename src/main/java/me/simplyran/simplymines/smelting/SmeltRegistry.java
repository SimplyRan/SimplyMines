package me.simplyran.simplymines.smelting;

import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.objects.ConfigData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public final class SmeltRegistry {

    private static final ConfigData<List<String>> EXCLUDED_MATERIALS = ConfigFactory.newConfigData(
            "auto-smelt.excluded-materials", List.of("COBBLESTONE", "COBBLED_DEEPSLATE", "STONE"));

    private static volatile Map<Material, ItemStack> results = Map.of();

    private SmeltRegistry() {}

    public static void init(@NotNull ConfigManager configManager) {
        configManager.register(EXCLUDED_MATERIALS);
    }

    private static Set<Material> readExcluded() {
        Set<Material> excluded = EnumSet.noneOf(Material.class);
        List<String> names = EXCLUDED_MATERIALS.getValue();
        if (names == null) return excluded;

        for (Object name : names) {
            Material material = Material.matchMaterial(String.valueOf(name));
            if (material == null) {
                Bukkit.getLogger().warning("[SimplyMines] Unknown material in auto-smelt.excluded-materials: " + name);
                continue;
            }
            excluded.add(material);
        }
        return excluded;
    }

    public static void rebuild() {
        Map<Material, ItemStack> built = new EnumMap<>(Material.class);
        Set<Material> excluded = readExcluded();

        try {
            Iterator<Recipe> iterator = Bukkit.recipeIterator();
            while (iterator.hasNext()) {
                if (!(iterator.next() instanceof FurnaceRecipe recipe)) continue;

                ItemStack result = recipe.getResult();
                if (result.isEmpty()) continue;

                if (!(recipe.getInputChoice() instanceof RecipeChoice.MaterialChoice choice)) continue;

                for (Material input : choice.getChoices()) {
                    if (excluded.contains(input)) continue;
                    built.putIfAbsent(input, result.clone());
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING,
                    "[SimplyMines] Could not read every furnace recipe, auto smelt may be incomplete.", e);
        }

        results = Collections.unmodifiableMap(built);
    }

    public static void clear() {
        results = Map.of();
    }

    @Nullable
    public static ItemStack smelt(@NotNull ItemStack stack) {
        if (stack.isEmpty() || stack.hasItemMeta()) return null;

        ItemStack result = results.get(stack.getType());
        if (result == null) return null;

        ItemStack smelted = result.clone();
        smelted.setAmount(stack.getAmount() * result.getAmount());
        return smelted;
    }
}
