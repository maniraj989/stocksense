package com.stocksense;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DatabaseIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Verify PostgreSQL connection and HikariCP connection pool")
    void testPostgreSqlConnectionAndHikariPool() {
        assertThat(dataSource).isNotNull();
        assertThat(dataSource).isInstanceOf(HikariDataSource.class);

        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        assertThat(hikariDataSource.getPoolName()).isEqualTo("StockSenseHikariCP");

        String dbVersion = jdbcTemplate.queryForObject("SELECT version()", String.class);
        assertThat(dbVersion).isNotNull().containsIgnoringCase("PostgreSQL");
    }

    @Test
    @DisplayName("Verify Flyway migration history and V1 execution")
    void testFlywayMigrationApplied() {
        Integer historyTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'flyway_schema_history'",
                Integer.class
        );
        assertThat(historyTableCount).isGreaterThan(0);

        List<Map<String, Object>> migrations = jdbcTemplate.queryForList(
                "SELECT version, description, type, script, success FROM flyway_schema_history WHERE version = '1'"
        );
        assertThat(migrations).hasSize(1);

        Map<String, Object> v1 = migrations.get(0);
        assertThat(v1.get("version")).isEqualTo("1");
        assertThat(v1.get("description").toString()).containsIgnoringCase("initial");
        assertThat((Boolean) v1.get("success")).isTrue();
    }

    @Test
    @DisplayName("Verify all 9 legacy-compatible tables exist in PostgreSQL")
    void testAllLegacyCompatibleTablesExist() {
        List<String> expectedTables = List.of(
                "users",
                "suppliers",
                "products",
                "purchases",
                "purchase_items",
                "sales",
                "sale_items",
                "stock_movements",
                "alerts"
        );

        List<String> actualTables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE'",
                String.class
        );

        assertThat(actualTables).containsAll(expectedTables);
    }

    @Test
    @DisplayName("Verify primary keys and foreign key constraints on tables")
    void testConstraintsExist() {
        List<String> expectedFkConstraints = List.of(
                "fk_products_supplier",
                "fk_purchases_supplier",
                "fk_purchases_user",
                "fk_purchase_items_purchase",
                "fk_purchase_items_product",
                "fk_sales_user",
                "fk_sale_items_sale",
                "fk_sale_items_product",
                "fk_stock_movements_product",
                "fk_stock_movements_user",
                "fk_alerts_product"
        );

        List<String> actualConstraints = jdbcTemplate.queryForList(
                "SELECT constraint_name FROM information_schema.table_constraints WHERE table_schema = 'public' AND constraint_type = 'FOREIGN KEY'",
                String.class
        );

        assertThat(actualConstraints).containsAll(expectedFkConstraints);
    }

    @Test
    @DisplayName("Verify simple query execution across core tables")
    void testSimpleQueryExecution() {
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        Integer productCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class);
        Integer supplierCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM suppliers", Integer.class);

        assertThat(userCount).isNotNull().isGreaterThanOrEqualTo(0);
        assertThat(productCount).isNotNull().isGreaterThanOrEqualTo(0);
        assertThat(supplierCount).isNotNull().isGreaterThanOrEqualTo(0);
    }
}
