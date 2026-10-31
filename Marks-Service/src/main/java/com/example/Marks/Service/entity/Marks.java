package com.example.Marks.Service.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "marks")
public class Marks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Student ID is required")
    @Positive(message = "Student ID must be positive")
    @Column(nullable = false)
    private Long studentId;

    @NotBlank(message = "Subject is required")
    @Column(nullable = false)
    private String subject;

    @NotNull(message = "Semester is required")
    @Positive(message = "Semester must be positive")
    @Column(nullable = false)
    private Integer semester;

    @NotNull(message = "Marks obtained is required")
    @DecimalMin(value = "0.0", message = "Marks cannot be negative")
    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal marksObtained;

    @NotNull(message = "Maximum marks is required")
    @DecimalMin(value = "0.01", message = "Maximum marks must be greater than zero")
    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal maximumMarks;

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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public BigDecimal getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(BigDecimal marksObtained) {
        this.marksObtained = marksObtained;
    }

    public BigDecimal getMaximumMarks() {
        return maximumMarks;
    }

    public void setMaximumMarks(BigDecimal maximumMarks) {
        this.maximumMarks = maximumMarks;
    }

    @Transient
    public BigDecimal getPercentage() {
        if (marksObtained == null || maximumMarks == null
                || maximumMarks.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return marksObtained.multiply(BigDecimal.valueOf(100))
                .divide(maximumMarks, 2, RoundingMode.HALF_UP);
    }

    @Transient
    public String getGrade() {
        BigDecimal percentage = getPercentage();

        if (percentage == null) {
            return null;
        }

        if (percentage.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return "A+";
        } else if (percentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "A";
        } else if (percentage.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return "B";
        } else if (percentage.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "C";
        } else if (percentage.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return "D";
        } else {
            return "F";
        }
    }
}