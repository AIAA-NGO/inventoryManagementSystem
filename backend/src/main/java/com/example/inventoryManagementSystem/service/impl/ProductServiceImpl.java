package com.example.inventoryManagementSystem.service.impl;

import com.example.inventoryManagementSystem.dto.request.ProductRequest;
import com.example.inventoryManagementSystem.dto.response.ProductResponse;
import com.example.inventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.inventoryManagementSystem.model.*;
import com.example.inventoryManagementSystem.repository.*;
import com.example.inventoryManagementSystem.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;


    //to be reviewed
    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Supplier supplier = request.getSupplierId() != null ?
                supplierRepository.findById(request.getSupplierId())
                        .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId())) :
                null;

        Category category = request.getCategoryId() != null ?
                categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId())) :
                null;

        Brand brand = request.getBrandId() != null ?
                brandRepository.findById(request.getBrandId())
                        .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId())) :
                null;

        Unit unit = request.getUnitId() != null ?
                unitRepository.findById(request.getUnitId())
                        .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + request.getUnitId())) :
                null;

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setBarcode(request.getBarcode());
        product.setPrice(request.getPrice());
        product.setCostPrice(request.getCostPrice());
        product.setQuantityInStock(request.getQuantityInStock());
        product.setLowStockThreshold(request.getLowStockThreshold());
        product.setExpiryDate(request.getExpiryDate());


        product.setSupplier(supplier);
        product.setCategory(category);
        product.setBrand(brand);
        product.setUnit(unit);

        Product savedProduct = productRepository.save(product);
        return mapToProductResponse(savedProduct);
    }

    @Override
    public Page<ProductResponse> getAllProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size))
                .map(this::mapToProductResponse);
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

        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));
//            existingProduct.setSupplier(supplier);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            existingProduct.setCategory(category);
        }

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));
            existingProduct.setBrand(brand);
        }

        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + request.getUnitId()));
            existingProduct.setUnit(unit);
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return mapToProductResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
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
    @Transactional
    public void importProducts(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                ProductRequest request = new ProductRequest();
                request.setName(getCellStringValue(row.getCell(0)));
                request.setDescription(getCellStringValue(row.getCell(1)));
                request.setSku(getCellStringValue(row.getCell(2)));
                request.setBarcode(getCellStringValue(row.getCell(3)));
                request.setPrice(getCellNumericValue(row.getCell(4)));
                request.setCostPrice(getCellNumericValue(row.getCell(5)));
                request.setQuantityInStock((int) getCellNumericValue(row.getCell(6)));
                request.setLowStockThreshold((int) getCellNumericValue(row.getCell(7)));
                request.setSupplierId((long) getCellNumericValue(row.getCell(8)));
                request.setCategoryId((long) getCellNumericValue(row.getCell(9)));
                request.setBrandId((long) getCellNumericValue(row.getCell(10)));
                request.setUnitId((long) getCellNumericValue(row.getCell(11)));

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

            String[] headers = {
                    "ID", "Name", "Description", "SKU", "Barcode",
                    "Price", "Cost Price", "Quantity", "Low Stock Threshold",
                    "Supplier ID", "Supplier Name",
                    "Category ID", "Category Name",
                    "Brand ID", "Brand Name",
                    "Unit ID", "Unit Name"
            };

            Row headerRow = sheet.createRow(0);
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
                    row.createCell(cellNum).setCellValue(product.getUnit().getName());
                }
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
                return String.valueOf(cell.getNumericCellValue());
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

    @Override
    public List<ProductResponse> getProductsBySupplier(Long supplierId) {
        List<Product> products = productRepository.findBySupplierId(supplierId);
        return products.stream()
                .map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return products.stream()
                .map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getExpiringProducts(LocalDate thresholdDate) {
        List<Product> products = productRepository.findByExpiryDateBefore(thresholdDate);
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
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

                // Supplier details
//                .supplierId(product.getSupplier() != null ? product.getSupplier().getId() : null)
//                .supplierName(product.getSupplier() != null ? product.getSupplier().getCompanyName() : null)
//                .supplierContactPerson(product.getSupplier() != null ? product.getSupplier().getContactPerson() : null)
//                .supplierEmail(product.getSupplier() != null ? product.getSupplier().getEmail() : null)
//                .supplierPhone(product.getSupplier() != null ? product.getSupplier().getPhone() : null)
//                .supplierAddress(product.getSupplier() != null ? product.getSupplier().getAddress() : null)
//                .supplierWebsite(product.getSupplier() != null ? product.getSupplier().getWebsite() : null)

                // Category details
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)

                // Brand details
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)

                // Unit details
                .unitId(product.getUnit() != null ? product.getUnit().getId() : null)
                .unitName(product.getUnit() != null ? product.getUnit().getName() : null)
                .unitAbbreviation(product.getUnit() != null ? product.getUnit().getAbbreviation() : null)

                // Timestamps
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    @Override
    public ProductResponse deleteProductImage(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setImageData(null);
        Product updatedProduct = productRepository.save(product);

        // Manual mapping from Product to ProductResponse
        return mapProductToResponse(updatedProduct);
    }

    private ProductResponse mapProductToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setSku(product.getSku());
        response.setBarcode(product.getBarcode());
        response.setPrice(product.getPrice());
        response.setCostPrice(product.getCostPrice());
        response.setQuantityInStock(product.getQuantityInStock());
        response.setLowStockThreshold(product.getLowStockThreshold());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        response.setExpiryDate(product.getExpiryDate());
        response.setImageData(product.getImageData()); // Will be null after deletion

        // Map relationships (IDs only)
        if (product.getSupplier() != null) {
            response.setSupplierId(product.getSupplier().getId());
        }
        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
        }
        if (product.getBrand() != null) {
            response.setBrandId(product.getBrand().getId());
        }
        if (product.getUnit() != null) {
            response.setUnitId(product.getUnit().getId());
        }

        return response;
    }

}