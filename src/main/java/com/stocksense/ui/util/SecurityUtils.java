package com.stocksense.ui.util;

import com.stocksense.user.entity.UserRole;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtils {

    private final AuthenticationContext authenticationContext;

    public SecurityUtils(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public Optional<UserDetails> getAuthenticatedUser() {
        Optional<UserDetails> vaadinUser = authenticationContext.getAuthenticatedUser(UserDetails.class);
        if (vaadinUser.isPresent()) {
            return vaadinUser;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)
                && auth.getPrincipal() instanceof UserDetails ud) {
            return Optional.of(ud);
        }
        return Optional.empty();
    }

    public String getCurrentUsername() {
        Optional<UserDetails> user = getAuthenticatedUser();
        if (user.isPresent()) {
            return user.get().getUsername();
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)
                && !"anonymousUser".equals(auth.getName())
                && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "Guest";
    }

    public boolean isUserLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return false;
        }
        return !"anonymousUser".equals(auth.getName()) && !"anonymousUser".equals(auth.getPrincipal());
    }

    public boolean isAdmin() {
        if (!isUserLoggedIn()) {
            return false;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals(UserRole.ADMIN.name()));
    }

    public void logout() {
        if (authenticationContext.isAuthenticated()) {
            authenticationContext.logout();
        } else if (UI.getCurrent() != null) {
            UI.getCurrent().getPage().setLocation("/login");
        }
    }
}
