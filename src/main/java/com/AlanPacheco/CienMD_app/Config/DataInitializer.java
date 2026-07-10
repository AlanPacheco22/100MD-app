package com.AlanPacheco.CienMD_app.Config;

import com.AlanPacheco.CienMD_app.Entity.User;
import com.AlanPacheco.CienMD_app.Enum.UserRole;
import com.AlanPacheco.CienMD_app.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@100md.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Administrador");
            admin.setRole(UserRole.ADMIN);
            userRepository.save(admin);
            System.out.println("Usuario admin creado (admin / admin123)");
        }
    }
}
