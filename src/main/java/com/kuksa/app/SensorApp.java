package com.kuksa.app;

import java.nio.file.Path;
import java.util.List;

import com.kuksa.client.ChannelFactory;
import com.kuksa.client.ServiceClient;
import com.kuksa.config.ConfigLoader;
import com.kuksa.config.SensorConfig;
import com.kuksa.sensor.SensorResult;
import com.kuksa.sensor.SensorTask;
import com.kuksa.sensor.SensorTaskFactory;
import com.kuksa.sensor.TaskScheduler;
import com.kuksa.util.CsvWriter;

public final class SensorApp {

    public static void main(String[] args) throws Exception {
        SensorConfig config = ConfigLoader.loadFromArgs(args);

        try (ServiceClient client =
                     new ServiceClient(
                             ChannelFactory.createChannel(config.server),
                             config.timeout_ms
                     )) {

            List<SensorTask> tasks =
                    SensorTaskFactory.createTasks(config, client);

            List<SensorResult> results =
                    TaskScheduler.run(tasks, config.thread_pool_size);

            CsvWriter.writeCombinedResults(
                    results,
                    Path.of("all_signals.csv")
            );
        }
    }
}