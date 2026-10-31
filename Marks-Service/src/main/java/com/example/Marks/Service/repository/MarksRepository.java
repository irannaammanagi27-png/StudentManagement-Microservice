package com.example.Marks.Service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Marks.Service.entity.Marks;


public interface MarksRepository extends JpaRepository<Marks, Long> {

    List<Marks> findByStudentId(Long studentId);

    List<Marks> findByStudentIdAndSemester(
            Long studentId, Integer semester);

    boolean existsByStudentIdAndSubjectIgnoreCaseAndSemester(
            Long studentId, String subject, Integer semester);

    boolean existsByStudentIdAndSubjectIgnoreCaseAndSemesterAndIdNot(
            Long studentId, String subject, Integer semester, Long id);
}