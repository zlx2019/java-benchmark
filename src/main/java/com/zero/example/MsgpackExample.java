package com.zero.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.zero.benchmark.pojo.ComplexEntity;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 *
 * @author Zero.
 * <p> Created on 2025/8/14 12:34 </p>
 */
public class MsgpackExample {
    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new MessagePackFactory());
        JavaTimeModule timeModule = new JavaTimeModule();
        timeModule.addSerializer(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        timeModule.addSerializer(new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        mapper.registerModule(timeModule);
        var entity = new ComplexEntity(
                1,
                98.5,
                true,
                'A',
                "Alice",
                new BigDecimal("123425.67"),
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
        byte[] bytes = mapper.writeValueAsBytes(entity);
        ComplexEntity value = mapper.readValue(bytes, ComplexEntity.class);
        System.out.println(value);
    }
}
