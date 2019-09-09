package ru.urururu.sanity.cpp.util;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import de.vandermeer.asciitable.AsciiTable;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A reporter which outputs measurements to a {@link PrintStream}, like {@code System.out}.
 */
public class TableConsoleReporter extends ScheduledReporter {

    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    public static class Builder {
        private final MetricRegistry registry;
        private PrintStream output;
        private Locale locale;
        private Clock clock;
        private TimeZone timeZone;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private ScheduledExecutorService executor;
        private boolean shutdownExecutorOnStop;
        private Set<MetricAttribute> disabledMetricAttributes;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.output = System.out;
            this.locale = Locale.getDefault();
            this.clock = Clock.defaultClock();
            this.timeZone = TimeZone.getDefault();
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.executor = null;
            this.shutdownExecutorOnStop = true;
            disabledMetricAttributes = Collections.emptySet();
        }

        /**
         * Specifies whether or not, the executor (used for reporting) will be stopped with same time with reporter.
         * Default value is true.
         * Setting this parameter to false, has the sense in combining with providing external managed executor via {@link #scheduleOn(ScheduledExecutorService)}.
         *
         * @param shutdownExecutorOnStop if true, then executor will be stopped in same time with this reporter
         * @return {@code this}
         */
        public Builder shutdownExecutorOnStop(boolean shutdownExecutorOnStop) {
            this.shutdownExecutorOnStop = shutdownExecutorOnStop;
            return this;
        }

        /**
         * Specifies the executor to use while scheduling reporting of metrics.
         * Default value is null.
         * Null value leads to executor will be auto created on start.
         *
         * @param executor the executor to use while scheduling reporting of metrics.
         * @return {@code this}
         */
        public Builder scheduleOn(ScheduledExecutorService executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Write to the given {@link PrintStream}.
         *
         * @param output a {@link PrintStream} instance.
         * @return {@code this}
         */
        public Builder outputTo(PrintStream output) {
            this.output = output;
            return this;
        }

        /**
         * Format numbers for the given {@link Locale}.
         *
         * @param locale a {@link Locale}
         * @return {@code this}
         */
        public Builder formattedFor(Locale locale) {
            this.locale = locale;
            return this;
        }

        /**
         * Use the given {@link Clock} instance for the time.
         *
         * @param clock a {@link Clock} instance
         * @return {@code this}
         */
        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        /**
         * Use the given {@link TimeZone} for the time.
         *
         * @param timeZone a {@link TimeZone}
         * @return {@code this}
         */
        public Builder formattedFor(TimeZone timeZone) {
            this.timeZone = timeZone;
            return this;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Don't report the passed metric attributes for all metrics (e.g. "p999", "stddev" or "m15").
         * See {@link MetricAttribute}.
         *
         * @param disabledMetricAttributes a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder disabledMetricAttributes(Set<MetricAttribute> disabledMetricAttributes) {
            this.disabledMetricAttributes = disabledMetricAttributes;
            return this;
        }

        public TableConsoleReporter build() {
            return new TableConsoleReporter(registry,
                output,
                locale,
                clock,
                timeZone,
                rateUnit,
                durationUnit,
                filter,
                executor,
                shutdownExecutorOnStop,
                disabledMetricAttributes);
        }
    }

    private static final int CONSOLE_WIDTH = 80;

    private final PrintStream output;
    private final Locale locale;
    private final Clock clock;
    private final DateFormat dateFormat;

    private TableConsoleReporter(MetricRegistry registry,
                            PrintStream output,
                            Locale locale,
                            Clock clock,
                            TimeZone timeZone,
                            TimeUnit rateUnit,
                            TimeUnit durationUnit,
                            MetricFilter filter,
                            ScheduledExecutorService executor,
                            boolean shutdownExecutorOnStop,
                            Set<MetricAttribute> disabledMetricAttributes) {
        super(registry, "console-reporter", filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop, disabledMetricAttributes);
        this.output = output;
        this.locale = locale;
        this.clock = clock;
        this.dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT,
            DateFormat.MEDIUM,
            locale);
        dateFormat.setTimeZone(timeZone);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        final String dateTime = dateFormat.format(new Date(clock.getTime()));
        printWithBanner(dateTime, '=');
        output.println();

        if (!gauges.isEmpty()) {
            printWithBanner("-- Gauges", '-');
            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                output.println(entry.getKey());
                printGauge(entry.getValue());
            }
            output.println();
        }

