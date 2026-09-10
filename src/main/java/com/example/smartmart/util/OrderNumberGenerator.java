package com.example.smartmart.util;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class OrderNumberGenerator {

    private static final AtomicLong counter = new AtomicLong(0);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    public String generate() {
        return "ORD-" + LocalDateTime.now().format(FORMATTER) + "-" + String.format("%04d", counter.incrementAndGet() % 10000);
    }
}
