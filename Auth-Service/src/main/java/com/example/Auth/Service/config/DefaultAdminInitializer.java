
package com.example.Auth.Service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.Auth.Service.entity.Role;
import com.example.Auth.Service.entity.User;
import com.example.Auth.Service.repository.UserRepository;



@Configuration
public class DefaultAdminInitializer {

    @Bean
    public ApplicationRunner createDefaultAdmin(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        @Value("${app.admin.email}") String adminEmail,
        @Value("${app.admin.password}") String adminPassword
    ) {

        return args -> {

            if (userRepository.existsByEmail(adminEmail)) {
                System.out.println("Default Admin already exists.");
                return;
            }

            User admin = new User();

            admin.setFullName("EduManage Administrator");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setStudentId(null);
            admin.setEnabled(true);

            userRepository.save(admin);

            System.out.println("Default Admin account created successfully.");
            System.out.println("Admin email: " + adminEmail);
        };
    }
}
