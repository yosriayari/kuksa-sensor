package com.kuksa.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.kuksa.sensor.SensorResult;

public final class CsvWriter implements AutoCloseable {

    private final PrintWriter writer;

    private CsvWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public static CsvWriter open(Path filePath, String header) throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("filePath is null");
        }

        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        BufferedWriter bw = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8);
        PrintWriter pw = new PrintWriter(bw);

        CsvWriter writer = new CsvWriter(pw);

        if (header != null && !header.isBlank()) {
            writer.writer.println(header);
        }

        return writer;
    }

    public void writeRow(String csvLine) {
        if (csvLine == null) {
            csvLine = "";
        }
        writer.println(csvLine);
    }

    public static void writeCombinedResults(
            List<SensorResult> results,
            Path outputPath
    ) throws IOException {
        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException("results is null or empty");
        }
        if (outputPath == null) {
            throw new IllegalArgumentException("outputPath is null");
        }

        String header = "signal,seq,t_pub_us,value";

        try (CsvWriter writer = CsvWriter.open(outputPath, header)) {
            for (SensorResult result : results) {
                if (result == null) {
                    continue;
                }

                for (int seq = 1; seq <= result.valueCount(); seq++) {
                    writer.writeRow(
                            result.signalPath() + "," +
                            seq + "," +
                            result.publishedAtUs(seq) + "," +
                            result.valueAt(seq)
                    );
                }
            }
        }
    }

    @Override
    public void close() {
        writer.flush();
        writer.close();
    }
}