package com.llw.java;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.Test;

public class CompletableFutureTest {

    @Test
    public void test1() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(
                new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        System.out.println(
                                "executorService 是否为守护线程 :" + Thread.currentThread().isDaemon());
                        return null;
                    }
                });
        final CompletableFuture<String> completableFuture =
                CompletableFuture.supplyAsync(
                        () -> {
                            System.out.println("this is lambda supplyAsync");
                            System.out.println(
                                    "supplyAsync 是否为守护线程 " + Thread.currentThread().isDaemon());
                            try {
                                TimeUnit.SECONDS.sleep(2);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.out.println("this lambda is executed by forkJoinPool");
                            return "result1";
                        });
        final CompletableFuture<String> future =
                CompletableFuture.supplyAsync(
                        () -> {
                            System.out.println("this is task with executor");
                            System.out.println(
                                    "supplyAsync 使用executorService 时是否为守护线程 : "
                                            + Thread.currentThread().isDaemon());
                            return "result2";
                        },
                        executorService);
        // System.out.println(completableFuture.get());
        // System.out.println(future.get());
        Thread.sleep(5 * 1000);
        executorService.shutdown();
    }

    @Test
    public void test2() throws ExecutionException, InterruptedException {

        final CompletableFuture<String> futureOne =
                CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                System.out.println("futureOne InterruptedException");
                            }
                            return "futureOneResult";
                        });
        final CompletableFuture<String> futureTwo =
                CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                Thread.sleep(6000);
                            } catch (InterruptedException e) {
                                System.out.println("futureTwo InterruptedException");
                            }
                            return "futureTwoResult";
                        });
        //        CompletableFuture future = CompletableFuture.allOf(futureOne, futureTwo);
        //        System.out.println(future.get());
        CompletableFuture completableFuture = CompletableFuture.anyOf(futureOne, futureTwo);
        completableFuture.thenAcceptAsync(
                new Consumer() {
                    @Override
                    public void accept(Object o) {
                        System.out.println(o);
                    }
                });
        System.out.println(completableFuture.get());
    }

    @Test
    public void testWhen() throws ExecutionException, InterruptedException {
        CompletableFuture<String> futureOne =
                CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                Thread.sleep(3000);
                                System.out.println(
                                        "completable thread name:"
                                                + Thread.currentThread().getName());
                            } catch (InterruptedException e) {
                                System.out.println("futureOne InterruptedException");
                            }
                            return "futureOneResult";
                        });
        Thread.sleep(1 * 1000);
        futureOne.complete("aabbcc");
        futureOne.whenComplete(
                (s, throwable) -> {
                    System.out.println("when complete:" + s);
                    System.out.println("when thread name:" + Thread.currentThread().getName());
                });

        Thread.sleep(5000);
        System.out.println("main");
    }
}
