package com.stocksense.security;

import com.stocksense.user.entity.User;
import com.stocksense.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpgradePasswordAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(UpgradePasswordAuthenticationProvider.class);

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UpgradePasswordAuthenticationProvider(CustomUserDetailsService userDetailsService,
                                                PasswordEncoder passwordEncoder,
                                                UserRepository userRepository) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        Object credentials = authentication.getCredentials();
        if (credentials == null) {
            throw new BadCredentialsException("Invalid username or password");
        }
        String rawPassword = credentials.toString();

        StockSenseUserDetails userDetails;
        try {
            userDetails = (StockSenseUserDetails) userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            log.warn("Authentication failed for username: {} (user not found)", username);
            throw new BadCredentialsException("Invalid username or password");
        }
        String storedPassword = userDetails.getPassword();

        if (isBcrypt(storedPassword)) {
            if (!passwordEncoder.matches(rawPassword, storedPassword)) {
                log.warn("Authentication failed for user: {} (invalid credentials)", username);
                throw new BadCredentialsException("Invalid username or password");
            }
        } else {
            // Legacy password format check (e.g. legacy MySQL plaintext)
            if (storedPassword.equals(rawPassword)) {
                // Transparently upgrade to BCrypt
                User entity = userRepository.findById(userDetails.getId()).orElse(null);
                if (entity != null) {
                    entity.setPassword(passwordEncoder.encode(rawPassword));
                    userRepository.save(entity);
                    log.info("Transparently migrated legacy password to BCrypt hash for user: {}", username);
                }
            } else {
                log.warn("Authentication failed for user: {} (invalid credentials)", username);
                throw new BadCredentialsException("Invalid username or password");
            }
        }

        log.info("User successfully authenticated: {} with role: {}", username, userDetails.getRole());
        return UsernamePasswordAuthenticationToken.authenticated(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public static boolean isBcrypt(String password) {
        return password != null &&
                (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$")) &&
                password.length() == 60;
    }
}
