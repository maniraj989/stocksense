package com.stocksense.ui.profile;

import com.stocksense.ui.layout.MainLayout;
import com.stocksense.ui.util.SecurityUtils;
import com.stocksense.user.entity.User;
import com.stocksense.user.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("My Profile — StockSense")
@PermitAll
public class ProfileView extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserService userService;
    private final SecurityUtils securityUtils;

    public ProfileView(UserService userService, SecurityUtils securityUtils) {
        this.userService = userService;
        this.securityUtils = securityUtils;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Account Profile");
        title.getStyle().set("margin-top", "0").set("color", "#0f172a");

        String currentUsername = securityUtils.getCurrentUsername();
        Optional<User> userOpt = userService.findByUsername(currentUsername);

        HorizontalLayout sectionsRow = new HorizontalLayout();
        sectionsRow.setWidthFull();
        sectionsRow.setSpacing(true);

        sectionsRow.add(createDetailsCard(userOpt.orElse(null)), createPasswordCard(currentUsername));
        add(title, sectionsRow);
    }

    private VerticalLayout createDetailsCard(User user) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "8px")
                .set("padding", "24px");
        card.setWidth("45%");

        H3 cardTitle = new H3("User Information");
        cardTitle.getStyle().set("margin-top", "0").set("color", "#1e293b");

        if (user != null) {
            Paragraph nameP = new Paragraph("Full Name: " + user.getName());
            nameP.getStyle().set("font-weight", "500").set("margin", "4px 0");

            Paragraph userP = new Paragraph("Username: " + user.getUsername());
            userP.getStyle().set("color", "#64748b").set("margin", "4px 0");

            Paragraph emailP = new Paragraph("Email: " + (user.getEmail() != null ? user.getEmail() : "None registered"));
            emailP.getStyle().set("color", "#64748b").set("margin", "4px 0");

            Span roleBadge = new Span("Role: " + user.getRole().name());
            roleBadge.getStyle()
                    .set("background-color", user.getRole().name().equals("ADMIN") ? "#f3e8ff" : "#e0f2fe")
                    .set("color", user.getRole().name().equals("ADMIN") ? "#7e22ce" : "#0369a1")
                    .set("padding", "4px 12px")
                    .set("border-radius", "12px")
                    .set("font-size", "0.85rem")
                    .set("font-weight", "600")
                    .set("display", "inline-block")
                    .set("margin-top", "8px");

            Paragraph createdP = new Paragraph("Member Since: " + (user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_FORMATTER) : "-"));
            createdP.getStyle().set("color", "#94a3b8").set("font-size", "0.8rem").set("margin-top", "16px");

            card.add(cardTitle, nameP, userP, emailP, roleBadge, createdP);
        } else {
            card.add(cardTitle, new Paragraph("User profile information currently unavailable."));
        }
        return card;
    }

    private VerticalLayout createPasswordCard(String username) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "8px")
                .set("padding", "24px");
        card.setWidth("55%");

        H3 cardTitle = new H3("Security & Password");
        cardTitle.getStyle().set("margin-top", "0").set("color", "#1e293b");

        PasswordField currentPass = new PasswordField("Current Password");
        currentPass.setRequired(true);

        PasswordField newPass = new PasswordField("New Password");
        newPass.setRequired(true);
        newPass.setHelperText("Must contain at least 8 characters");

        PasswordField confirmPass = new PasswordField("Confirm New Password");
        confirmPass.setRequired(true);

        Button updateBtn = new Button("Update Password", e -> {
            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Notification.show("Please complete all password fields", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (!newPass.getValue().equals(confirmPass.getValue())) {
                Notification.show("New passwords do not match", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                userService.changePassword(username, currentPass.getValue(), newPass.getValue());
                Notification.show("Password updated successfully", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                currentPass.clear();
                newPass.clear();
                confirmPass.clear();
            } catch (Exception ex) {
                Notification.show("Failed to update password: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        updateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        FormLayout form = new FormLayout(currentPass, newPass, confirmPass, updateBtn);
        form.setColspan(currentPass, 2);
        form.setColspan(newPass, 2);
        form.setColspan(confirmPass, 2);

        card.add(cardTitle, form);
        return card;
    }
}
