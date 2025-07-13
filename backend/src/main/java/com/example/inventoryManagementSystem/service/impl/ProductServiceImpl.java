package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.request.ProductRequest;
import com.example.inventoryManagementSystem.dto.response.ProductResponse;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.model.*;
import com.example.inventoryManagementSystem.repository.*;
import com.example.inventoryManagementSystem.service.FileStorageService;
import com.example.inventoryManagementSystem.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final UnitRepository unitRepository;
    private final SupplierRepository supplierRepository;
    private final FileStorageService fileStorageService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        return createProductInternal(request, null);
    }

    @Override
    @Transactional
    public ProductResponse createProductWithImage(ProductRequest request, MultipartFile imageFile) {
        try {
            String imageUrl = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = fileStorageService.storeFile(imageFile);
            }
            return createProductInternal(request, imageUrl);
        } catch (Exception ex) {
            throw new RuntimeException("Error creating product with image", ex);
        }
    }

    private ProductResponse createProductInternal(ProductRequest request, String imageUrl) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Brand brand = request.getBrandId() != null ?
                brandRepository.findById(request.getBrandId())
                        .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId())) :
                null;

        Unit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + request.getUnitId()));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sku(request.getSku())
                .barcode(request.getBarcode())
                .price(request.getPrice())
                .costPrice(request.getCostPrice())
                .quantityInStock(request.getQuantityInStock())
                .lowStockThreshold(request.getLowStockThreshold())
                .expiryDate(request.getExpiryDate())
                .imageUrl(imageUrl)
                .supplier(supplier)
                .category(category)
                .brand(brand)
                .unit(unit)
                .build();

        Product savedProduct = productRepository.save(product);
        return mapToProductResponse(savedProduct);
    }

    @Override
    public Page<ProductResponse> getAllProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size))
                .map(this::mapToProductResponse);
    }

    @Override
    public List<ProductResponse> getAllProductsList() {
        return productRepository.findAll().stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        try {
            Product existingProduct = productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

            existingProduct.setName(request.getName());
            existingProduct.setDescription(request.getDescription());
            existingProduct.setSku(request.getSku());
            existingProduct.setBarcode(request.getBarcode());
            existingProduct.setPrice(request.getPrice());
            existingProduct.setCostPrice(request.getCostPrice());
            existingProduct.setQuantityInStock(request.getQuantityInStock());
            existingProduct.setLowStockThreshold(request.getLowStockThreshold());
            existingProduct.setExpiryDate(request.getExpiryDate());

            updateProductRelationships(existingProduct, request);

            Product updatedProduct = productRepository.save(existingProduct);
            return mapToProductResponse(updatedProduct);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Data integrity violation while updating product: " + ex.getMostSpecificCause().getMessage(), ex);
        }
    }

    @Override
    @Transactional
    public ProductResponse updateProductWithImage(Long id, ProductRequest request, MultipartFile imageFile) {
        try {
            String imageUrl = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = fileStorageService.storeFile(imageFile);
            }

            Product existingProduct = productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

            existingProduct.setName(request.getName());
            existingProduct.setDescription(request.getDescription());
            existingProduct.setSku(request.getSku());
            existingProduct.setBarcode(request.getBarcode());
            existingProduct.setPrice(request.getPrice());
            existingProduct.setCostPrice(request.getCostPrice());
            existingProduct.setQuantityInStock(request.getQuantityInStock());
            existingProduct.setLowStockThreshold(request.getLowStockThreshold());
            existingProduct.setExpiryDate(request.getExpiryDate());

            if (imageUrl != null) {
                existingProduct.setImageUrl(imageUrl);
            }

            updateProductRelationships(existingProduct, request);

            Product updatedProduct = productRepository.save(existingProduct);
            return mapToProductResponse(updatedProduct);
        } catch (Exception ex) {
            throw new RuntimeException("Error updating product with image", ex);
        }
    }

    private void updateProductRelationships(Product product, ProductRequest request) {
        if (!product.getSupplier().getId().equals(request.getSupplierId())) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));
            product.setSupplier(supplier);
        }

        if (!product.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getBrandId() != null) {
            if (product.getBrand() == null || !product.getBrand().getId().equals(request.getBrandId())) {
                Brand brand = brandRepository.findById(request.getBrandId())
                        .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));
                product.setBrand(brand);
            }
        } else {
            product.setBrand(null);
        }

        if (!product.getUnit().getId().equals(request.getUnitId())) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + request.getUnitId()));
            product.setUnit(unit);
        }
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

            // Check if product is referenced in sales
            if (productRepository.isProductReferencedInSales(id)) {
                throw new RuntimeException("Cannot delete product as it is referenced in existing sales");
            }

            // Delete directly without clearing relationships
            productRepository.delete(product);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Data integrity violation while deleting product: " + ex.getMostSpecificCause().getMessage(), ex);
        }
    }

    @Override
    public ProductResponse deleteProductImage(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setImageUrl(null);
        Product updatedProduct = productRepository.save(product);
        return mapToProductResponse(updatedProduct);
    }

    @Override
    public List<ProductResponse> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return productRepository.findAll().stream()
                    .map(this::mapToProductResponse)
                    .collect(Collectors.toList());
        }
        return productRepository.searchProducts(query).stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getLowStockProducts() {
        List<Product> products = productRepository.findLowStockProducts();
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsBySupplier(Long supplierId) {
        List<Product> products = productRepository.findBySupplierId(supplierId);
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getExpiringProducts(LocalDate thresholdDate) {
        List<Product> products = productRepository.findExpiringProducts(thresholdDate);
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void importProducts(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                ProductRequest request = ProductRequest.builder()
                        .name(getCellStringValue(row.getCell(0)))
                        .description(getCellStringValue(row.getCell(1)))
                        .sku(getCellStringValue(row.getCell(2)))
                        .barcode(getCellStringValue(row.getCell(3)))
                        .price(getCellNumericValue(row.getCell(4)))
                        .costPrice(getCellNumericValue(row.getCell(5)))
                        .quantityInStock((int) getCellNumericValue(row.getCell(6)))
                        .lowStockThreshold((int) getCellNumericValue(row.getCell(7)))
                        .supplierId((long) getCellNumericValue(row.getCell(8)))
                        .categoryId((long) getCellNumericValue(row.getCell(9)))
                        .brandId((long) getCellNumericValue(row.getCell(10)))
                        .unitId((long) getCellNumericValue(row.getCell(11)))
                        .build();

                createProduct(request);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to import products: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] exportProducts() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Products");

            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "ID", "Name", "Description", "SKU", "Barcode",
                    "Price", "Cost Price", "Quantity", "Low Stock Threshold",
                    "Supplier ID", "Supplier Name",
                    "Category ID", "Category Name",
                    "Brand ID", "Brand Name",
                    "Unit ID", "Unit Name", "Expiry Date", "Image URL"
            };
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            List<Product> products = productRepository.findAll();
            for (int i = 0; i < products.size(); i++) {
                Product product = products.get(i);
                Row row = sheet.createRow(i + 1);

                int cellNum = 0;
                row.createCell(cellNum++).setCellValue(product.getId());
                row.createCell(cellNum++).setCellValue(product.getName());
                row.createCell(cellNum++).setCellValue(product.getDescription());
                row.createCell(cellNum++).setCellValue(product.getSku());
                row.createCell(cellNum++).setCellValue(product.getBarcode());
                row.createCell(cellNum++).setCellValue(product.getPrice());
                row.createCell(cellNum++).setCellValue(product.getCostPrice());
                row.createCell(cellNum++).setCellValue(product.getQuantityInStock());
                row.createCell(cellNum++).setCellValue(product.getLowStockThreshold());

                if (product.getSupplier() != null) {
                    row.createCell(cellNum++).setCellValue(product.getSupplier().getId());
                    row.createCell(cellNum++).setCellValue(product.getSupplier().getCompanyName());
                } else {
                    cellNum += 2;
                }

                if (product.getCategory() != null) {
                    row.createCell(cellNum++).setCellValue(product.getCategory().getId());
                    row.createCell(cellNum++).setCellValue(product.getCategory().getName());
                } else {
                    cellNum += 2;
                }

                if (product.getBrand() != null) {
                    row.createCell(cellNum++).setCellValue(product.getBrand().getId());
                    row.createCell(cellNum++).setCellValue(product.getBrand().getName());
                } else {
                    cellNum += 2;
                }

                if (product.getUnit() != null) {
                    row.createCell(cellNum++).setCellValue(product.getUnit().getId());
                    row.createCell(cellNum++).setCellValue(product.getUnit().getName());
                } else {
                    cellNum += 2;
                }

                if (product.getExpiryDate() != null) {
                    row.createCell(cellNum++).setCellValue(product.getExpiryDate().toString());
                }

                row.createCell(cellNum).setCellValue(product.getImageUrl() != null ? product.getImageUrl() : "");
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export products: " + e.getMessage(), e);
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    private double getCellNumericValue(Cell cell) {
        if (cell == null) return 0;
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0;
                }
            default:
                return 0;
        }
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .barcode(product.getBarcode())
                .price(product.getPrice())
                .costPrice(product.getCostPrice())
                .quantityInStock(product.getQuantityInStock())
                .lowStockThreshold(product.getLowStockThreshold())
                .expiryDate(product.getExpiryDate())
                .imageUrl(product.getImageUrl())
                .supplierId(product.getSupplier() != null ? product.getSupplier().getId() : null)
                .supplierName(product.getSupplier() != null ? product.getSupplier().getCompanyName() : null)
                .supplierContactPerson(product.getSupplier() != null ? product.getSupplier().getContactPerson() : null)
                .supplierEmail(product.getSupplier() != null ? product.getSupplier().getEmail() : null)
                .supplierPhone(product.getSupplier() != null ? product.getSupplier().getPhone() : null)
                .supplierAddress(product.getSupplier() != null ? product.getSupplier().getAddress() : null)
                .supplierWebsite(product.getSupplier() != null ? product.getSupplier().getWebsite() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .unitId(product.getUnit() != null ? product.getUnit().getId() : null)
                .unitName(product.getUnit() != null ? product.getUnit().getName() : null)
                .unitAbbreviation(product.getUnit() != null ? product.getUnit().getAbbreviation() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}