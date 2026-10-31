package com.example.Marks.Service.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Marks.Service.entity.Marks;
import com.example.Marks.Service.repository.MarksRepository;


@Service
@Transactional
public class MarksService {

    private final MarksRepository marksRepository;

    public MarksService(MarksRepository marksRepository) {
        this.marksRepository = marksRepository;
    }

    public Marks addMarks(Marks marks) {
        validateMarks(marks);

        boolean duplicate = marksRepository
                .existsByStudentIdAndSubjectIgnoreCaseAndSemester(
                        marks.getStudentId(),
                        marks.getSubject().trim(),
                        marks.getSemester());

        if (duplicate) {
            throw new IllegalArgumentException(
                    "Marks already exist for this student, subject and semester");
        }

        marks.setSubject(marks.getSubject().trim());

        return marksRepository.save(marks);
    }

    @Transactional(readOnly = true)
    public List<Marks> getAllMarks() {
        return marksRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Marks getMarksById(Long id) {
        return marksRepository.findById(id)
                .orElseThrow(() -> new MarksNotFoundException(
                        "Marks record not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Marks> getMarksByStudentId(Long studentId) {
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException(
                    "Student ID must be positive");
        }

        return marksRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<Marks> getMarksByStudentAndSemester(
            Long studentId, Integer semester) {

        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException(
                    "Student ID must be positive");
        }

        if (semester == null || semester <= 0) {
            throw new IllegalArgumentException(
                    "Semester must be positive");
        }

        return marksRepository.findByStudentIdAndSemester(
                studentId, semester);
    }

    public Marks updateMarks(Long id, Marks updatedMarks) {
        Marks existing = getMarksById(id);

        validateMarks(updatedMarks);

        boolean duplicate = marksRepository
                .existsByStudentIdAndSubjectIgnoreCaseAndSemesterAndIdNot(
                        updatedMarks.getStudentId(),
                        updatedMarks.getSubject().trim(),
                        updatedMarks.getSemester(),
                        id);

        if (duplicate) {
            throw new IllegalArgumentException(
                    "Marks already exist for this student, subject and semester");
        }

        existing.setStudentId(updatedMarks.getStudentId());
        existing.setSubject(updatedMarks.getSubject().trim());
        existing.setSemester(updatedMarks.getSemester());
        existing.setMarksObtained(updatedMarks.getMarksObtained());
        existing.setMaximumMarks(updatedMarks.getMaximumMarks());

        return marksRepository.save(existing);
    }

    public void deleteMarks(Long id) {
        Marks marks = getMarksById(id);
        marksRepository.delete(marks);
    }

    private void validateMarks(Marks marks) {
        if (marks.getMarksObtained() == null
                || marks.getMaximumMarks() == null) {
            throw new IllegalArgumentException(
                    "Marks obtained and maximum marks are required");
        }

        if (marks.getMaximumMarks().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Maximum marks must be greater than zero");
        }

        if (marks.getMarksObtained().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Marks obtained cannot be negative");
        }

        if (marks.getMarksObtained()
                .compareTo(marks.getMaximumMarks()) > 0) {
            throw new IllegalArgumentException(
                    "Marks obtained cannot exceed maximum marks");
        }

        if (marks.getSubject() == null
                || marks.getSubject().isBlank()) {
            throw new IllegalArgumentException(
                    "Subject is required");
        }
    }
}