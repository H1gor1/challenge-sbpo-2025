package org.sbpo2025.challenge.ThreadPoolController;

import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ThreadPool {

    private final ThreadPoolExecutor executor;
    private LinkedList<Future<?>> futures = new LinkedList<>();

    public ThreadPool(int nThreads) {
        executor = new ThreadPoolExecutor(
            nThreads,
            nThreads,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>()
        );
    }

    public void submit(Runnable task) {
        Future<?> future = executor.submit(task);
        futures.add(future);
    }

    public void waitAll() {
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        futures = new LinkedList<>();
    }
    public void close() {
        executor.shutdownNow();
    }
}
