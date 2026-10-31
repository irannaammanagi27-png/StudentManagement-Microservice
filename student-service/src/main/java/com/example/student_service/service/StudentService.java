package com.example.student_service.service;



import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_service.entity.Student;
import com.example.student_service.repository.StudentRepository;



@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student addStudent(Student student) {
        if (studentRepository.existsByEmailIgnoreCase(student.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        return studentRepository.save(student);
    }

    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() ->
                        new StudentNotFoundException("Student not found with ID: " + id));
    }

    public Student updateStudent(Long id, Student updatedStudent) {
        Student existingStudent = getStudentById(id);

        boolean emailUsedByAnotherStudent =
                studentRepository.existsByEmailIgnoreCase(updatedStudent.getEmail())
                && !existingStudent.getEmail().equalsIgnoreCase(updatedStudent.getEmail());

        if (emailUsedByAnotherStudent) {
            throw new IllegalArgumentException("Email already exists");
        }

        existingStudent.setName(updatedStudent.getName());
        existingStudent.setEmail(updatedStudent.getEmail());
        existingStudent.setDepartment(updatedStudent.getDepartment());
        existingStudent.setAge(updatedStudent.getAge());

        return studentRepository.save(existingStudent);
    }

    public void deleteStudent(Long id) {
        Student student = getStudentById(id);
        studentRepository.delete(student);
    }

    @Transactional(readOnly = true)
    public List<Student> searchStudentsByName(String name) {
        return studentRepository.findByNameContainingIgnoreCase(name);
    }
}

