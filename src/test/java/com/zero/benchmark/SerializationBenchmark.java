package com.zero.benchmark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.zero.pojo.ComplexEntity;
import org.apache.fory.Fory;
import org.apache.fory.config.Language;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * @author Zero.
 * <p> Created on 2025/8/13 22:25 </p>
 */
@Fork(1) // 启动一个 JVM 进程
@Threads(4) // 启动 4个 线程
@BenchmarkMode(Mode.Throughput) // 每秒操作数
@OutputTimeUnit(TimeUnit.SECONDS) // 输出单位
@State(Scope.Benchmark) // 基准测试范围
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS) // 预热
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS) // 测量
public class SerializationBenchmark {
    private ObjectMapper jacksonMapper;
    private ObjectMapper msgpackMapper;
    private Fory fory;
    private ComplexEntity entity;
    private byte[] buffer;

    /**
     * 初始化设置操作
     */
    @Setup
    public void setup() throws JsonProcessingException {
        jacksonMapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        timeModule.addSerializer(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        timeModule.addSerializer(new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        jacksonMapper.registerModule(timeModule);

        msgpackMapper = new ObjectMapper(new MessagePackFactory());
        msgpackMapper.registerModule(timeModule);

        fory = Fory.builder().withLanguage(Language.JAVA).requireClassRegistration(false).build();
        fory.register(ComplexEntity.class);

        entity = new ComplexEntity(
                1,
                98.5,
                true,
                'A',
                "Alice",
                new BigDecimal("12345.67"),
                Arrays.asList("VIP", "Premium"),
                new HashSet<>(Arrays.asList(10, 20, 30)),
                new HashMap<>() {{
                    put("email", "alice@example.com");
                    put("phone", "1234567890");
                }},
                new int[]{90, 85, 88},
                new String[]{"Ali", "A"},
                LocalDate.of(1995, 5, 20),
                LocalDateTime.now()
        );

        byte[] jacksonBytes = jacksonMapper.writeValueAsBytes(entity);
        System.out.println("jackson to bytes " + jacksonBytes.length);
        byte[] msgpackBytes = msgpackMapper.writeValueAsBytes(entity);
        System.out.println("msgpack to bytes " + msgpackBytes.length);
//        byte[] foryBytes = fory.serializeJavaObject(entity);
//        System.out.println("fory to bytes " + foryBytes.length);
    }

    @Benchmark
    public void jacksonSerialization(Blackhole blackhole) throws JsonProcessingException {
        buffer = jacksonMapper.writeValueAsBytes(entity);
        blackhole.consume(buffer);
    }

    @Benchmark
    public void msgpackSerialization(Blackhole blackhole) throws JsonProcessingException {
        buffer = msgpackMapper.writeValueAsBytes(entity);
        blackhole.consume(buffer);
    }

//    @Benchmark
    public void fastJsonSerialization(Blackhole blackhole) throws JsonProcessingException {

    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                // 指定要运行的基准测试类
                .include(SerializationBenchmark.class.getSimpleName())
                .result("result.json")
                .resultFormat(ResultFormatType.JSON)
                // 可以添加其他配置，例如运行模式、线程数等
                .build();
        new Runner(opt).run();
    }

}
