package me.simplyran.simplymines.utils;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.workload.IBlock;
import me.simplyran.simplymines.workload.blocks.*;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.util.Key;
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

        if (SimplyMines.isCRAFTENGINE_LOADED()) {
            Key key = keyFromName(name);
            BukkitItemDefinition bukkitItemDefinition = CraftEngineItems.byId(key);
            if (bukkitItemDefinition != null){
                return bukkitItemDefinition.buildBukkitItem();
            }
        }


        return new ItemStack(Material.BARRIER);
    }

    public static boolean isBlock(ItemStack itemStack){
        Material vannilaMaterial = itemStack.getType();
        if (vannilaMaterial.isBlock()){
            return true;
        }

        if (SimplyMines.isNEXO_LOADED()) {
            if (NexoBlocks.isCustomBlock(itemStack)) {
                return true;
            }
        }

        if (SimplyMines.isITEMSADDER_LOADED()) {
            if (CustomBlock.isBlock(itemStack)) return true;
        }

        if (SimplyMines.isCRAFTENGINE_LOADED()) {
            Key key = CraftEngineItems.getCustomItemId(itemStack);
            if (key != null){
                BlockDefinition blockDefinition = CraftEngineBlocks.byId(key);
                if (blockDefinition != null) return true;
            }
        }


        return false;

    }

    public static IBlock getCustomBlock(String name){
        Material vannilaMaterial = Material.matchMaterial(name);
        if (vannilaMaterial != null
                && vannilaMaterial.isBlock()){
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

        if (SimplyMines.isCRAFTENGINE_LOADED()) {
            Key key = keyFromName(name);
            BlockDefinition blockDefinition = CraftEngineBlocks.byId(key);
            if (blockDefinition != null){
                return new CraftEngineBlock(key);
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

        if (SimplyMines.isCRAFTENGINE_LOADED()) {
            Key key = CraftEngineItems.getCustomItemId(itemStack);
            if (key != null){
                return keyToName(key);
            }
        }


        return itemStack.getType().name();
    }

    public static IBlock getNoPhysicsBlock(IBlock block){
        if (block instanceof Block(Material material))
            block = new NoPhysicsBlock(material);
        if (block instanceof NexoBlock(String blockID))
            block = new NoPhysicsNexoBlock(blockID);
        if (block instanceof CraftEngineBlock(Key key))
            block = new NoPhysicsCraftEngineBlock(key);
        return block;
    }

    public static Key keyFromName(String name){
        return Key.of(name.split(":"));
    }

    public static String keyToName(Key key){
        return key.namespace() + ":" + key.value();
    }




}
