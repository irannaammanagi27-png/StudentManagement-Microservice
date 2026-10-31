package com.example.Attendance.Service.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Attendance.Service.entity.Attendance;
import com.example.Attendance.Service.repository.AttendanceRepository;


@Service
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceService(
            AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public Attendance addAttendance(Attendance attendance) {
        validateAttendance(attendance);

        boolean duplicate = attendanceRepository
                .existsByStudentIdAndAttendanceDate(
                        attendance.getStudentId(),
                        attendance.getAttendanceDate());

        if (duplicate) {
            throw new IllegalArgumentException(
                    "Attendance already exists for this student on this date");
        }

        return attendanceRepository.save(attendance);
    }

    @Transactional(readOnly = true)
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Attendance getAttendanceById(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() ->
                    new AttendanceNotFoundException(
                        "Attendance not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceByStudentId(
            Long studentId) {
        validateStudentId(studentId);
        return attendanceRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceByDateRange(
            Long studentId, LocalDate startDate, LocalDate endDate) {

        validateStudentId(studentId);

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date must not be after end date");
        }

        return attendanceRepository
                .findByStudentIdAndAttendanceDateBetween(
                        studentId, startDate, endDate);
    }

    public Attendance updateAttendance(
            Long id, Attendance updatedAttendance) {

        Attendance existing = getAttendanceById(id);
        validateAttendance(updatedAttendance);

        boolean duplicate = attendanceRepository
                .existsByStudentIdAndAttendanceDateAndIdNot(
                        updatedAttendance.getStudentId(),
                        updatedAttendance.getAttendanceDate(), id);

        if (duplicate) {
            throw new IllegalArgumentException(
                    "Attendance already exists for this student on this date");
        }

        existing.setStudentId(updatedAttendance.getStudentId());
        existing.setAttendanceDate(
                updatedAttendance.getAttendanceDate());
        existing.setStatus(updatedAttendance.getStatus());
        existing.setRemarks(updatedAttendance.getRemarks());

        return attendanceRepository.save(existing);
    }

    public void deleteAttendance(Long id) {
        Attendance attendance = getAttendanceById(id);
        attendanceRepository.delete(attendance);
    }

    @Transactional(readOnly = true)
    public double getAttendancePercentage(Long studentId) {
        validateStudentId(studentId);

        long present = attendanceRepository.countByStudentIdAndStatus(
                studentId, Attendance.Status.PRESENT);

        long absent = attendanceRepository.countByStudentIdAndStatus(
                studentId, Attendance.Status.ABSENT);

        long total = present + absent;

        if (total == 0) {
            return 0.0;
        }

        return Math.round((present * 10000.0) / total) / 100.0;
    }

    private void validateStudentId(Long studentId) {
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException(
                    "Student ID must be positive");
        }
    }

    private void validateAttendance(Attendance attendance) {
        validateStudentId(attendance.getStudentId());

        if (attendance.getAttendanceDate() == null) {
            throw new IllegalArgumentException(
                    "Attendance date is required");
        }

        if (attendance.getAttendanceDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Attendance date cannot be in the future");
        }

        if (attendance.getStatus() == null) {
            throw new IllegalArgumentException(
                    "Attendance status is required");
        }
    }
}