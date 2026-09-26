package com.stocksense.ui.login;

import com.stocksense.ui.util.SecurityUtils;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login — StockSense")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();
    private final SecurityUtils securityUtils;

    public LoginView(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background-color", "#f1f5f9");

        H1 title = new H1("StockSense");
        title.getStyle().set("color", "#1e3a8a");
        title.getStyle().set("margin-bottom", "0");
        title.getStyle().set("font-weight", "700");

        H3 subtitle = new H3("Smart Inventory Management System");
        subtitle.getStyle().set("color", "#64748b");
        subtitle.getStyle().set("margin-top", "4px");
        subtitle.getStyle().set("margin-bottom", "24px");
        subtitle.getStyle().set("font-size", "1.1rem");

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.getForm().setTitle("Sign In");
        i18n.getForm().setUsername("Username");
        i18n.getForm().setPassword("Password");
        i18n.getForm().setSubmit("Log In");
        i18n.getErrorMessage().setTitle("Authentication Failed");
        i18n.getErrorMessage().setMessage("Invalid username or password. Please verify your credentials.");
        loginForm.setI18n(i18n);

        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);

        add(title, subtitle, loginForm);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (securityUtils.isUserLoggedIn()) {
            event.forwardTo("");
            return;
        }

        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }
}
