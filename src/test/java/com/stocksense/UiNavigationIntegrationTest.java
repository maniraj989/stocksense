package com.stocksense;

import com.stocksense.inventory.service.StockMovementService;
import com.stocksense.notification.service.AlertService;
import com.stocksense.product.service.ProductService;
import com.stocksense.product.service.SupplierService;
import com.stocksense.purchase.service.PurchaseService;
import com.stocksense.sales.service.SaleService;
import com.stocksense.ui.admin.AdminUsersView;
import com.stocksense.ui.alert.AlertsView;
import com.stocksense.ui.dashboard.DashboardView;
import com.stocksense.ui.inventory.LowStockView;
import com.stocksense.ui.inventory.StockMovementsView;
import com.stocksense.ui.inventory.StockView;
import com.stocksense.ui.layout.MainLayout;
import com.stocksense.ui.login.LoginView;
import com.stocksense.ui.product.ProductsView;
import com.stocksense.ui.profile.ProfileView;
import com.stocksense.ui.purchase.PurchasesView;
import com.stocksense.ui.sales.SalesHistoryView;
import com.stocksense.ui.sales.SalesPosView;
import com.stocksense.ui.supplier.SuppliersView;
import com.stocksense.ui.util.SecurityUtils;
import com.stocksense.user.service.UserService;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UiNavigationIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private SaleService saleService;

    @Autowired
    private StockMovementService stockMovementService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private com.stocksense.barcode.service.BarcodeService barcodeService;

    @Autowired
    private com.stocksense.report.service.AnalyticsService analyticsService;

    @Autowired
    private com.stocksense.report.service.PdfReportService pdfReportService;

    @Autowired
    private com.stocksense.report.service.CsvExportService csvExportService;

    @Autowired
    private com.stocksense.report.service.ExcelExportService excelExportService;

    @Autowired
    private com.stocksense.report.service.InventoryValuationService inventoryValuationService;

    @Test
    @DisplayName("UI Architecture: SecurityUtils helper bean is loaded")
    void testSecurityUtilsBeanLoaded() {
        assertThat(securityUtils).isNotNull();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("UI Architecture: SecurityUtils recognizes ADMIN role")
    void testSecurityUtilsAdminCheck() {
        assertThat(securityUtils.isAdmin()).isTrue();
        assertThat(securityUtils.getCurrentUsername()).isEqualTo("admin");
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("UI Architecture: SecurityUtils recognizes STAFF role (not admin)")
    void testSecurityUtilsStaffCheck() {
        assertThat(securityUtils.isAdmin()).isFalse();
        assertThat(securityUtils.getCurrentUsername()).isEqualTo("staff");
    }

    @Test
    @DisplayName("UI Routing: All views have valid @Route configurations")
    void testViewRouteAnnotations() {
        assertThat(LoginView.class.getAnnotation(Route.class).value()).isEqualTo("login");
        assertThat(DashboardView.class.getAnnotation(Route.class).value()).isEqualTo("");
        assertThat(ProductsView.class.getAnnotation(Route.class).value()).isEqualTo("products");
        assertThat(SuppliersView.class.getAnnotation(Route.class).value()).isEqualTo("suppliers");
        assertThat(PurchasesView.class.getAnnotation(Route.class).value()).isEqualTo("purchases");
        assertThat(SalesPosView.class.getAnnotation(Route.class).value()).isEqualTo("sales");
        assertThat(SalesHistoryView.class.getAnnotation(Route.class).value()).isEqualTo("sales/history");
        assertThat(StockView.class.getAnnotation(Route.class).value()).isEqualTo("stock");
        assertThat(StockMovementsView.class.getAnnotation(Route.class).value()).isEqualTo("stock/movements");
        assertThat(LowStockView.class.getAnnotation(Route.class).value()).isEqualTo("stock/low");
        assertThat(AlertsView.class.getAnnotation(Route.class).value()).isEqualTo("alerts");
        assertThat(AdminUsersView.class.getAnnotation(Route.class).value()).isEqualTo("admin/users");
        assertThat(ProfileView.class.getAnnotation(Route.class).value()).isEqualTo("profile");
        assertThat(com.stocksense.ui.report.ReportsView.class.getAnnotation(Route.class).value()).isEqualTo("reports");
    }

    @Test
    @DisplayName("UI Security: AdminUsersView is restricted to ADMIN role")
    void testAdminUsersViewSecurityAnnotation() {
        RolesAllowed rolesAllowed = AdminUsersView.class.getAnnotation(RolesAllowed.class);
        assertThat(rolesAllowed).isNotNull();
        assertThat(rolesAllowed.value()).contains("ADMIN");
    }

    @Test
    @DisplayName("UI Security: Business views are secured with @PermitAll")
    void testBusinessViewsSecurityAnnotation() {
        assertThat(DashboardView.class.getAnnotation(PermitAll.class)).isNotNull();
        assertThat(ProductsView.class.getAnnotation(PermitAll.class)).isNotNull();
        assertThat(SuppliersView.class.getAnnotation(PermitAll.class)).isNotNull();
        assertThat(PurchasesView.class.getAnnotation(PermitAll.class)).isNotNull();
        assertThat(SalesPosView.class.getAnnotation(PermitAll.class)).isNotNull();
        assertThat(SalesHistoryView.class.getAnnotation(PermitAll.class)).isNotNull();
        assertThat(StockView.class.getAnnotation(PermitAll.class)).isNotNull();
        assertThat(StockMovementsView.class.getAnnotation(PermitAll.class)).isNotNull();
        assertThat(LowStockView.class.getAnnotation(PermitAll.class)).isNotNull();
        assertThat(AlertsView.class.getAnnotation(PermitAll.class)).isNotNull();
        assertThat(ProfileView.class.getAnnotation(PermitAll.class)).isNotNull();
        assertThat(com.stocksense.ui.report.ReportsView.class.getAnnotation(PermitAll.class)).isNotNull();
    }

    @Autowired
    private com.stocksense.product.repository.ProductRepository productRepository;

    @Autowired
    private com.stocksense.product.repository.SupplierRepository supplierRepository;

    @Autowired
    private com.stocksense.sales.repository.SaleRepository saleRepository;

    @Autowired
    private com.stocksense.purchase.repository.PurchaseRepository purchaseRepository;

    @Autowired
    private com.stocksense.inventory.repository.StockMovementRepository stockMovementRepository;

    @Autowired
    private com.stocksense.notification.repository.AlertRepository alertRepository;

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("UI Instantiation: Core views instantiate without errors with Spring services")
    void testViewsInstantiationWithSpringServices() {
        MainLayout layout = new MainLayout(securityUtils);
        assertThat(layout).isNotNull();

        LoginView loginView = new LoginView(securityUtils);
        assertThat(loginView).isNotNull();

        DashboardView dashboardView = new DashboardView(productService, supplierService, purchaseService, saleService, alertService, analyticsService);
        assertThat(dashboardView).isNotNull();

        ProductsView productsView = new ProductsView(productService, supplierService, securityUtils, barcodeService);
        assertThat(productsView).isNotNull();

        SuppliersView suppliersView = new SuppliersView(supplierService, securityUtils);
        assertThat(suppliersView).isNotNull();

        PurchasesView purchasesView = new PurchasesView(purchaseService, supplierService, productService, pdfReportService);
        assertThat(purchasesView).isNotNull();

        SalesPosView salesPosView = new SalesPosView(productService, saleService, barcodeService);
        assertThat(salesPosView).isNotNull();

        SalesHistoryView salesHistoryView = new SalesHistoryView(saleService, pdfReportService);
        assertThat(salesHistoryView).isNotNull();

        StockView stockView = new StockView(productService, stockMovementService);
        assertThat(stockView).isNotNull();

        StockMovementsView movementsView = new StockMovementsView(stockMovementService);
        assertThat(movementsView).isNotNull();

        LowStockView lowStockView = new LowStockView(productService);
        assertThat(lowStockView).isNotNull();

        AlertsView alertsView = new AlertsView(alertService);
        assertThat(alertsView).isNotNull();

        ProfileView profileView = new ProfileView(userService, securityUtils);
        assertThat(profileView).isNotNull();

        AdminUsersView adminUsersView = new AdminUsersView(userService);
        assertThat(adminUsersView).isNotNull();

        com.stocksense.ui.report.ReportsView reportsView = new com.stocksense.ui.report.ReportsView(
                inventoryValuationService, analyticsService, pdfReportService, csvExportService, excelExportService,
                productRepository, supplierRepository, saleRepository, purchaseRepository, stockMovementRepository, alertRepository);
        assertThat(reportsView).isNotNull();
    }
}
