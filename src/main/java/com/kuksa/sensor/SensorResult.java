package com.kuksa.sensor;

import java.util.Arrays;

public final class SensorResult {

    private final String signalPath;
    private final long[] publishedAtUsBySeq;
    private final float[] valuesBySeq;

    public SensorResult(
            String signalPath,
            long[] publishedAtUsBySeq,
            float[] valuesBySeq
    ) {
        if (signalPath == null || signalPath.isBlank()) {
            throw new IllegalArgumentException("signalPath is blank");
        }
        if (publishedAtUsBySeq == null) {
            throw new IllegalArgumentException("publishedAtUsBySeq is null");
        }
        if (valuesBySeq == null) {
            throw new IllegalArgumentException("valuesBySeq is null");
        }


        this.signalPath = signalPath;
        this.publishedAtUsBySeq = Arrays.copyOf(publishedAtUsBySeq, publishedAtUsBySeq.length);
        this.valuesBySeq = Arrays.copyOf(valuesBySeq, valuesBySeq.length);
        
    }

    public String signalPath() {
        return signalPath;
    }

    public long publishedAtUs(int seq) {
        checkSeq(seq);
        return publishedAtUsBySeq[seq];
    }

    public float valueAt(int seq) {
        checkSeq(seq);
        return valuesBySeq[seq];
    }

    public int valueCount() {
    return valuesBySeq.length - 1;
    }

    public boolean hasPublicationAt(int seq) {
        checkSeq(seq);
        return publishedAtUsBySeq[seq] >= 0;
    }

    private void checkSeq(int seq) {
        if (seq < 1 || seq > valueCount()) {
            throw new IllegalArgumentException("seq out of range: " + seq);
        }
    }
}
