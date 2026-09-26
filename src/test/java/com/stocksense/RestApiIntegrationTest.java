package com.stocksense;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocksense.inventory.dto.StockAdjustmentRequest;
import com.stocksense.inventory.entity.MovementType;
import com.stocksense.notification.entity.Alert;
import com.stocksense.notification.entity.AlertSeverity;
import com.stocksense.notification.entity.AlertType;
import com.stocksense.notification.repository.AlertRepository;
import com.stocksense.product.dto.ProductRequest;
import com.stocksense.product.dto.SupplierRequest;
import com.stocksense.product.entity.Product;
import com.stocksense.product.entity.Supplier;
import com.stocksense.product.repository.ProductRepository;
import com.stocksense.product.repository.SupplierRepository;
import com.stocksense.purchase.dto.PurchaseItemRequest;
import com.stocksense.purchase.dto.PurchaseRequest;
import com.stocksense.sales.dto.SaleItemRequest;
import com.stocksense.sales.dto.SaleRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RestApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private AlertRepository alertRepository;

    // =========================================================================
    // SECURITY & ERROR TESTS
    // =========================================================================

    @Test
    @DisplayName("API: Unauthenticated request to /api/products returns 401 Unauthorized")
    void testUnauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("API: Nonexistent product ID returns 404 with structured error JSON")
    void testGetNonExistentProductReturns404() throws Exception {
        mockMvc.perform(get("/api/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Product not found")))
                .andExpect(jsonPath("$.path", is("/api/products/999999")));
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("API: Invalid product input returns 400 Bad Request with field validation errors")
    void testCreateInvalidProductReturns400() throws Exception {
        ProductRequest invalidReq = new ProductRequest(); // missing required fields: code, name, prices

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("VALIDATION_FAILED")))
                .andExpect(jsonPath("$.errors", hasKey("productCode")))
                .andExpect(jsonPath("$.errors", hasKey("name")))
                .andExpect(jsonPath("$.errors", hasKey("purchasePrice")))
                .andExpect(jsonPath("$.errors", hasKey("sellingPrice")));
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("API: Duplicate product code returns 409 Conflict")
    void testCreateDuplicateProductReturns409() throws Exception {
        Product existing = productRepository.save(new Product(
                "PRD-API-DUP", "Api Prod DUP", "Cat", "Desc", null,
                new BigDecimal("10.0000"), new BigDecimal("15.0000"), 10, 2, 50, null
        ));

        ProductRequest req = new ProductRequest(
                "PRD-API-DUP", "Another Prod", "Cat", "Desc", null,
                new BigDecimal("10.0000"), new BigDecimal("15.0000"), 10, 2, 50, null
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("DUPLICATE_RESOURCE")))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("API: STAFF cannot delete product (403 Forbidden)")
    void testStaffCannotDeleteProductReturns403() throws Exception {
        Product prod = productRepository.save(new Product(
                "PRD-API-DEL1", "To Delete", "Cat", "Desc", null,
                new BigDecimal("5.0000"), new BigDecimal("10.0000"), 5, 1, 20, null
        ));

        mockMvc.perform(delete("/api/products/" + prod.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("API: ADMIN can delete product (204 No Content)")
    void testAdminCanDeleteProductReturns204() throws Exception {
        Product prod = productRepository.save(new Product(
                "PRD-API-DEL2", "To Delete Admin", "Cat", "Desc", null,
                new BigDecimal("5.0000"), new BigDecimal("10.0000"), 5, 1, 20, null
        ));

        mockMvc.perform(delete("/api/products/" + prod.getId()))
                .andExpect(status().isNoContent());
    }

    // =========================================================================
    // CORE WORKFLOW ENDPOINT TESTS
    // =========================================================================

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("API: Valid product creation returns 201 Created")
    void testCreateProductApiSuccess() throws Exception {
        ProductRequest req = new ProductRequest(
                "PRD-API-NEW", "Brand New", "Electronics", "Desc", null,
                new BigDecimal("100.0000"), new BigDecimal("150.0000"), 20, 5, 100, null
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productCode", is("PRD-API-NEW")))
                .andExpect(jsonPath("$.name", is("Brand New")));
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("API: Get products with pagination returns 200 OK")
    void testListProductsApiSuccess() throws Exception {
        mockMvc.perform(get("/api/products?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("API: Valid supplier creation returns 201 Created")
    void testCreateSupplierApiSuccess() throws Exception {
        SupplierRequest req = new SupplierRequest("API Sup", "API Corp", "555-8888", "api@corp.com", "Addr");

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("API Sup")));
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("API: List suppliers returns 200 OK")
    void testListSuppliersApiSuccess() throws Exception {
        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("API: Purchase creation returns 201 Created")
    void testPurchaseApiSuccess() throws Exception {
        Supplier supplier = supplierRepository.save(new Supplier("API Purch Sup", "Co", "555", "e@c.com", "A"));
        Product product = productRepository.save(new Product(
                "PRD-API-PUR", "Purch Api Prod", "Cat", "Desc", supplier,
                new BigDecimal("20.0000"), new BigDecimal("40.0000"), 10, 5, 100, null
        ));

        PurchaseRequest req = new PurchaseRequest(
                supplier.getId(),
                List.of(new PurchaseItemRequest(product.getId(), 5, new BigDecimal("20.0000")))
        );

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount", is(100.0)))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("API: Sale with insufficient stock returns 400 with INSUFFICIENT_STOCK code")
    void testSaleApiInsufficientStockReturns400() throws Exception {
        Product product = productRepository.save(new Product(
                "PRD-API-LOW", "Low Stock Prod", "Cat", "Desc", null,
                new BigDecimal("10.0000"), new BigDecimal("20.0000"), 2, 1, 50, null
        ));

        SaleRequest req = new SaleRequest(
                List.of(new SaleItemRequest(product.getId(), 50, new BigDecimal("20.0000")))
        );

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("INSUFFICIENT_STOCK")))
                .andExpect(jsonPath("$.message", containsString("Insufficient stock")));
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("API: Stock adjust endpoint records adjustment and returns 200 OK")
    void testStockAdjustApiSuccess() throws Exception {
        Product product = productRepository.save(new Product(
                "PRD-API-ADJ", "Adjust Prod", "Cat", "Desc", null,
                new BigDecimal("10.0000"), new BigDecimal("20.0000"), 15, 2, 50, null
        ));

        StockAdjustmentRequest req = new StockAdjustmentRequest(product.getId(), 5, MovementType.IN, null);

        mockMvc.perform(post("/api/stock/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(5)))
                .andExpect(jsonPath("$.movementType", is("IN")));
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("API: Alert resolve endpoint updates status and returns 200 OK")
    void testResolveAlertApiSuccess() throws Exception {
        Product product = productRepository.save(new Product(
                "PRD-API-ALT", "Alert Api Prod", "Cat", "Desc", null,
                new BigDecimal("10.0000"), new BigDecimal("20.0000"), 1, 5, 50, null
        ));

        Alert alert = alertRepository.save(new Alert(product, AlertType.LOW_STOCK, "Low stock test", AlertSeverity.HIGH));

        mockMvc.perform(put("/api/alerts/" + alert.getId() + "/resolve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")));
    }
}
