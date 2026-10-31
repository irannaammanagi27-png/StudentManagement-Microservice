package com.example.Attendance.Service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.Attendance.Service.client.StudentEmailClient;
import com.example.Attendance.Service.client.StudentEmailClient.StudentResponse;
import com.example.Attendance.Service.entity.Attendance;

@Service
public class AttendanceEmailService {

    private final JavaMailSender mailSender;
    private final StudentEmailClient studentEmailClient;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public AttendanceEmailService(
            JavaMailSender mailSender,
            StudentEmailClient studentEmailClient) {

        this.mailSender = mailSender;
        this.studentEmailClient = studentEmailClient;
    }

    public void sendAttendanceNotification(
            Attendance attendance,
            String authorization) {

        try {

            // Retrieve student details
            StudentResponse student =
                    studentEmailClient.getStudent(
                            attendance.getStudentId(),
                            authorization
                    );

            if (student == null
                    || student.email() == null
                    || student.email().isBlank()) {

                System.err.println("Student email not found.");
                return;
            }

            // Create email
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(senderEmail);
            message.setTo(student.email());

            message.setSubject(
                    "EduManage - Attendance Notification"
            );

            message.setText(
                    "Hello " + student.name() + ",\n\n"

                    + "Your attendance has been added or updated.\n\n"

                    + "Student ID: "
                    + attendance.getStudentId() + "\n"

                    + "Attendance Date: "
                    + attendance.getAttendanceDate() + "\n"

                    + "Attendance Status: "
                    + attendance.getStatus() + "\n"

                    + "Remarks: "
                    + (attendance.getRemarks() == null
                            ? "No remarks"
                            : attendance.getRemarks())
                    + "\n\n"

                    + "Please check your attendance details.\n\n"

                    + "Regards,\n"
                    + "EduManage Student Management System"
            );

            mailSender.send(message);

            System.out.println(
                    "Attendance notification sent to "
                    + student.email()
            );

        } catch (Exception e) {

            System.err.println(
                    "Attendance email failed: "
                    + e.getMessage()
            );
        }
    }
}