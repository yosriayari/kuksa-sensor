package com.kuksa.sensor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.kuksa.client.ServiceClient;
import com.kuksa.config.SensorConfig;
import com.kuksa.config.SignalCfg;
import com.kuksa.util.ValueLoader;

public final class SensorTaskFactory {

    private SensorTaskFactory() {}

    public static List<SensorTask> createTasks(
            SensorConfig config,
            ServiceClient client
    ) throws IOException {

        List<SensorTask> tasks = new ArrayList<>();

        for (SignalCfg signal : config.signals) {
            if (signal.values_file == null || signal.values_file.isBlank()) {
                throw new IllegalArgumentException(
                        "values_file is required for signal: " + signal.path);
            }

            float[] values = ValueLoader.loadFromFile(
                    Path.of(signal.values_file),
                    signal.path
            );


            SensorTask task = new SensorTask(
                    signal.path,
                    client,
                    signal.interval_ms,
                    values
            );

            tasks.add(task);
        }

        return tasks;
    }

}