package ru.urururu.sanity.cpp.util;

import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import java.util.Arrays;
import java.util.HashSet;

public class Metrics {

    private static MetricRegistry registry = new MetricRegistry();
    private static Timer.Context lifetime = time(Metrics.class, "lifetime");

    static {
        TableConsoleReporter reporter = TableConsoleReporter.forRegistry(registry)
            .disabledMetricAttributes(new HashSet<>(Arrays.asList(MetricAttribute.M1_RATE, MetricAttribute.M5_RATE,
                MetricAttribute.M15_RATE, MetricAttribute.MEAN_RATE)))
            .build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            lifetime.close();
            reporter.report();
        }));
    }

    public static Timer.Context time(Class<?> klass, String... names) {
        Timer timer = registry.timer(getName(klass, names));
        return timer.time();
    }

    private static String getName(Class<?> klass, String[] names) {
        return MetricRegistry.name(klass.getSimpleName(), names);
    }
}