        if (!counters.isEmpty()) {
            printWithBanner("-- Counters", '-');
            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                output.println(entry.getKey());
                printCounter(entry);
            }
            output.println();
        }

        if (!histograms.isEmpty()) {

            printWithBanner("-- Histograms", '-');

            AsciiTable table = new AsciiTable();
            table.addRule();

            addHistogramHeader(table);
            table.addRule();
            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                addHistogram(table, entry.getKey(), entry.getValue());
            }
            table.addRule();
            output.println(render(table));
        }

        if (!meters.isEmpty()) {
            printWithBanner("-- Meters", '-');
            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                output.println(entry.getKey());
                printMeter(entry.getValue());
            }
            output.println();
        }

        if (!timers.isEmpty()) {
            printWithBanner("-- Timers", '-');

            AsciiTable table = new AsciiTable();
            table.addRule();

            addTimerHeader(table);
            table.addRule();

            List<Map.Entry<String, Timer>> sorted = new ArrayList<>(timers.entrySet());
            Comparator<Map.Entry<String, Timer>> timerComparator = Comparator.comparing(e -> e.getValue().getCount() * e.getValue().getSnapshot().getMean());
            sorted.sort(timerComparator.reversed());

            for (Map.Entry<String, Timer> entry : sorted) {
                addTimer(table, entry.getKey(), entry.getValue());
            }
            table.addRule();
            output.println(render(table));
        }

        output.println();
        output.flush();
    }

    private String render(AsciiTable table) {
        return table.render(175);
    }

    private void printMeter(Meter meter) {
        printIfEnabled(MetricAttribute.COUNT, String.format(locale, "             count = %d", meter.getCount()));
        printIfEnabled(MetricAttribute.MEAN_RATE, String.format(locale, "         mean rate = %2.2f events/%s", convertRate(meter.getMeanRate()), getRateUnit()));
        printIfEnabled(MetricAttribute.M1_RATE, String.format(locale, "     1-minute rate = %2.2f events/%s", convertRate(meter.getOneMinuteRate()), getRateUnit()));
        printIfEnabled(MetricAttribute.M5_RATE, String.format(locale, "     5-minute rate = %2.2f events/%s", convertRate(meter.getFiveMinuteRate()), getRateUnit()));
        printIfEnabled(MetricAttribute.M15_RATE, String.format(locale, "    15-minute rate = %2.2f events/%s", convertRate(meter.getFifteenMinuteRate()), getRateUnit()));
    }

    private void printCounter(Map.Entry<String, Counter> entry) {
        output.printf(locale, "             count = %d%n", entry.getValue().getCount());
    }

    private void printGauge(Gauge<?> gauge) {
        output.printf(locale, "             value = %s%n", gauge.getValue());
    }

    private void addHistogram(AsciiTable table, String name, Histogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();

        List<Object> cells = new ArrayList<>();

        cells.add(name);
        addSnapshot(cells, histogram.getCount(), snapshot);

        table.addRow(cells);
    }

    private void addHistogramHeader(AsciiTable table) {
        List<Object> cells = new ArrayList<>();

        cells.add("name");
        addSnapshotHeaderCells(cells);

        table.addRow(cells);
    }

    private void addTimerHeader(AsciiTable table) {
        List<Object> cells = new ArrayList<>();

        cells.add("name");
        addSnapshotHeaderCells(cells);
        addIfEnabled(MetricAttribute.MEAN_RATE, cells, String.format(locale, "mean rate (calls/%s)", getRateUnit()));
        addIfEnabled(MetricAttribute.M1_RATE, cells, String.format(locale, "1-minute rate (calls/%s)", getRateUnit()));
        addIfEnabled(MetricAttribute.M5_RATE, cells, String.format(locale, "5-minute rate (calls/%s)", getRateUnit()));
        addIfEnabled(MetricAttribute.M15_RATE, cells, String.format(locale, "15-minute rate (calls/%s)", getRateUnit()));

        table.addRow(cells);
    }

    private void addSnapshotHeaderCells(List<Object> cells) {
        cells.add("count");
        cells.add("total");
        addIfEnabled(MetricAttribute.MIN, cells, String.format(locale, "min (%s)", getDurationUnit()));
        addIfEnabled(MetricAttribute.MAX, cells, String.format(locale, "max (%s)", getDurationUnit()));
        addIfEnabled(MetricAttribute.MEAN, cells, String.format(locale, "mean (%s)", getDurationUnit()));
        addIfEnabled(MetricAttribute.STDDEV, cells, String.format(locale, "stddev (%s)", getDurationUnit()));
        addIfEnabled(MetricAttribute.P50, cells, String.format(locale, "median (%s)", getDurationUnit()));
        addIfEnabled(MetricAttribute.P75, cells, String.format(locale, "> 75%% (%s)", getDurationUnit()));
        addIfEnabled(MetricAttribute.P95, cells, String.format(locale, "> 95%% (%s)", getDurationUnit()));
        addIfEnabled(MetricAttribute.P98, cells, String.format(locale, "> 98%% (%s)", getDurationUnit()));
        addIfEnabled(MetricAttribute.P99, cells, String.format(locale, "> 99%% (%s)", getDurationUnit()));
        addIfEnabled(MetricAttribute.P999, cells, String.format(locale, "> 99.9%% (%s)", getDurationUnit()));
    }

    private void addTimer(AsciiTable table, String name, Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();

        List<Object> cells = new ArrayList<>();

        cells.add(name);
        addSnapshot(cells, timer.getCount(), snapshot);
        addIfEnabled(MetricAttribute.MEAN_RATE, cells, String.format(locale, "%2.2f", convertRate(timer.getMeanRate())));
        addIfEnabled(MetricAttribute.M1_RATE, cells, String.format(locale, "%2.2f", convertRate(timer.getOneMinuteRate())));
        addIfEnabled(MetricAttribute.M5_RATE, cells, String.format(locale, "%2.2f", convertRate(timer.getFiveMinuteRate())));
        addIfEnabled(MetricAttribute.M15_RATE, cells, String.format(locale, "%2.2f", convertRate(timer.getFifteenMinuteRate())));

        table.addRow(cells);
    }

    private void addSnapshot(List<Object> cells, long count, Snapshot snapshot) {
        addIfEnabled(MetricAttribute.COUNT, cells, count);
        cells.add(String.format(locale, "%2.2f", convertDuration(count * snapshot.getMean())));
        addIfEnabled(MetricAttribute.MIN, cells, String.format(locale, "%2.2f", convertDuration(snapshot.getMin())));
        addIfEnabled(MetricAttribute.MAX, cells, String.format(locale, "%2.2f", convertDuration(snapshot.getMax())));
        addIfEnabled(MetricAttribute.MEAN, cells, String.format(locale, "%2.2f", convertDuration(snapshot.getMean())));
        addIfEnabled(MetricAttribute.STDDEV, cells, String.format(locale, "%2.2f", convertDuration(snapshot.getStdDev())));
        addIfEnabled(MetricAttribute.P50, cells, String.format(locale, "%2.2f", convertDuration(snapshot.getMedian())));
        addIfEnabled(MetricAttribute.P75, cells, String.format(locale, "%2.2f", convertDuration(snapshot.get75thPercentile())));
        addIfEnabled(MetricAttribute.P95, cells, String.format(locale, "%2.2f", convertDuration(snapshot.get95thPercentile())));
        addIfEnabled(MetricAttribute.P98, cells, String.format(locale, "%2.2f", convertDuration(snapshot.get98thPercentile())));
        addIfEnabled(MetricAttribute.P99, cells, String.format(locale, "%2.2f", convertDuration(snapshot.get99thPercentile())));
        addIfEnabled(MetricAttribute.P999, cells, String.format(locale, "%2.2f", convertDuration(snapshot.get999thPercentile())));
    }

    private void printWithBanner(String s, char c) {
        output.print(s);
        output.print(' ');
        for (int i = 0; i < (CONSOLE_WIDTH - s.length() - 1); i++) {
            output.print(c);
        }
        output.println();
    }

    /**
     * Print only if the attribute is enabled
     *
     * @param type   Metric attribute
     * @param status Status to be logged
     */
    private void printIfEnabled(MetricAttribute type, String status) {
        if (getDisabledMetricAttributes().contains(type)) {
            return;
        }

        output.println(status);
    }

    private <E> void addIfEnabled(MetricAttribute type, List<E> list, E item) {
        if (getDisabledMetricAttributes().contains(type)) {
            return;
        }

        list.add(item);
    }

    @Override
    protected String getDurationUnit() {
        return shortenUnit(super.getDurationUnit());
    }

    @Override
    protected String getRateUnit() {
        return shortenUnit(super.getRateUnit());
    }

    private String shortenUnit(String unit) {
        try {
            TimeUnit tu = TimeUnit.valueOf(unit.toUpperCase());

            switch (tu) {
                case NANOSECONDS:
                    return "ns";
                case MICROSECONDS:
                    return "µs";
                case MILLISECONDS:
                    return "ms";
                case SECONDS:
                    return "s";
                case MINUTES:
                    return "m";
                case HOURS:
                    return "h";
                case DAYS:
                    return "d";
                default:
                    return "n/a";
            }
        } catch (Throwable e) {
            return unit;
        }
    }
}
