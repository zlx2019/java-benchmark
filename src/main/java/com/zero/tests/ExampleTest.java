package com.zero.tests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Zero.
 * <p> Created on 2025/8/14 12:43 </p>
 */
@Slf4j
public class ExampleTest {
    @BeforeAll
    public static void beforeAll() {
        log.info("开始所有测试之前执行");
    }

    @AfterAll
    public static void afterAll() {
        log.info("所有测试结束之后执行");
    }

    @Test
    public void test1() {
        log.info("test1");
    }
}
