package com.zero.benchmark;

import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Java 基准测试示例
 *
 * @BenchmarkMode 测量模式
 *  - Mode.Throughput：吞吐量模式，测量单位时间内的操作次数
 *  - Mode.AverageTime：平均时间模式，测量每次操作的平均时间
 *  - Mode.SampleTime：采样时间模式，采样每次操作的时间
 *  - Mode.SingleShotTime：单次执行时间模式
 *  - Mode.All：所有模式
 * @OutputTimeUnit 输出的时间单位
 * @State 状态对象的作用范围
 *  - Scope.Benchmark：所有线程共享同一个实例
 *  - Scope.Group：同一个组内的线程共享实例
 *  - Scope.Thread：每个线程都有自己的实例
 *
 * @Warmup 预热配置, JIT优化
 *  - iterations：预热的次数
 *  - time: 每次预热的时长
 *  - timeUnit: 预热时长单位
 *  - batchSize: 批处理大小，每次操作调用几次方法
 * @Measurement 实际测试配置, 与预热配置相同
 * @Fork fork 启动的进程数
 * @Threads 每个进程的线程数
 *
 *
 *
 *
 * @author Zero.
 * <p> Created on 2025/8/14 12:53 </p>
 */
@BenchmarkMode(Mode.AverageTime) // 测量模式
@OutputTimeUnit(TimeUnit.NANOSECONDS) // 输出时间单位
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)    // 预热配置
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS) // 实际测试配置
@Fork(1)
@Threads(4)
@Slf4j
public class BenchmarkExample {
    @Setup
    public void setup() {
        log.info("基准测试之前执行...");
    }
    @TearDown
    public void tearDown() {
        log.info("基准测试之后执行...");
    }
    @Benchmark
    public void benchmark1() {
        System.out.println("benchmark1");
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                // 指定要运行的基准测试类
                .include(BenchmarkExample.class.getSimpleName())
                .result("result.json")
                .resultFormat(ResultFormatType.JSON)
                // 可以添加其他配置，例如运行模式、线程数等
                .build();
        new Runner(opt).run();
    }
}
