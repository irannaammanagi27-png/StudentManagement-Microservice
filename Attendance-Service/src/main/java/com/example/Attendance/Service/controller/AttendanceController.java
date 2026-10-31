
package com.example.Attendance.Service.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Attendance.Service.entity.Attendance;
import com.example.Attendance.Service.service.AttendanceService;
import com.example.Attendance.Service.email.AttendanceEmailService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceEmailService attendanceEmailService;

    public AttendanceController(
            AttendanceService attendanceService,
            AttendanceEmailService attendanceEmailService) {

        this.attendanceService = attendanceService;
        this.attendanceEmailService = attendanceEmailService;
    }

    @PostMapping
    public ResponseEntity<Attendance> addAttendance(
            @Valid @RequestBody Attendance attendance,
            @RequestHeader("Authorization") String authorization) {

        Attendance savedAttendance =
                attendanceService.addAttendance(attendance);

        attendanceEmailService.sendAttendanceNotification(
                savedAttendance, authorization);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedAttendance);
    }

    @GetMapping
    public ResponseEntity<List<Attendance>> getAllAttendance() {

        return ResponseEntity.ok(
                attendanceService.getAllAttendance());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Attendance> getAttendanceById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                attendanceService.getAttendanceById(id));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Attendance>> getAttendanceByStudent(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                attendanceService.getAttendanceByStudentId(studentId));
    }

    @GetMapping("/student/{studentId}/range")
    public ResponseEntity<List<Attendance>> getAttendanceByRange(
            @PathVariable Long studentId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return ResponseEntity.ok(
                attendanceService.getAttendanceByDateRange(
                        studentId, startDate, endDate));
    }

    @GetMapping("/student/{studentId}/percentage")
    public ResponseEntity<Map<String, Object>> getPercentage(
            @PathVariable Long studentId) {

        double percentage = attendanceService
                .getAttendancePercentage(studentId);

        return ResponseEntity.ok(Map.of(
                "studentId", studentId,
                "attendancePercentage", percentage));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Attendance> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody Attendance attendance,
            @RequestHeader("Authorization") String authorization) {

        Attendance updatedAttendance =
                attendanceService.updateAttendance(id, attendance);

        attendanceEmailService.sendAttendanceNotification(
                updatedAttendance, authorization);

        return ResponseEntity.ok(updatedAttendance);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttendance(
            @PathVariable Long id) {

        attendanceService.deleteAttendance(id);

        return ResponseEntity.noContent().build();
    }
}
