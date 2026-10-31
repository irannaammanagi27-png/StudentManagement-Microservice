package com.example.Course.Service.service;



import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Course.Service.entity.Course;
import com.example.Course.Service.repository.CourseRepository;



@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Course addCourse(Course course) {
        if (courseRepository.existsByCodeIgnoreCase(course.getCode())) {
            throw new IllegalArgumentException("Course code already exists");
        }

        return courseRepository.save(course);
    }

    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() ->
                        new CourseNotFoundException(
                                "Course not found with ID: " + id));
    }

    public Course updateCourse(Long id, Course updatedCourse) {
        Course existingCourse = getCourseById(id);

        boolean codeUsedByAnotherCourse =
                courseRepository.existsByCodeIgnoreCase(updatedCourse.getCode())
                && !existingCourse.getCode()
                        .equalsIgnoreCase(updatedCourse.getCode());

        if (codeUsedByAnotherCourse) {
            throw new IllegalArgumentException("Course code already exists");
        }

        existingCourse.setName(updatedCourse.getName());
        existingCourse.setCode(updatedCourse.getCode());
        existingCourse.setDepartment(updatedCourse.getDepartment());
        existingCourse.setDurationMonths(updatedCourse.getDurationMonths());
        existingCourse.setFee(updatedCourse.getFee());

        return courseRepository.save(existingCourse);
    }

    public void deleteCourse(Long id) {
        Course course = getCourseById(id);
        courseRepository.delete(course);
    }

    @Transactional(readOnly = true)
    public List<Course> searchCourses(String name) {
        return courseRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Course> getCoursesByDepartment(String department) {
        return courseRepository.findByDepartmentIgnoreCase(department);
    }
}