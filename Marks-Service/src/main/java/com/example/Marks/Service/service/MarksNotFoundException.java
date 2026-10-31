package com.example.Marks.Service.service;

public class MarksNotFoundException extends RuntimeException {

    public MarksNotFoundException(String message) {
        super(message);
    }
}