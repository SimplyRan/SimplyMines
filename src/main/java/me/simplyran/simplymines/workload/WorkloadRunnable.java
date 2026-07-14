package me.simplyran.simplymines.workload;

/*
thx for https://www.spigotmc.org/threads/guide-on-workload-distribution-or-how-to-handle-heavy-splittable-tasks.409003/
fot the guide!
 */


import java.util.ArrayDeque;
import java.util.Deque;

public class WorkloadRunnable implements Runnable{

    private static final double MAX_MILLIS_PER_TICK = 2.5;
    private static final int MAX_NANOS_PER_TICK = (int) (MAX_MILLIS_PER_TICK*1E6);
    private static final int MAX_WORKLOADS = 20_000_000;
    private Deque<Workload> workloadDeque = new ArrayDeque<>();

    public void addWorkload(Workload workload){
        // If workload is more than 1m too much
        if (workloadDeque.size() >= MAX_WORKLOADS) return;

        this.workloadDeque.add(workload);
    }


    public void resetWorkloadDeque(){
        workloadDeque = new ArrayDeque<>();
    }

    @Override
    public void run() {
        long stopTime = System.nanoTime() + MAX_NANOS_PER_TICK;

        Workload nextLoad;
        while (System.nanoTime() <= stopTime && (nextLoad = this.workloadDeque.poll()) != null){
            nextLoad.compute();
        }
    }
}
