package com.llw.java;

import java.time.format.DateTimeFormatter;

public class DataFormatterTest {

    public static void main(String[] args) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss.S");
        System.out.println(dateTimeFormatter.parse("2010-1-1 12:12:12.111"));
    }
}
