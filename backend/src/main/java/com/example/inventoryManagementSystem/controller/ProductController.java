package com.example.inventoryManagementSystem.controller;

import com.example.inventoryManagementSystem.dto.request.ProductRequest;
import com.example.inventoryManagementSystem.dto.response.ProductResponse;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @GetMapping
    public ResponseEntity<?> getAllProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            if (page == null || size == null) {
                // Return all products if pagination parameters are not provided
                List<ProductResponse> products = productService.getAllProductsList();
                return ResponseEntity.ok(products);
            } else {
                // Return paginated results if parameters are provided
                Page<ProductResponse> products = productService.getAllProducts(page, size);
                return ResponseEntity.ok(products);
            }
        } catch (Exception e) {
            logger.error("Error fetching products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching products: " + e.getMessage());
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @Valid @RequestPart("request") ProductRequest request,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            logger.info("Creating product with request: {}", request);
            ProductResponse response;
            if (imageFile != null && !imageFile.isEmpty()) {
                logger.info("Processing product with image: {}", imageFile.getOriginalFilename());
                response = productService.createProductWithImage(request, imageFile);
            } else {
                response = productService.createProduct(request);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid request data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating product: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @Valid @RequestPart("request") ProductRequest request,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            ProductResponse response;
            if (imageFile != null && !imageFile.isEmpty()) {
                response = productService.updateProductWithImage(id, request, imageFile);
            } else {
                response = productService.updateProduct(id, request);
            }
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException ex) {
            logger.warn("Product not found: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error updating product: id={}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating product: " + ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.getProductById(id));
        } catch (ResourceNotFoundException ex) {
            logger.warn("Product not found: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error fetching product: id={}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching product: " + ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException ex) {
            logger.warn("Product not found: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error deleting product: id={}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting product: " + ex.getMessage());
        }
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<?> deleteProductImage(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.deleteProductImage(id));
        } catch (ResourceNotFoundException ex) {
            logger.warn("Product image not found: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error deleting product image: id={}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting product image: " + ex.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(
            @RequestParam(required = false) String query) {
        try {
            return ResponseEntity.ok(productService.searchProducts(query));
        } catch (Exception ex) {
            logger.error("Error searching products: query={}", query, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching products: " + ex.getMessage());
        }
    }

    @GetMapping("/low-stock")
    public ResponseEntity<?> getLowStockProducts(
            @RequestParam(required = false) Integer threshold) {
        try {
            if (threshold != null) {
                // Implement this method in your ProductService if you want custom threshold
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                        .body("Custom threshold not implemented yet");
            }
            return ResponseEntity.ok(productService.getLowStockProducts());
        } catch (Exception ex) {
            logger.error("Error fetching low stock products", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching low stock products: " + ex.getMessage());
        }
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<?> getProductsBySupplier(
            @PathVariable Long supplierId) {
        try {
            return ResponseEntity.ok(productService.getProductsBySupplier(supplierId));
        } catch (Exception ex) {
            logger.error("Error fetching products by supplier: supplierId={}", supplierId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching products by supplier: " + ex.getMessage());
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(
            @PathVariable Long categoryId) {
        try {
            return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
        } catch (Exception ex) {
            logger.error("Error fetching products by category: categoryId={}", categoryId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching products by category: " + ex.getMessage());
        }
    }

    @GetMapping("/expiring")
    public ResponseEntity<?> getExpiringProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate thresholdDate) {
        try {
            LocalDate defaultThreshold = LocalDate.now().plusDays(100);
            LocalDate effectiveThreshold = thresholdDate != null ? thresholdDate : defaultThreshold;
            return ResponseEntity.ok(productService.getExpiringProducts(effectiveThreshold));
        } catch (Exception ex) {
            logger.error("Error fetching expiring products: thresholdDate={}", thresholdDate, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching expiring products: " + ex.getMessage());
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        logger.warn("Validation errors: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}