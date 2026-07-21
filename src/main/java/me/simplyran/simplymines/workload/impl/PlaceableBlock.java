package me.simplyran.simplymines.workload.impl;

import lombok.AllArgsConstructor;
import me.simplyran.simplymines.workload.IBlock;
import me.simplyran.simplymines.workload.Workload;
import org.bukkit.Location;

/*
thx for
https://www.spigotmc.org/threads/guide-on-workload-distribution-or-how-to-handle-heavy-splittable-tasks.409003/
fot the guide!
 */

@AllArgsConstructor
public class PlaceableBlock implements Workload {

    private final Location location;
    private final IBlock block;


    @Override
    public void compute() {
        block.place(location);
    }
}
