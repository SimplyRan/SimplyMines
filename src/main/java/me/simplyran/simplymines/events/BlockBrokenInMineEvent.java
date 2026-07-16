package me.simplyran.simplymines.events;

import lombok.Getter;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class BlockBrokenInMineEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    @Getter private final BasicMine mine;
    @Getter private final Player player;
    @Getter private final Block block;

    public BlockBrokenInMineEvent(@NotNull BasicMine mine,
                                  Player player,
                                  Block block){
        this.mine = mine;
        this.player = player;
        this.block = block;
    }




    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
