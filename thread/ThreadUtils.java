package com.hywa.pricepublish.common.utils.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ThreadUtils {

    /**
     * 执行批量操作
     */
    public static void batchExec(int count, List<?> list, String methodName, Object... objs) throws InterruptedException {
        //数据集合大小
        int listSize = list.size();
        //开启的线程数
        int runSize = (listSize / count) + 1;
        //存放每个线程的执行数据
        List<?> newlist = null;
        //线程因子
        int def = 5;
        //创建一个线程池，数量和开启线程的数量一样
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("thread-utils-pool").build();
        ExecutorService executor = new ThreadPoolExecutor(runSize, runSize + def,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());//创建两个个计数器
        CountDownLatch begin = new CountDownLatch(1);
        CountDownLatch end = new CountDownLatch(runSize);
        //循环创建线程
        for (int i = 0; i < runSize; i++) {
            //计算每个线程执行的数据
            if ((i + 1) == runSize) {
                int startIndex = (i * count);
                int endIndex = list.size();
                newlist = list.subList(startIndex, endIndex);
            } else {
                int startIndex = (i * count);
                int endIndex = (i + 1) * count;
                newlist = list.subList(startIndex, endIndex);
            }
            //线程类(通过executor.execute(()->)隐式启动线程)
            List<?> finalNewlist = newlist;

            executor.execute(() -> {
                try {
                    //这里还要说一下，由于在实质项目中，当处理的数据存在等待超时和出错会使线程一直处于等待状态
                    //此处进行实际的业务操作
                    //通过反射调用不同的方法(传入方法名和集合及对应的特定参数)
                    invoke(methodName, finalNewlist, objs);
                    //执行完让线程直接进入等待
                    begin.await();
                } catch (InstantiationException | InvocationTargetException |
                        InterruptedException | NoSuchMethodException | IllegalAccessException
                        | ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    //这里需注意，当一个线程执行完 了计数要减一不然这个线程会被一直挂起
                    //end.countDown()，这个方法就是直接把计数器减一的
                    end.countDown();
                }
            });
        }

        begin.countDown();
        end.await();

        //执行完关闭线程池
        executor.shutdown();
    }


    /**
     * 第一个参数为list,后面的参数都是作为方法中的特定参数
     */
    private static void invoke(String flag, Object... objs) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        Class<?> clazz = Class.forName("com.hywa.pricepublish.common.utils.thread.Methods");
        Method method = Methods.class.getMethod(flag, Object[].class);
        method.invoke(clazz.newInstance(), (Object) objs);
    }

    /**
     * 测试
     */
    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        //数据越大线程越多
        for (int i = 0; i < 1000; i++) {
            list.add("hello" + i);
        }
        try {
            batchExec(100, list, "thread");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
