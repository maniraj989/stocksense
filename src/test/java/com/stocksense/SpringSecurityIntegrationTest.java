package com.stocksense;

import com.stocksense.security.UpgradePasswordAuthenticationProvider;
import com.stocksense.user.entity.User;
import com.stocksense.user.entity.UserRole;
import com.stocksense.user.repository.UserRepository;
import com.stocksense.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SpringSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Authentication: Valid user with BCrypt password authenticates successfully")
    void testBcryptAuthenticationSuccess() {
        String rawPassword = "SecurePassword123";
        User user = new User("Sec Admin", "secadmin", passwordEncoder.encode(rawPassword), UserRole.ADMIN, "secadmin@test.com");
        userRepository.save(user);

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("secadmin", rawPassword)
        );

        assertThat(auth).isNotNull();
        assertThat(auth.isAuthenticated()).isTrue();
        assertThat(auth.getAuthorities()).extracting("authority").contains("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Authentication: Invalid password is rejected with BadCredentialsException")
    void testInvalidPasswordRejected() {
        User user = new User("Sec Staff", "secstaff", passwordEncoder.encode("CorrectPassword1"), UserRole.STAFF, "secstaff@test.com");
        userRepository.save(user);

        assertThatThrownBy(() -> authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("secstaff", "WrongPassword")
        )).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Authentication: Unknown username is rejected with BadCredentialsException")
    void testUnknownUserRejected() {
        assertThatThrownBy(() -> authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("nonexistentuser", "SomePassword")
        )).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Legacy Password Compatibility: Transparently authenticates and upgrades plaintext to BCrypt")
    void testLegacyPlaintextPasswordTransparentUpgrade() {
        // Simulates a legacy user row migrated directly from MySQL with plaintext password
        User legacyUser = new User("Legacy Cashier", "legacyuser", "legacy123", UserRole.STAFF, "legacy@test.com");
        User savedLegacy = userRepository.save(legacyUser);

        // Pre-condition: Stored password is raw plaintext
        assertThat(savedLegacy.getPassword()).isEqualTo("legacy123");
        assertThat(UpgradePasswordAuthenticationProvider.isBcrypt(savedLegacy.getPassword())).isFalse();

        // Authenticate with legacy credentials
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("legacyuser", "legacy123")
        );
        assertThat(auth.isAuthenticated()).isTrue();

        // Post-condition: Stored password was transparently upgraded to BCrypt
        User upgradedUser = userRepository.findById(savedLegacy.getId()).orElseThrow();
        assertThat(upgradedUser.getPassword()).startsWith("$2a$");
        assertThat(UpgradePasswordAuthenticationProvider.isBcrypt(upgradedUser.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("legacy123", upgradedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Method-Level Security: ADMIN can invoke administrative user creation")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminMethodSecurityAllowed() {
        User created = userService.createUser("New Operative", "newoperative", "SecretPass123", UserRole.STAFF, "op@test.com");
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getPassword()).startsWith("$2a$");
    }

    @Test
    @DisplayName("Method-Level Security: STAFF receives AccessDeniedException on administrative operations")
    @WithMockUser(username = "staffuser", roles = {"STAFF"})
    void testStaffMethodSecurityDenied() {
        assertThatThrownBy(() ->
                userService.createUser("Unauthorized User", "unauthuser", "SecretPass123", UserRole.STAFF, "unauth@test.com")
        ).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Route Security: /api/health is accessible anonymously")
    void testPublicHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Route Security: /api/auth/me rejects anonymous requests with 401")
    void testProtectedEndpointRejectsAnonymous() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Route Security: /api/auth/me returns user profile without exposing password")
    void testProtectedEndpointReturnsUserProfile() throws Exception {
        User user = new User("Jane Doe", "janedoe", passwordEncoder.encode("Password123"), UserRole.STAFF, "jane@test.com");
        userRepository.save(user);

        mockMvc.perform(get("/api/auth/me")
                        .with(httpBasic("janedoe", "Password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("janedoe"))
                .andExpect(jsonPath("$.role").value("STAFF"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("RBAC Route Security: /api/admin/users rejects STAFF with 403 Forbidden")
    void testAdminRouteRejectsStaff() throws Exception {
        User staff = new User("Staff Bob", "staffbob", passwordEncoder.encode("BobPassword1"), UserRole.STAFF, "bob@test.com");
        userRepository.save(staff);

        mockMvc.perform(get("/api/admin/users")
                        .with(httpBasic("staffbob", "BobPassword1")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RBAC Route Security: /api/admin/users permits ADMIN with 200 OK")
    void testAdminRoutePermitsAdmin() throws Exception {
        User admin = new User("Super Admin", "superadmin", passwordEncoder.encode("AdminPass123"), UserRole.ADMIN, "super@test.com");
        userRepository.save(admin);

        mockMvc.perform(get("/api/admin/users")
                        .with(httpBasic("superadmin", "AdminPass123")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Password Change: Verifies current password and updates to BCrypt")
    void testPasswordChangeWorkflow() {
        User user = new User("Alice Smith", "alicesmith", passwordEncoder.encode("OldPassword123"), UserRole.STAFF, "alice@test.com");
        userRepository.save(user);

        // Incorrect current password fails
        assertThatThrownBy(() ->
                userService.changePassword("alicesmith", "WrongOldPassword", "NewPassword456")
        ).isInstanceOf(BadCredentialsException.class);

        // Correct change succeeds
        userService.changePassword("alicesmith", "OldPassword123", "NewPassword456");

        Optional<User> updated = userRepository.findByUsername("alicesmith");
        assertThat(updated).isPresent();
        assertThat(passwordEncoder.matches("NewPassword456", updated.get().getPassword())).isTrue();
    }
}
