package me.simplyran.simplymines.actions.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.Pair;
import me.simplyran.simplymines.actions.IAction;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ItemDropAction implements IAction {

    private static final Random RANDOM = new Random();
    private static final Gson GSON = new Gson();

    public static final String NAME = "item_drop_action";

    private ItemStack itemStack;
    private double chance;

    public ItemDropAction(@NotNull ItemStack itemStack) {
        this(itemStack, 1.0);
    }

    public ItemDropAction(@NotNull ItemStack itemStack, double chance) {
        this.itemStack = itemStack.clone();
        this.chance = chance;
    }

    @Override
    public void perform(@NotNull Location location,
                        @NotNull BasicMine mine,
                        @NotNull Player player) {

        int fortuneLevel = 0;

        if (mine.isFortuneEnabled()) {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (mainHand.hasItemMeta()) {
                fortuneLevel = mainHand.getEnchantmentLevel(Enchantment.FORTUNE);
            }
        }

        // Calculate total drops: base stack amount * standard Minecraft fortune multiplier
        int multiplier = getItemMultiCount(fortuneLevel);
        int totalAmount = itemStack.getAmount() * multiplier;

        ItemStack drop = itemStack.clone();
        drop.setAmount(totalAmount);

        if (mine.isAutoPickup()) {
            HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(drop);

            // Drop leftovers on the ground if inventory gets full
            if (!remaining.isEmpty() && location.getWorld() != null) {
                for (ItemStack leftover : remaining.values()) {
                    location.getWorld().dropItem(location, leftover);
                }
            }
        } else if (location.getWorld() != null) {
            location.getWorld().dropItem(location, drop);
        }
    }

    @Override
    public List<Pair<String, Object>> serialize() {
        return List.of(Pair.of("itemStack", itemStack.serialize()), Pair.of("chance", chance));
    }

    @Override
    public String name() {
        return NAME;
    }

    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    public void setItemStack(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    public int getAmount() {
        return itemStack.getAmount();
    }

    public void setAmount(int amount) {
        itemStack.setAmount(Math.clamp(amount, 1, itemStack.getMaxStackSize()));
    }

    @Override
    public double getChance() {
        return chance;
    }

    @Override
    public void setChance(double chance) {
        this.chance = Math.clamp(chance, 0, 1);
    }

    public static IAction deserialize(@NotNull JsonObject json) {
        if (!json.has("itemStack") || !json.get("itemStack").isJsonObject()) {
            return null;
        }

        JsonObject itemObj = json.getAsJsonObject("itemStack");
        Map<String, Object> serializedMap = jsonToMap(itemObj);

        ItemStack itemStack = ItemStack.deserialize(serializedMap);
        double chance = json.has("chance") ? json.get("chance").getAsDouble() : 1.0;
        return new ItemDropAction(itemStack, chance);
    }

    private static Map<String, Object> jsonToMap(JsonObject jsonObject) {
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        return GSON.fromJson(jsonObject, type);
    }

    /**
     * Standard Minecraft Vanilla Fortune drop multiplier calculation.
     */
    public static int getItemMultiCount(int fortuneLevel) {
        if (fortuneLevel <= 0) {
            return 1;
        }

        int roll = RANDOM.nextInt(fortuneLevel + 2);

        if (roll < 2) {
            return 1;
        }

        return roll;
    }
}