package com.kuksa.sensor;

import com.kuksa.client.ServiceClient;
import com.kuksa.util.TimeUtil;
import com.kuksa.util.TimestampCodec;

import kuksa.val.v2.Types;

public final class SensorTask implements Runnable {

    private final String signalPath;
    private final ServiceClient client;
    private final long intervalMs;

    private int seq = 0;
    private volatile boolean finished = false;

    private final long[] tPubUs;
    private final float[] value;

    public SensorTask(
            String signalPath,
            ServiceClient client,
            long intervalMs,
            float[] value
    ) {
        if (signalPath == null || signalPath.isBlank()) {
            throw new IllegalArgumentException("signalPath is blank");
        }
        if (client == null) {
            throw new IllegalArgumentException("client is null");
        }
        if (intervalMs <= 0) {
            throw new IllegalArgumentException("intervalMs must be > 0");
        }
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }


        this.signalPath = signalPath;
        this.client = client;
        this.intervalMs = intervalMs;
        this.value = value;

        this.tPubUs = new long[value.length];

        for (int i = 0; i < value.length; i++) {
            tPubUs[i] = -1;
        }
    }

    @Override
    public void run() {
        if (finished) {
            return;
        }

        try {
            seq++;

            long pubUs = TimeUtil.nowEpochMicros();
            float currentValue = value[seq];

            Types.Datapoint datapoint =
                    TimestampCodec.encode(pubUs, currentValue);

            client.publishValueByPath(signalPath, datapoint);

            tPubUs[seq] = pubUs;

            if (seq >= value.length - 1) {
                finished = true;
            }

        } catch (Throwable t) {
            System.err.println("SensorTask error for " + signalPath + ": " + t);
        }
    }

    // --- getters ---

    public String signalPath() {
        return signalPath;
    }

    public long intervalMs() {
        return intervalMs;
    }

    public int currentSeq() {
        return seq;
    }

    public boolean isFinished() {
        return finished;
    }

    public long[] tPubUsBySeq() {
        return tPubUs;
    }

    public float[] value() {
        return value;
    }

    public SensorResult result() {
        return new SensorResult(signalPath, tPubUs, value);
    }
}