package com.example.demo.Log;

import lombok.Getter;

@Getter
public class LogResult {
    private final boolean success;
    private final String errorMessage;

    public LogResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

}