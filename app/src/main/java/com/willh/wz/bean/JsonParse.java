package com.willh.wz.bean;

public interface JsonParse<T> {
    void parse(String json);

    String toJson();
}
