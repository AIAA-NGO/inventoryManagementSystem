package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.request.ProductRequest;
import com.example.inventoryManagementSystem.dto.response.ProductResponse;
import com.example.inventoryManagementSystem.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getAllProducts(page, size));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> createProductWithJson(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));

    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> updateProductWithJson(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @ModelAttribute ProductRequest request,
            @RequestParam(required = false) MultipartFile image) {
        if (image != null && !image.isEmpty()) {
            request.setImageFile(image);
        }
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping(value = "/{id}/image", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        if (product.getImageData() == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType contentType = product.getImageContentType() != null
                ? MediaType.parseMediaType(product.getImageContentType())
                : MediaType.IMAGE_JPEG;

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"product_" + id + "\"")
                .body(product.getImageData());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductRequest request,
            @RequestParam(required = false) MultipartFile image) {
        if (image != null && !image.isEmpty()) {
            request.setImageFile(image);
        }
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<ProductResponse> deleteProductImage(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deleteProductImage(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(productService.searchProducts(query));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponse>> getLowStockProducts() {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<ProductResponse>> getProductsBySupplier(
            @PathVariable Long supplierId) {
        return ResponseEntity.ok(productService.getProductsBySupplier(supplierId));
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importProducts(@RequestParam("file") MultipartFile file) {
        productService.importProducts(file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportProducts() {
        byte[] exportedData = productService.exportProducts();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=products_export.xlsx")
                .body(exportedData);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<ProductResponse>> getExpiringProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate thresholdDate) {
        LocalDate defaultThreshold = LocalDate.now().plusDays(100);
        LocalDate effectiveThreshold = thresholdDate != null ? thresholdDate : defaultThreshold;
        return ResponseEntity.ok(productService.getExpiringProducts(effectiveThreshold));
    }
}