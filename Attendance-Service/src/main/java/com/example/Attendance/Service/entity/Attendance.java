package com.example.Attendance.Service.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
    name = "attendance",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_attendance_student_date",
        columnNames = {"student_id", "attendance_date"}
    )
)
public class Attendance {

    public enum Status {
        PRESENT, ABSENT, LEAVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Student ID is required")
    @Positive(message = "Student ID must be positive")
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @NotNull(message = "Attendance date is required")
    @PastOrPresent(message = "Attendance date cannot be in the future")
    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @NotNull(message = "Attendance status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private String remarks;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Transient
    public boolean isPresent() {
        return status == Status.PRESENT;
    }
}