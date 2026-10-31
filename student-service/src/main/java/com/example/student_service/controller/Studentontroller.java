package com.example.student_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.student_service.entity.Student;
import com.example.student_service.service.StudentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/students")
public class Studentontroller {

    private final StudentService studentService;

    public Studentontroller(StudentService studentService) {
        this.studentService = studentService;
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isFaculty(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_FACULTY"));
    }

    private boolean isAdminOrFaculty(Authentication auth) {
        return isAdmin(auth) || isFaculty(auth);
    }

    @PostMapping
    public ResponseEntity<Student> addStudent(
            @Valid @RequestBody Student student,
            Authentication auth) {

        if (!isAdmin(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studentService.addStudent(student));
    }

    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents(
            Authentication auth) {

        if (!isAdminOrFaculty(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(
            @PathVariable Long id,
            Authentication auth) {

        if (isAdminOrFaculty(auth)) {
            return ResponseEntity.ok(
                    studentService.getStudentById(id));
        }

        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {

            Object studentId = auth.getDetails();

            if (studentId == null
                    || !String.valueOf(studentId).equals(String.valueOf(id))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(
                    studentService.getStudentById(id));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody Student student,
            Authentication auth) {

        if (!isAdmin(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                studentService.updateStudent(id, student));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(
            @PathVariable Long id,
            Authentication auth) {

        if (!isAdmin(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        studentService.deleteStudent(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Student>> searchStudents(
            @RequestParam String name,
            Authentication auth) {

        if (!isAdminOrFaculty(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                studentService.searchStudentsByName(name));
    }
}