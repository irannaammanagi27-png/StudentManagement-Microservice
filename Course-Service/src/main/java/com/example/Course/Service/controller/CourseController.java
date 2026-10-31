package com.example.Course.Service.controller;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Course.Service.entity.Course;
import com.example.Course.Service.service.CourseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<Course> addCourse(
            @Valid @RequestBody Course course) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.addCourse(course));
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(
            @PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody Course course) {
        return ResponseEntity.ok(courseService.updateCourse(id, course));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Course>> searchCourses(
            @RequestParam String name) {
        return ResponseEntity.ok(courseService.searchCourses(name));
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<List<Course>> getCoursesByDepartment(
            @PathVariable String department) {
        return ResponseEntity.ok(
                courseService.getCoursesByDepartment(department));
    }
}
