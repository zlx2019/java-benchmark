package com.zero.example;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * disruptor 示例
 *
 * @author Zero.
 * <p> Created on 2025/8/14 17:09 </p>
 */
public class DisruptorExample {

    public static void main(String[] args) throws InterruptedException, InsufficientCapacityException {
        // Disruptor 环形数组大小
        int bufferSize = 8;
        // 创建队列
        Disruptor<UserEvent> disruptor = new Disruptor<>(
                UserEvent::new,
                bufferSize,
                Executors.defaultThreadFactory(),
                ProducerType.MULTI,
                new BlockingWaitStrategy()
        );
        // 注册消费处理器
        // disruptor.handleEventsWith(new UserEventHandler());
        disruptor.handleEventsWithWorkerPool(new UserEventWorkHandler(), new UserEventWorkHandler(), new UserEventWorkHandler());
        // 启动队列
        disruptor.start();

        // 获取队列缓冲区
        RingBuffer<UserEvent> ringBuffer = disruptor.getRingBuffer();
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putLong(System.currentTimeMillis());

        // mark 第一种发布事件方式
        // 手动获取序号，发布
        long seq = ringBuffer.next(); // 获取下一个可用序号
        UserEvent event = ringBuffer.get(seq); // 获取该序号事件
        event.setUserId(buf.getLong(0));
        ringBuffer.publish(seq);    // 发布事件

        TimeUnit.SECONDS.sleep(1);

        // mark 第二种, 通过转换器
        buf.flip();
        buf.putLong(System.currentTimeMillis());
        EventTranslatorOneArg<UserEvent, Long> TRANSLATOR = (e, sequence, userId) -> e.setUserId(userId);
        ringBuffer.publishEvent(TRANSLATOR, buf.getLong(0));

        TimeUnit.SECONDS.sleep(1);

        // mark 第三种 非阻塞式发布事件, 如果没有多余的空间，则返回false
        buf.flip();
        buf.putLong(System.currentTimeMillis());
        ringBuffer.tryPublishEvent(TRANSLATOR, buf.getLong(0));

        buf.clear();
        Thread.sleep(3000);
        disruptor.shutdown();
    }

    /**
     * 用户事件, 作为队列中的数据元素
     */
    @Data
    static class UserEvent {
        private Long userId;
    }

    /**
     * 用户事件工厂, 用于构建事件对象, 也可以是方法引用, 比如 UserEvent::new
     */
    static class UserEventFactory implements EventFactory<UserEvent> {
        @Override
        public UserEvent newInstance() {
            return new UserEvent();
        }
    }

    /**
     * 事件消费处理器
     */
    @Slf4j
    static class UserEventHandler implements EventHandler<UserEvent> {
        /**
         * @param userEvent  消费事件
         * @param seq        事件序号
         * @param endOfBatch 该事件是否为这一批事件的最后一个事件，一般用于事件批优化. 比如不是最后一个事件的保存起来，直到收集完最后一个事件，然后统一处理.
         */
        @Override
        public void onEvent(UserEvent userEvent, long seq, boolean endOfBatch) throws Exception {
            log.info("USER-EVENT: {}", userEvent);

        }
    }

    /**
     * 事件消费处理器, 单消费多线程处理
     */
    @Slf4j
    static class UserEventWorkHandler implements WorkHandler<UserEvent> {
        @Override
        public void onEvent(UserEvent userEvent) throws Exception {
            log.info("USER-EVENT: {}", userEvent);
            throw new RuntimeException("模拟的异常!!!");
        }
    }

    /**
     * 异常处理器
     */
    @Slf4j
    static class UserEventExceptionHandler implements ExceptionHandler<UserEvent> {
        @Override
        public void handleEventException(Throwable e, long sequence, UserEvent event) {
            log.error("处理事件时发生异常, seq: {}, event: {}", sequence, event, e);
        }

        @Override
        public void handleOnStartException(Throwable ex) {
            log.error("启动处理器时发生异常: {}", ex.getMessage(), ex);
        }
        @Override
        public void handleOnShutdownException(Throwable ex) {
            log.error("关闭处理器时发生异常: {}", ex.getMessage(), ex);
        }
    }
}
