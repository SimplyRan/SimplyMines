package me.simplyran.simplymines.utils;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import me.simplyran.simplymines.workload.Workload;
import me.simplyran.simplymines.workload.impl.ItemsAdderBlock;
import me.simplyran.simplymines.workload.impl.NexoBlock;
import me.simplyran.simplymines.workload.impl.PlaceableBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemUtils {



    public static ItemStack getItemStackFromName(String name){
        Material vannilaMaterial = Material.matchMaterial(name);
        if (vannilaMaterial != null){
            return new ItemStack(vannilaMaterial);
        }
        ItemBuilder nexoItem = NexoItems.itemFromId(name);
        if (nexoItem != null && nexoItem.getFinalItemStack() != null){
            System.out.println("NEXO ITEM!");
            return nexoItem.getFinalItemStack();
        }
        CustomStack stack = CustomStack.getInstance(name);
        if(stack != null) {
            return stack.getItemStack();
        }

        return new ItemStack(Material.BARRIER);
    }

    public static Workload getCustomWorkload(String name, Location location){
        Material vannilaMaterial = Material.matchMaterial(name);
        if (vannilaMaterial != null){
            return new PlaceableBlock(location.getWorld().getUID(),
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ(),
                    vannilaMaterial);
        }
        if (NexoBlocks.isCustomBlock(name)){
            return new NexoBlock(location.getWorld().getUID(),
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ(),
                    name);
        }
        CustomBlock customBlock = CustomBlock.getInstance(name);
        if(customBlock != null)
        {
            return new ItemsAdderBlock(location.getWorld().getUID(),
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ(),
                    name);
        }

        return new PlaceableBlock(location.getWorld().getUID(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                Material.STONE);

    }



}
