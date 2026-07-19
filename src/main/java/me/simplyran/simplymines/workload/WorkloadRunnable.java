package me.simplyran.simplymines.workload;

/*
thx for https://www.spigotmc.org/threads/guide-on-workload-distribution-or-how-to-handle-heavy-splittable-tasks.409003/
fot the guide!
 */


import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.objects.ConfigFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;

public class WorkloadRunnable implements Runnable{

    private final ConfigData<Integer> maxWorkloads = ConfigFactory.newConfigData("max_workload", 20_000_000);
    private final ConfigData<Double> maxMillisPerTick = ConfigFactory.newConfigData("max_milles_per_tick", 2.5);

    private Deque<Workload> workloadDeque = new ArrayDeque<>();

    public WorkloadRunnable(@NotNull ConfigManager configManager){
        configManager.register(maxWorkloads);
        configManager.register(maxMillisPerTick);
    }

    public void addWorkload(Workload workload){
        // If workload is more than max_workload its too much and not adding new workloads
        if (workloadDeque.size() >= maxWorkloads.getValue()) return;

        this.workloadDeque.add(workload);
    }


    public void resetWorkloadDeque(){
        workloadDeque = new ArrayDeque<>();
    }

    @Override
    public void run() {
        long maxNanosPerTick = (long) (maxMillisPerTick.getValue() * 1E6);
        long stopTime = System.nanoTime() + maxNanosPerTick;

        Workload nextLoad;
        while (System.nanoTime() <= stopTime && (nextLoad = this.workloadDeque.poll()) != null){
            nextLoad.compute();
        }
    }
}
