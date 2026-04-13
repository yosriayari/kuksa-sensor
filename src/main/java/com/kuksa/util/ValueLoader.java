package com.kuksa.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ValueLoader {

    private ValueLoader() {}

    public static float[] loadFromFile(Path path, String signalPath) throws IOException {
        List<String> lines = Files.readAllLines(path);

        if (lines.isEmpty()) {
            throw new IllegalArgumentException(
                    "values_file is empty for signal: " + signalPath);
        }

        float[] values = new float[lines.size() + 1]; // index 0 unused

        for (int i = 0; i < lines.size(); i++) {
            values[i + 1] = Float.parseFloat(lines.get(i).trim());
        }

        return values;
    }
}