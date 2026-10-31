package com.example.Marks.Service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.Marks.Service.client.StudentEmailClient;
import com.example.Marks.Service.client.StudentEmailClient.StudentResponse;
import com.example.Marks.Service.entity.Marks;

@Service
public class MarksEmailService {

    private final JavaMailSender mailSender;
    private final StudentEmailClient studentEmailClient;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public MarksEmailService(
            JavaMailSender mailSender,
            StudentEmailClient studentEmailClient) {

        this.mailSender = mailSender;
        this.studentEmailClient = studentEmailClient;
    }

    public void sendMarksNotification(
            Marks marks,
            String authorization) {

        try {
            StudentResponse student =
                    studentEmailClient.getStudent(
                            marks.getStudentId(),
                            authorization
                    );

            if (student == null || student.email() == null
                    || student.email().isBlank()) {
                System.err.println("Student email not found.");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(senderEmail);
            message.setTo(student.email());

            message.setSubject(
                    "EduManage - Marks Notification"
            );

            message.setText(
                    "Hello " + student.name() + ",\n\n"
                    + "Your marks have been added or updated.\n\n"
                    + "Student ID: " + marks.getStudentId() + "\n"
                    + "Subject: " + marks.getSubject() + "\n"
                    + "Semester: " + marks.getSemester() + "\n"
                    + "Marks Obtained: " + marks.getMarksObtained() + "\n"
                    + "Maximum Marks: " + marks.getMaximumMarks() + "\n"
                    + "Percentage: " + marks.getPercentage() + "%\n"
                    + "Grade: " + marks.getGrade() + "\n\n"
                    + "Regards,\n"
                    + "EduManage Student Management System"
            );

            mailSender.send(message);

            System.out.println(
                    "Marks notification sent to " + student.email()
            );

        } catch (Exception e) {

            System.err.println(
                    "Marks notification failed: " + e.getMessage()
            );
        }
    }
}