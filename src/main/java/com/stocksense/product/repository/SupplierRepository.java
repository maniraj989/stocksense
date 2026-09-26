package com.stocksense.product.repository;

import com.stocksense.product.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByNameContainingIgnoreCase(String name);

    List<Supplier> findByCompanyContainingIgnoreCase(String company);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    org.springframework.data.domain.Page<Supplier> findByNameContainingIgnoreCaseOrCompanyContainingIgnoreCase(String name, String company, org.springframework.data.domain.Pageable pageable);
}
