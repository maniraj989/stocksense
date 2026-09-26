package com.stocksense.barcode.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.stocksense.product.dto.ProductResponse;
import com.stocksense.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BarcodeService {

    private final ProductRepository productRepository;

    public BarcodeService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public byte[] generateBarcodePng(String productCode, int width, int height) {
        if (!isValidBarcode(productCode)) {
            throw new IllegalArgumentException("Invalid product code for barcode generation: " + productCode);
        }

        try {
            Code128Writer barcodeWriter = new Code128Writer();
            BitMatrix bitMatrix = barcodeWriter.encode(productCode.trim(), BarcodeFormat.CODE_128, width, height);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate barcode image: " + e.getMessage(), e);
        }
    }

    public boolean isValidBarcode(String productCode) {
        if (productCode == null || productCode.trim().isEmpty()) {
            return false;
        }
        String clean = productCode.trim();
        return clean.length() <= 50 && clean.matches("^[\\x20-\\x7E]+$");
    }

    public Optional<ProductResponse> findProductByBarcode(String barcode) {
        if (!isValidBarcode(barcode)) {
            return Optional.empty();
        }
        return productRepository.findByProductCode(barcode.trim())
                .map(ProductResponse::fromEntity);
    }
}
