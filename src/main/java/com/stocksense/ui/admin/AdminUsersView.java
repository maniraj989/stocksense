package com.stocksense.ui.admin;

import com.stocksense.ui.layout.MainLayout;
import com.stocksense.user.entity.User;
import com.stocksense.user.entity.UserRole;
import com.stocksense.user.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;

@Route(value = "admin/users", layout = MainLayout.class)
@PageTitle("User Administration — StockSense")
@RolesAllowed("ADMIN")
public class AdminUsersView extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserService userService;
    private final Grid<User> grid = new Grid<>();

    public AdminUsersView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("User Management & Access Control");
        title.getStyle().set("margin-top", "0").set("color", "#0f172a");

        Paragraph description = new Paragraph("Administrative portal for managing system operators, roles, and access credentials.");
        description.getStyle().set("color", "#64748b");

        Button addUserBtn = new Button("Add New User", new Icon(VaadinIcon.PLUS), e -> openAddUserDialog());
        addUserBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(addUserBtn);
        toolbar.setWidthFull();

        configureGrid();
        add(title, description, toolbar, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(User::getId).setHeader("ID").setAutoWidth(true).setSortable(true);
        grid.addColumn(User::getName).setHeader("Full Name").setFlexGrow(2).setSortable(true);
        grid.addColumn(User::getUsername).setHeader("Username").setFlexGrow(1).setSortable(true);
        grid.addColumn(User::getEmail).setHeader("Email Address").setFlexGrow(2);

        grid.addComponentColumn(u -> {
            boolean isAdmin = u.getRole() == UserRole.ADMIN;
            Span badge = new Span(u.getRole() != null ? u.getRole().name() : "-");
            badge.getStyle()
                    .set("background-color", isAdmin ? "#f3e8ff" : "#e0f2fe")
                    .set("color", isAdmin ? "#7e22ce" : "#0369a1")
                    .set("padding", "3px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "0.75rem")
                    .set("font-weight", "600");
            return badge;
        }).setHeader("Role").setAutoWidth(true);

        grid.addColumn(u -> u.getCreatedAt() != null ? u.getCreatedAt().format(DATE_FORMATTER) : "-")
                .setHeader("Created Date").setAutoWidth(true);

        grid.addComponentColumn(u -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button toggleRoleBtn = new Button(u.getRole() == UserRole.ADMIN ? "Demote to Staff" : "Promote to Admin", e -> {
                UserRole newRole = u.getRole() == UserRole.ADMIN ? UserRole.STAFF : UserRole.ADMIN;
                userService.changeRole(u.getId(), newRole);
                Notification.show("Updated role for " + u.getUsername() + " to " + newRole, 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshGrid();
            });
            toggleRoleBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            actions.add(toggleRoleBtn);

            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH), e -> confirmDelete(u));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
            deleteBtn.setTooltipText("Delete user");
            actions.add(deleteBtn);

            return actions;
        }).setHeader("Actions").setAutoWidth(true);
    }

    private void openAddUserDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Register New System User");

        TextField nameField = new TextField("Full Name");
        nameField.setRequired(true);

        TextField usernameField = new TextField("Username");
        usernameField.setRequired(true);

        PasswordField passwordField = new PasswordField("Initial Password");
        passwordField.setRequired(true);
        passwordField.setHelperText("Must be at least 8 characters");

        ComboBox<UserRole> roleCombo = new ComboBox<>("Role Assignment");
        roleCombo.setItems(UserRole.values());
        roleCombo.setValue(UserRole.STAFF);
        roleCombo.setRequired(true);

        EmailField emailField = new EmailField("Email Address");

        FormLayout form = new FormLayout(nameField, usernameField, passwordField, roleCombo, emailField);
        form.setColspan(emailField, 2);

        Button createBtn = new Button("Create User", e -> {
            if (nameField.isEmpty() || usernameField.isEmpty() || passwordField.isEmpty() || roleCombo.getValue() == null) {
                Notification.show("Please fill all required fields", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                userService.createUser(
                        nameField.getValue().trim(),
                        usernameField.getValue().trim(),
                        passwordField.getValue(),
                        roleCombo.getValue(),
                        emailField.getValue() != null ? emailField.getValue().trim() : null
                );

                Notification.show("User " + usernameField.getValue() + " created successfully", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                refreshGrid();
            } catch (Exception ex) {
                Notification.show("Error creating user: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        dialog.add(form);
        dialog.getFooter().add(cancelBtn, createBtn);
        dialog.setWidth("500px");
        dialog.open();
    }

    private void confirmDelete(User user) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Deletion");
        confirmDialog.add(new Span("Are you sure you want to permanently delete user '" + user.getUsername() + "'?"));

        Button deleteBtn = new Button("Delete", e -> {
            try {
                userService.deleteById(user.getId());
                Notification.show("User deleted successfully", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                confirmDialog.close();
                refreshGrid();
            } catch (Exception ex) {
                Notification.show("Cannot delete user: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelBtn = new Button("Cancel", e -> confirmDialog.close());
        confirmDialog.getFooter().add(cancelBtn, deleteBtn);
        confirmDialog.open();
    }

    private void refreshGrid() {
        grid.setItems(userService.findAll());
    }
}
