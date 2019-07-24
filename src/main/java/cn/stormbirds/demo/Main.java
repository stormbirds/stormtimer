package cn.stormbirds.demo;

import cn.stormbirds.stormtimer.HashedWheelTimer;
import cn.stormbirds.stormtimer.Timeout;
import cn.stormbirds.stormtimer.TimerTask;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        HashedWheelTimer timer = new HashedWheelTimer();
        HashedWheelTimer timer1 = new HashedWheelTimer(1,TimeUnit.SECONDS,256);
        System.out.print(LocalDateTime.now()+" 任务开始\n");
        timer1.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                System.out.print(LocalDateTime.now());
                System.out.print("  timer1\n");
                System.out.print("timer1 剩余任务数："+ timer1.pendingTimeouts());
                System.out.print("\n");
                if(timer1.pendingTimeouts()<=0){
                    timer1.stop();
                    System.out.print("timer1 结束");
                }
            }
        },2,TimeUnit.SECONDS);
        timer1.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                System.out.print(LocalDateTime.now());
                System.out.print("  timer1\n");
                System.out.print("timer1 剩余任务数："+ timer1.pendingTimeouts());
                System.out.print("\n");

            }
        },4,TimeUnit.SECONDS);
        hugerTimer(timer,1);
        hugerTimer(timer,3);
        hugerTimer(timer,5);
        System.out.print(" 结束");
        if(timer1.pendingTimeouts()<=0){
            timer1.stop();
            System.out.print("timer1 结束");
        }

        if(timer.pendingTimeouts()<=0){
            timer.stop();
            System.out.print("timer 结束");
        }
    }

    public static void hugerTimer(HashedWheelTimer timer,int i){
        Random random = new Random();
        timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                System.out.print(LocalDateTime.now());
                System.out.print("  timer\n");
                System.out.print("timer 剩余任务数："+ timer.pendingTimeouts());
                System.out.print("\n");

                /* 不要在定时任务中运行耗时任务，这样会影响后面的任务
                if(random.nextBoolean()){
                    Thread.sleep(2000);
                    System.out.print("延迟2秒 \n");
                }
                */
            }
        },i, TimeUnit.SECONDS);
    }
}
