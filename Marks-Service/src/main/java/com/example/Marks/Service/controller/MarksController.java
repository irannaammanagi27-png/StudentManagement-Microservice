package com.example.Marks.Service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Marks.Service.entity.Marks;
import com.example.Marks.Service.service.MarksService;
import com.example.Marks.Service.email.MarksEmailService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/marks")
public class MarksController {

    private final MarksService marksService;
    private final MarksEmailService marksEmailService;

    public MarksController(
            MarksService marksService,
            MarksEmailService marksEmailService) {

        this.marksService = marksService;
        this.marksEmailService = marksEmailService;
    }

    @PostMapping
    public ResponseEntity<Marks> addMarks(
            @Valid @RequestBody Marks marks,
            @RequestHeader("Authorization") String authorization) {

        Marks savedMarks = marksService.addMarks(marks);

        marksEmailService.sendMarksNotification(
                savedMarks, authorization);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedMarks);
    }

    @GetMapping
    public ResponseEntity<List<Marks>> getAllMarks() {

        return ResponseEntity.ok(
                marksService.getAllMarks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Marks> getMarksById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                marksService.getMarksById(id));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Marks>> getMarksByStudentId(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                marksService.getMarksByStudentId(studentId));
    }

    @GetMapping("/student/{studentId}/semester/{semester}")
    public ResponseEntity<List<Marks>> getMarksByStudentAndSemester(
            @PathVariable Long studentId,
            @PathVariable Integer semester) {

        return ResponseEntity.ok(
                marksService.getMarksByStudentAndSemester(
                        studentId, semester));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Marks> updateMarks(
            @PathVariable Long id,
            @Valid @RequestBody Marks marks,
            @RequestHeader("Authorization") String authorization) {

        Marks updatedMarks =
                marksService.updateMarks(id, marks);

        marksEmailService.sendMarksNotification(
                updatedMarks, authorization);

        return ResponseEntity.ok(updatedMarks);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMarks(
            @PathVariable Long id) {

        marksService.deleteMarks(id);

        return ResponseEntity.noContent().build();
    }
}