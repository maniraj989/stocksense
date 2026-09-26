package com.stocksense.product.service;

import com.stocksense.common.exception.BusinessException;
import com.stocksense.common.exception.DuplicateResourceException;
import com.stocksense.common.exception.ResourceNotFoundException;
import com.stocksense.product.dto.SupplierRequest;
import com.stocksense.product.dto.SupplierResponse;
import com.stocksense.product.entity.Supplier;
import com.stocksense.product.repository.ProductRepository;
import com.stocksense.product.repository.SupplierRepository;
import com.stocksense.purchase.repository.PurchaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;

    public SupplierService(SupplierRepository supplierRepository,
                           ProductRepository productRepository,
                           PurchaseRepository purchaseRepository) {
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.purchaseRepository = purchaseRepository;
    }

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public Page<SupplierResponse> findAll(Pageable pageable) {
        return supplierRepository.findAll(pageable)
                .map(SupplierResponse::fromEntity);
    }

    public Optional<Supplier> findById(Long id) {
        return supplierRepository.findById(id);
    }

    public SupplierResponse getById(Long id) {
        return supplierRepository.findById(id)
                .map(SupplierResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    public List<Supplier> searchByName(String name) {
        return supplierRepository.findByNameContainingIgnoreCase(name);
    }

    public Page<SupplierResponse> search(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return findAll(pageable);
        }
        return supplierRepository.findByNameContainingIgnoreCaseOrCompanyContainingIgnoreCase(query, query, pageable)
                .map(SupplierResponse::fromEntity);
    }

    @Transactional
    public Supplier save(Supplier supplier) {
        if (supplier.getId() == null) {
            if (supplierRepository.existsByName(supplier.getName())) {
                throw new DuplicateResourceException("Supplier with name '" + supplier.getName() + "' already exists");
            }
        } else {
            if (supplierRepository.existsByNameAndIdNot(supplier.getName(), supplier.getId())) {
                throw new DuplicateResourceException("Supplier with name '" + supplier.getName() + "' already exists");
            }
        }
        return supplierRepository.save(supplier);
    }

    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        if (supplierRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Supplier with name '" + request.getName() + "' already exists");
        }

        Supplier supplier = new Supplier(
                request.getName(),
                request.getCompany(),
                request.getPhone(),
                request.getEmail(),
                request.getAddress()
        );
        Supplier saved = supplierRepository.save(supplier);
        return SupplierResponse.fromEntity(saved);
    }

    @Transactional
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        if (supplierRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("Supplier with name '" + request.getName() + "' already exists");
        }

        supplier.setName(request.getName());
        supplier.setCompany(request.getCompany());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());

        Supplier saved = supplierRepository.save(supplier);
        return SupplierResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier not found with id: " + id);
        }

        if (productRepository.existsBySupplierId(id)) {
            throw new BusinessException("Cannot delete supplier because existing products are associated with it");
        }

        if (purchaseRepository.existsBySupplierId(id)) {
            throw new BusinessException("Cannot delete supplier because existing purchase records reference it");
        }

        supplierRepository.deleteById(id);
    }
}
