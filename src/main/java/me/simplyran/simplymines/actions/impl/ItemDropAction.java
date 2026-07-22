package me.simplyran.simplymines.actions.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.Pair;
import me.simplyran.simplymines.actions.IAction;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ItemDropAction implements IAction {

    public final static String NAME = "item_drop_action";

    private final ItemStack itemStack;

    public ItemDropAction(@NotNull ItemStack itemStack){
        this.itemStack = itemStack.clone();
    }

    @Override
    public void perform(@NotNull Location location,
                        @NotNull BasicMine mine,
                        @NotNull Player player) {

        if (mine.isAutoPickup()) player.getInventory().addItem(itemStack.clone());
        else location.getWorld().dropItem(location, itemStack.clone());
    }

    @Override
    public List<Pair<String, Object>> serialize() {
        return List.of(Pair.of("itemStack", itemStack.serialize()));
    }

    @Override
    public String name() {
        return NAME;
    }


    public static IAction deserialize(@NotNull JsonObject json) {
        if (!json.has("itemStack") || !json.get("itemStack").isJsonObject()) {
            return null;
        }

        JsonObject itemObj = json.getAsJsonObject("itemStack");
        Map<String, Object> serializedMap = jsonToMap(itemObj);

        ItemStack itemStack = ItemStack.deserialize(serializedMap);
        return new ItemDropAction(itemStack);
    }

    private static Map<String, Object> jsonToMap(JsonObject jsonObject) {
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        return new Gson().fromJson(jsonObject, type);
    }

}
