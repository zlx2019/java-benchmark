package com.zero.example;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
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
        int bufferSize = 2;
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
//        ByteBuffer buf = ByteBuffer.allocate(8);
//        buf.putLong(System.currentTimeMillis());

        // mark 第一种发布事件方式
        publish(ringBuffer, System.currentTimeMillis());

        TimeUnit.SECONDS.sleep(1);

        // mark 第二种, 通过转换器
        publishEvent(ringBuffer, System.currentTimeMillis());

        TimeUnit.SECONDS.sleep(3);

//        publishEvent(ringBuffer, System.currentTimeMillis());

        // mark 批量发布
        batchPublish(ringBuffer, List.of(1001L, 2001L));

        // 非阻塞式批量发布，失败则false
        boolean tried = tryBatchPublish(ringBuffer, List.of(1002L, 2002L, 3002L));
        System.out.println(tried);
//
//        TimeUnit.SECONDS.sleep(3);
//        batchPublish(ringBuffer, List.of(4001L, 5001L, 6001L));


//        buf.flip();
//        buf.putLong(System.currentTimeMillis());
//        EventTranslatorOneArg<UserEvent, Long> TRANSLATOR = (e, sequence, userId) -> e.setUserId(userId);
//        ringBuffer.publishEvent(TRANSLATOR, buf.getLong(0));
//
////        TimeUnit.SECONDS.sleep(1);
//
//        // mark 第三种 非阻塞式发布事件, 如果没有多余的空间，则返回false
//        buf.flip();
//        buf.putLong(System.currentTimeMillis());
////        boolean b = ringBuffer.tryPublishEvent(TRANSLATOR, buf.getLong(0));
////        System.out.println(b);
//
//        buf.clear();
        Thread.sleep(3000);
        disruptor.shutdown();
    }

    /// 转换器
    private static final EventTranslatorOneArg<UserEvent,Long> TRANSLATOR = (event, seq,userId) -> {
        event.setUserId(userId);
    };

    /**
     * 手动发布事件
     */
    static void publish(RingBuffer<UserEvent> buffer, long userId) {
        // 获取下一个可用的序号
        long seq = buffer.next();
        try {
            UserEvent event = buffer.get(seq); // 获取序号元素
            event.setUserId(userId); // 设置元素值
        }finally {
            // 无论如何, 都要将这个序号发布出去，否则整个队列都会阻塞.
            buffer.publish(seq);
        }
    }

    /**
     * 通过转换器发布事件, 更安全由 Disruptor sdk 封装.
     */
    static void publishEvent(RingBuffer<UserEvent> buffer, long userId) {
        buffer.publishEvent(TRANSLATOR, userId);
    }

    /**
     * 非阻塞时发布事件, 发布失败则返回false
     */
    private static boolean tryPublish(RingBuffer<UserEvent> ringBuffer, Long userId) {
        try {
            long seq = ringBuffer.tryNext();
            try {
                UserEvent event = ringBuffer.get(seq);
                event.setUserId(userId);
            }finally {
                ringBuffer.publish(seq);
            }
            return true;
        } catch (InsufficientCapacityException e) {
            return false;
        }
    }

    /**
     * 非阻塞发布, 发布失败直接返回 false
     */
    private static boolean tryPublishEvent(RingBuffer<UserEvent> ringBuffer, Long userId) {
        return ringBuffer.tryPublishEvent(TRANSLATOR, userId);
    }

    /**
     * 批量发布事件
     */
    private static void batchPublish(RingBuffer<UserEvent> ringBuffer, List<Long> userIds) {
        int size = userIds.size();
        // 批量获取可用多个序号
        long endSeq = ringBuffer.next(size); // 获取到的结尾序号
        long startSeq = endSeq - (size - 1); // 获取到的起始序号
        try {
            // 填充元素值
            for (int i = 0; i < size; i++) {
                long seq = startSeq + i;
                UserEvent event = ringBuffer.get(seq);
                event.setUserId(userIds.get(i));
            }
        }finally {
            // 必须保证这一批序号发布
            ringBuffer.publish(startSeq, endSeq);
        }
    }

    /**
     * 通过转换器批量发布事件
     */
    static void batchPublishEvent(RingBuffer<UserEvent> ringBuffer, List<Long> userIds) {
        ringBuffer.publishEvents(TRANSLATOR, userIds.toArray(new Long[0]));
    }

    /**
     * 非阻塞式批量发布事件
     */
    static boolean tryBatchPublish(RingBuffer<UserEvent> ringBuffer, List<Long> userIds) {
        int size = userIds.size();
        try {
            // 批量获取可用多个序号
            long endSeq = ringBuffer.tryNext(size); // 获取到的结尾序号
            long startSeq = endSeq - (size - 1); // 获取到的起始序号
            try {
                // 填充元素值
                for (int i = 0; i < size; i++) {
                    long seq = startSeq + i;
                    UserEvent event = ringBuffer.get(seq);
                    event.setUserId(userIds.get(i));
                }
                return true;
            }finally {
                ringBuffer.publish(startSeq, endSeq);
            }

        } catch (InsufficientCapacityException e) {
            return false;
        }
    }

    /**
     * 非阻塞式批量发布事件
     */
    static boolean tryBatchPublishEvent(RingBuffer<UserEvent> ringBuffer, List<Long> userIds) {
         return ringBuffer.tryPublishEvents(TRANSLATOR, userIds.toArray(new Long[0]));
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
