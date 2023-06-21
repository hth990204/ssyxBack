package com.atguigu.ssyx;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Demo4 {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        // 线程1执行返回的结果：100
        CompletableFuture<Integer> futureA =
                CompletableFuture.supplyAsync(() -> {
                    int res = 1024;
                    System.out.println("线程一："+res);
                    return res;
                }, executorService);

        // 线程2 获取到线程1执行的结果
        CompletableFuture<Integer> futureB = futureA.thenApplyAsync((res)->{
            System.out.println("线程二--"+res);
            return ++res;
        },executorService);

        //线程3: 无法获取futureA返回结果
        CompletableFuture<Void> futureC = futureA.thenRunAsync(() -> {
            System.out.println("线程三....");
        }, executorService);
    }
}