package com.kuksa.app;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.kuksa.client.ServiceClient;
import com.kuksa.config.ConfigLoader;
import com.kuksa.config.SensorConfig;
import com.kuksa.config.SignalCfg;
import com.kuksa.sensor.SensorResult;
import com.kuksa.sensor.SensorTask;
import com.kuksa.sensor.TaskScheduler;
import com.kuksa.util.CsvWriter;
import com.kuksa.util.ValueLoader;

import io.grpc.ManagedChannelBuilder;

public final class SensorApp {

    public static void main(String[] args) throws Exception {
        SensorConfig config = ConfigLoader.loadFromArgs(args);

        try (ServiceClient client = new ServiceClient(
                ManagedChannelBuilder
                    .forTarget(config.server.trim())
                    .usePlaintext()
                    .build(),
                config.timeout_ms
        )) {
            List<SensorTask> tasks = new ArrayList<>();
            for (SignalCfg signal : config.signals) {
                float[] values = ValueLoader.loadFromFile(
                        Path.of(signal.values_file), signal.path);
                tasks.add(new SensorTask(signal.path, client, signal.interval_ms, values));
            }

            List<SensorResult> results = TaskScheduler.run(tasks, config.thread_pool_size);

            CsvWriter.writeCombinedResults(results, Path.of("all_signals.csv"));
        }
    }
}