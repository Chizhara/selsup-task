import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {

    private final int requestLimit;
    private final AtomicInteger requestCounter;
    private final Duration delay;
    private long start;
    private final ScheduledExecutorService executorService;

    public Test(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.requestCounter = new AtomicInteger(0);
        this.delay = Duration.ofMillis(timeUnit.toMillis(1) / requestLimit);
        this.start = System.currentTimeMillis();
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public static void main(String[] args) throws InterruptedException {
        Test test = new Test(TimeUnit.SECONDS, 2);

        for (int i = 0; i < 10; i++) {
            final int j = i + 1;
            new Thread(() -> {
                try {
                    test.create(j);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }

        Thread.sleep(TimeUnit.SECONDS.toMillis(5));

        for (int i = 0; i < 10; i++) {

            final int j = i + 11;
            new Thread(() -> {
                try {
                    //System.out.println("Проходка  №" + j);
                    test.create(j);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }

    public void create(int i) throws InterruptedException {
        int temp = requestCounter.incrementAndGet();
        executorService.schedule(requestCounter::decrementAndGet, delay.toMillis() * temp, TimeUnit.MICROSECONDS);
        if (temp > requestLimit) {
            //System.out.println("Поток №" + i + " остановлен count = " + temp);
            Thread.sleep(delay.toMillis() * temp);
        }
            System.out.println("Поток №" + i + " выполнен с задержкой " +
                    (((System.currentTimeMillis() - start) / 1000 + "\t" + (System.currentTimeMillis() - start))));
    }
}
