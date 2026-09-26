package com.stocksense.user.service;

import com.stocksense.security.PasswordPolicy;
import com.stocksense.security.UpgradePasswordAuthenticationProvider;
import com.stocksense.user.entity.User;
import com.stocksense.user.entity.UserRole;
import com.stocksense.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       PasswordPolicy passwordPolicy) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(String name, String username, String rawPassword, UserRole role, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is already taken: " + username);
        }
        passwordPolicy.validate(rawPassword);

        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(name, username, encodedPassword, role, email);
        log.info("Creating new user: {} with role: {}", username, role);
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        boolean currentMatches = UpgradePasswordAuthenticationProvider.isBcrypt(user.getPassword())
                ? passwordEncoder.matches(currentPassword, user.getPassword())
                : user.getPassword().equals(currentPassword);

        if (!currentMatches) {
            log.warn("Password change failed for user: {} (incorrect current password)", username);
            throw new BadCredentialsException("Current password does not match");
        }

        passwordPolicy.validate(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password successfully updated for user: {}", username);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public User changeRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setRole(newRole);
        log.info("Role updated for user id: {} to {}", userId, newRole);
        return userRepository.save(user);
    }

    @Transactional
    public User save(User user) {
        if (user.getPassword() != null && !UpgradePasswordAuthenticationProvider.isBcrypt(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteById(Long id) {
        log.info("Deleting user id: {}", id);
        userRepository.deleteById(id);
    }
}
