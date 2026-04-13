// TimeUtil.java
package com.kuksa.util;

import java.time.Instant;

public final class TimeUtil {

    private TimeUtil() {}

    public static long nowEpochMicros() {
        Instant now = Instant.now();
        return now.getEpochSecond() * 1_000_000L
                + now.getNano() / 1_000L;
    }
}
