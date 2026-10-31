package com.example.Course.Service.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Course.Service.entity.Course;


public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<Course> findByCodeIgnoreCase(String code);

    List<Course> findByNameContainingIgnoreCase(String name);

    List<Course> findByDepartmentIgnoreCase(String department);
}
