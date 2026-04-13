package com.kuksa.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TaskScheduler {

    private TaskScheduler() {}

    public static List<SensorResult> run(List<SensorTask> tasks, int poolSize)
            throws InterruptedException {

        if (tasks == null || tasks.isEmpty()) {
            throw new IllegalArgumentException("tasks is null or empty");
        }
        if (poolSize <= 0) {
            throw new IllegalArgumentException("poolSize must be > 0");
        }

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(poolSize);
        AtomicBoolean stopped = new AtomicBoolean(false);

        Runnable stopAll = () -> {
            if (!stopped.compareAndSet(false, true)) {
                return;
            }

            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        };

        Runtime.getRuntime().addShutdownHook(new Thread(stopAll));

        try {
            for (SensorTask task : tasks) {
                scheduler.scheduleAtFixedRate(
                        task,
                        0,
                        task.intervalMs(),
                        TimeUnit.MILLISECONDS
                );
            }

            awaitCompletion(tasks);
            stopAll.run();

            List<SensorResult> results = new ArrayList<>(tasks.size());
            for (SensorTask task : tasks) {
                results.add(task.result());
            }
            return results;

        } finally {
            stopAll.run();
        }
    }

    private static void awaitCompletion(List<SensorTask> tasks) throws InterruptedException {
        while (!allTasksFinished(tasks)) {
            Thread.sleep(50);
        }
    }

    private static boolean allTasksFinished(List<SensorTask> tasks) {
        for (SensorTask task : tasks) {
            if (!task.isFinished()) {
                return false;
            }
        }
        return true;
    }
}