package com.example.Attendance.Service.service;

public class AttendanceNotFoundException
        extends RuntimeException {

    public AttendanceNotFoundException(String message) {
        super(message);
    }
}