package Java.com.lesrere.playground;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class ProgressLineOutputApplication {
    // Thresholds martin
    private static final Double TIMEOUT_LIMIT = 1.2;
    private static final Double SLOW_LIMIT = 1.1;
    private static final Double FAST_LIMIT = 0.9;

    // Symbols
    private static final String CS = "■"; // Leading Symbol
    private static final String AS = "▶"; // Arrow Symbol
    private static final String TS = "·"; // Trailing Symbol

    // Properties
    private static final int PROGRESS_MAX_CHAR = 100;
    private static final int MAX_ID_LONG = 16;

    // Public random usage
    private static final Random random = new Random();

    public ProgressLineOutputApplication() {
    }

    public static void main(String[] args) {
        final ArrayList<JobStatus> res = new ArrayList<>();
        final long start = System.currentTimeMillis();
        final long jobs = randomStep(2L, 20L);
        for (int i = 1; i < jobs; i++) {
            res.add(job(randomJobId()));
        }
        final long end = System.currentTimeMillis();
        final int success = res.stream().filter(it -> it.equals(JobStatus.SUCCESS)).toList().size();
        final int warning = res.stream().filter(it -> it.equals(JobStatus.WARNING)).toList().size();
        final int failure = res.stream().filter(it -> it.equals(JobStatus.FAILURE)).toList().size();
        final String output = "%sTotal %d jobs, totally using %ds, %d success, %d warning, %d failure%s "
                .formatted(Color.green, jobs, ((end - start) / 1000), success, warning, failure, Color.reset)
                .strip(); // .strip() removes leading and trailing whitespace
        System.out.println(output);
    }

    private static JobStatus job(Object id) {
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        final long max = random.nextLong(256, 1536 + 1);
        final long min = random.nextLong(1, 256);
        final long expectDuration = ((max + min) / 2) * PROGRESS_MAX_CHAR;
        System.out.printf("Job %s expect using %ds.%n", id.toString(), expectDuration / 1000);
        for (int i = 0; i < PROGRESS_MAX_CHAR; i++) {
            end = System.currentTimeMillis();
            final long duration = end - start;
            if (duration > (expectDuration * TIMEOUT_LIMIT)) {
                System.out.printf("\r%s%s job timeout%s\n", Color.red, progressBar(id, i, duration), Color.reset);
                return JobStatus.FAILURE;
            }
            final long average = duration / (i + 1);
            final long currencyFullyDuration = average * PROGRESS_MAX_CHAR;
            String color = Color.blue;
            if (currencyFullyDuration > expectDuration * TIMEOUT_LIMIT) {
                color = Color.red;
            } else if (currencyFullyDuration > expectDuration * SLOW_LIMIT) {
                color = Color.yellow;
            } else if (currencyFullyDuration < expectDuration * FAST_LIMIT) {
                color = Color.purple;
            }
            final long left = average * (PROGRESS_MAX_CHAR - i);
            System.out.printf("\r%s%s still %ss left", color, progressBar(id, i, duration), left / 1000);
            try {
                Thread.sleep(randomStep(min, max));
            } catch (InterruptedException e) {
                System.out.printf("\r%s%s system fatal%s\n", Color.red, progressBar(id, i, duration), Color.reset);
                return JobStatus.FAILURE;
            }
        }
        final long totalDuration = end - start;
        String color = Color.blue;
        JobStatus res = JobStatus.SUCCESS;
        if (totalDuration >= expectDuration * SLOW_LIMIT) {
            color = Color.yellow;
            res = JobStatus.WARNING;
        } else if (totalDuration <= (expectDuration * FAST_LIMIT)) {
            color = Color.purple;
        }
        System.out.printf("\r%s%s%s\n", color, progressBar(id, PROGRESS_MAX_CHAR, totalDuration), Color.reset);
        return res;
    }

    private static String progressBar(Object id, int i, long d) {
        final ArrayList<String> statusList = new ArrayList<>(List.of("|", "/", "-", "\\"));
        final String lead = (i == PROGRESS_MAX_CHAR) ? CS.repeat(i) : CS.repeat(Math.max(0, i - 1)) + AS;
        final String p = (i == PROGRESS_MAX_CHAR) ? "√" : statusList.get(i % statusList.size());
        return "%s: [%s%s] [%s] %d%% using %ds"
                .formatted(scriptId(id), lead, TS.repeat(Math.max(0, PROGRESS_MAX_CHAR - i)), p, i, d / 1000)
                .strip();
    }

    private static long randomStep(long min, long max) {
        long res = random.nextLong(min, max + 1);
        if (random.nextDouble() < 0.05) {
            res = random.nextLong(min, (max * 2) + 1);
        } else if (random.nextDouble() < 0.04) {
            res = random.nextLong(0, min + 1);
        }
        return res;
    }

    private static String randomJobId() {
        return UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
    }

    private static String scriptId(Object any) {
        final String str = any.toString();
        final String trail = " ".repeat(Math.max(0, MAX_ID_LONG - str.length()));
        final String lead = (str.length() > MAX_ID_LONG) ? str.substring(0, MAX_ID_LONG - 3) : str.substring(1, str.length() - 1);
        return "%s%s".formatted(lead, trail);
    }

    // Defined default color ANSI code
    private static class Color {
        static final String red = "\u001B[91m";

        static final String green = "\u001B[92m";

        static final String yellow = "\u001B[93m";

        static final String blue = "\u001B[94m";

        static final String purple = "\u001B[95m";

        static final String reset = "\u001B[0m";
    }

    private enum JobStatus {
        /**
         * Job running successful, complete in excepted time and without any error.
         */
        SUCCESS,
        /**
         * Job running with something error, but this error is not matter to complete.
         */
        WARNING,
        /**
         * Job running failure, never finished, current job is cancel causing some error or out of time.
         */
        FAILURE;

        JobStatus() {
        }
    }
}
