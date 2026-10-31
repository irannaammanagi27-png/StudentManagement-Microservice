package com.example.Attendance.Service.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Attendance.Service.entity.Attendance;


public interface AttendanceRepository
        extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudentId(Long studentId);

    List<Attendance> findByStudentIdAndAttendanceDateBetween(
            Long studentId, LocalDate startDate, LocalDate endDate);

    Optional<Attendance> findByStudentIdAndAttendanceDate(
            Long studentId, LocalDate attendanceDate);

    long countByStudentIdAndStatus(
            Long studentId, Attendance.Status status);

    boolean existsByStudentIdAndAttendanceDate(
            Long studentId, LocalDate attendanceDate);

    boolean existsByStudentIdAndAttendanceDateAndIdNot(
            Long studentId, LocalDate attendanceDate, Long id);
}