package com.kuksa.util;

import com.google.protobuf.Timestamp;

import kuksa.val.v2.Types;

public final class TimestampCodec {

    private TimestampCodec() {}

    public static Types.Datapoint encode(long tPubUs, float value) {
        return Types.Datapoint.newBuilder()
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(tPubUs / 1_000_000L)
                        .setNanos((int) ((tPubUs % 1_000_000L) * 1_000L))
                        .build())
                .setValue(Types.Value.newBuilder()
                        .setFloat(value)
                        .build())
                .build();
    }

    public static Long decode(Types.Datapoint datapoint) {
        if (datapoint == null || !datapoint.hasValue() || !datapoint.hasTimestamp()) {
            return null;
        }

        if (datapoint.getValue().getTypedValueCase() != Types.Value.TypedValueCase.FLOAT) {
            return null;
        }

        return datapoint.getTimestamp().getSeconds() * 1_000_000L
                + datapoint.getTimestamp().getNanos() / 1_000L;
    }
}