package com.zero.benchmark.pojo;

import lombok.Data;
import org.apache.fory.Fory;
import org.apache.fory.config.Language;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Zero.
 * <p> Created on 2025/8/13 22:55 </p>
 */
@Data
public class SimpleEntity implements Serializable {
    private Long id;
    private String name;
    private Integer age;
    private String address;

    public static void main(String[] args) {
        SimpleEntity simple = new SimpleEntity();
        simple.setId(1L);
        simple.setName("<UNK>");
        simple.setAge(1);
        simple.setAddress("<UNK>");
        Fory fory = Fory.builder().withLanguage(Language.JAVA).requireClassRegistration(false)
                .build();
        fory.register(SimpleEntity.class);
        fory.register(ComplexEntity.class);
        byte[] serialize = fory.serializeJavaObject(simple);
        System.out.println(serialize.length);

        var entity = new ComplexEntity(
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
        byte[] bytes = fory.serializeJavaObject(entity);
        System.out.println(bytes.length);
    }

}

