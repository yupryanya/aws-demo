package com.task11.models;

import lombok.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class RouteKey {
    private final String method;
    private final String path;

    public RouteKey(String method, String path) {
        String template = "^/[a-zA-Z]+/\\d+$";
        Pattern pattern = Pattern.compile(template);
        Matcher matcher = pattern.matcher(path);
        if (matcher.matches()) {
            this.path = path.replaceAll("/\\d+$", "/{id}");
        } else {
            this.path = path;
        }
        this.method = method;
    }
}