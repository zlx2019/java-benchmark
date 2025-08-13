package com.zero.benchmark.pojo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Data
public class ComplexEntity implements Serializable {
    // 基本数据类型
    private int id;
    private double score;
    private boolean active;
    private char level;

    // 引用类型
    private String name;
    private BigDecimal balance;

    // 集合类型
    private List<String> tags;
    private Set<Integer> numbers;
    private Map<String, String> attributes;

    // 数组类型
    private int[] scoresArray;
    private String[] aliasArray;

    // 时间类型
    private LocalDate birthday;
    private LocalDateTime lastLogin;
    // 构造函数
    public ComplexEntity(int id, double score, boolean active, char level, String name,
                         BigDecimal balance, List<String> tags, Set<Integer> numbers,
                         Map<String, String> attributes, int[] scoresArray, String[] aliasArray,
                         LocalDate birthday, LocalDateTime lastLogin) {
        this.id = id;
        this.score = score;
        this.active = active;
        this.level = level;
        this.name = name;
        this.balance = balance;
        this.tags = tags;
        this.numbers = numbers;
        this.attributes = attributes;
        this.scoresArray = scoresArray;
        this.aliasArray = aliasArray;
        this.birthday = birthday;
        this.lastLogin = lastLogin;
    }

    // 示例方法
    public static void main(String[] args) {
        ComplexEntity entity = new ComplexEntity(
                1,
                98.5,
                true,
                'A',
                "Alice",
                new BigDecimal("12345.67"),
                Arrays.asList("VIP", "Premium"),
                new HashSet<>(Arrays.asList(10, 20, 30)),
                new HashMap<String, String>() {{
                    put("email", "alice@example.com");
                    put("phone", "1234567890");
                }},
                new int[]{90, 85, 88},
                new String[]{"Ali", "A"},
                LocalDate.of(1995, 5, 20),
                LocalDateTime.now()
        );
        System.out.println(entity);
    }
}
