package com.kuksa.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class ConfigLoader {

    private static final long DEFAULT_TIMEOUT_MS = 10_000L;
    private static final int  DEFAULT_POOL_SIZE  = 8;

    private ConfigLoader() {}

    public static SensorConfig loadFromArgs(String[] args) throws IOException {
        String path = (args.length >= 1) ? args[0] : "sensor_config.json";
        return loadFromFile(path);
    }

    public static SensorConfig loadFromFile(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SensorConfig cfg = mapper.readValue(new File(path), SensorConfig.class);
        validateAndClean(cfg);
        return cfg;
    }

    private static void validateAndClean(SensorConfig cfg) {
        if (cfg == null) {
            throw new IllegalArgumentException("Config is null");
        }
        if (cfg.server == null || cfg.server.isBlank()) {
            throw new IllegalArgumentException("Missing config.server");
        }
        if (cfg.signals == null || cfg.signals.isEmpty()) {
            throw new IllegalArgumentException("Missing config.signals");
        }

        if (cfg.timeout_ms == null)       cfg.timeout_ms       = DEFAULT_TIMEOUT_MS;
        if (cfg.thread_pool_size == null) cfg.thread_pool_size = DEFAULT_POOL_SIZE;

        List<SignalCfg> valid = new ArrayList<>();
        for (SignalCfg signal : cfg.signals) {
            if (signal == null) continue;
            if (signal.path == null || signal.path.isBlank()) {
                throw new IllegalArgumentException("Signal path is blank");
            }
            if (signal.interval_ms <= 0) {
                throw new IllegalArgumentException("interval_ms must be > 0 for: " + signal.path);
            }
            if (signal.values_file == null || signal.values_file.isBlank()) {
                throw new IllegalArgumentException("values_file is required for signal: " + signal.path);
            }
            valid.add(signal);
        }

        if (valid.isEmpty()) {
            throw new IllegalArgumentException("No valid signals configured");
        }

        cfg.signals = valid;
    }
}