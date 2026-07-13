package me.simplyran.simplymines.utils;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.workload.IBlock;
import me.simplyran.simplymines.workload.blocks.Block;
import me.simplyran.simplymines.workload.blocks.ItemsAdderBlock;
import me.simplyran.simplymines.workload.blocks.NexoBlock;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemUtils {



    public static ItemStack getItemStackFromName(String name){
        Material vannilaMaterial = Material.matchMaterial(name);
        if (vannilaMaterial != null){
            return new ItemStack(vannilaMaterial);
        }

        if (SimplyMines.isNEXO_LOADED()) {
            ItemBuilder nexoItem = NexoItems.itemFromId(name);
            if (nexoItem != null && nexoItem.getFinalItemStack() != null) {
                return nexoItem.getFinalItemStack().clone();
            }
        }

        if (SimplyMines.isITEMSADDER_LOADED()) {
            CustomStack stack = CustomStack.getInstance(name);
            if (stack != null) {
                return stack.getItemStack().clone();
            }
        }

        return new ItemStack(Material.BARRIER);
    }

    public static IBlock getCustomBlock(String name){
        Material vannilaMaterial = Material.matchMaterial(name);
        if (vannilaMaterial != null){
            return new Block(vannilaMaterial);
        }

        if (SimplyMines.isNEXO_LOADED()) {
            if (NexoBlocks.isCustomBlock(name)) {
                return new NexoBlock(name);
            }
        }

        if (SimplyMines.isITEMSADDER_LOADED()) {
            CustomBlock customBlock = CustomBlock.getInstance(name);
            if (customBlock != null) {
                return new ItemsAdderBlock(name);
            }
        }

        //Not Found
        return new Block(Material.AIR);
    }

    public static String getIDFromItemStack(ItemStack itemStack){
        if (SimplyMines.isNEXO_LOADED()){
            if (NexoBlocks.isCustomBlock(itemStack)){
                return NexoItems.idFromItem(itemStack);
            }
        }
        if (SimplyMines.isITEMSADDER_LOADED()){
            CustomBlock customBlock = CustomBlock.byItemStack(itemStack);
            if(customBlock != null)
            {
                return customBlock.getId();
            }
        }

        return itemStack.getType().name();
    }



}
