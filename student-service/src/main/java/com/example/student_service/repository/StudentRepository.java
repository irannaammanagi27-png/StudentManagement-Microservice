package com.example.student_service.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.student_service.entity.Student;


public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByEmailIgnoreCase(String email);

    List<Student> findByNameContainingIgnoreCase(String name);
}
