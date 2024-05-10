package com.example.demo;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class Solution {
    public static void main(String[] args) {
//        Executors.newSingleThreadExecutor();
//        Executors.newFixedThreadPool(1);
//        // 可以缓存线程的线程池
//        // 有多少个任务，就创建多少个线程来处理
//        Executors.newCachedThreadPool();
//        Executors.newScheduledThreadPool(2);


        // 通过阻塞队列的方式，来实现线程池中线程的复用

    }

    void linkedBlockQueueTest() {
        Integer[] arr = new Integer[4];
        LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<>(3);
        queue.offer(1);
        queue.toArray(arr);
        System.out.println("插入1后：size=" + queue.size() + Arrays.toString(arr));
        queue.offer(2);
        queue.toArray(arr);

        System.out.println("插入2后：size=" + queue.size() + Arrays.toString(arr));
        queue.offer(3);
        queue.toArray(arr);

        System.out.println("插入3后：size=" + queue.size() + Arrays.toString(arr));
        queue.offer(4);
        queue.toArray(arr);

        System.out.println("插入4后：size=" + queue.size() + Arrays.toString(arr));

    }
}
