package com.example.inventoryManagementSystem.repository;

import com.example.inventoryManagementSystem.model.Product;
import com.example.inventoryManagementSystem.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.barcode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchProducts(@Param("query") String query);

    List<Product> findByExpiryDateBefore(LocalDate date);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.expiryDate < :date")
    long countByExpiryDateBefore(@Param("date") LocalDate date);

    @Query("SELECT p FROM Product p WHERE p.quantityInStock <= p.lowStockThreshold")
    List<Product> findLowStockProducts();

    List<Product> findBySupplier(Supplier supplier);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.supplier = :supplier")
    long countBySupplier(@Param("supplier") Supplier supplier);

    List<Product> findBySupplierId(Long supplierId);

    List<Product> findByCategoryId(Long categoryId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantityInStock <= p.lowStockThreshold")
    long countLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.expiryDate BETWEEN CURRENT_DATE AND :thresholdDate")
    List<Product> findExpiringProducts(@Param("thresholdDate") LocalDate thresholdDate);

    @Query("SELECT COALESCE(MIN(p.lowStockThreshold), 10) FROM Product p")
    Optional<Integer> findSystemLowStockThreshold();

    @Query("SELECT p FROM Product p WHERE p.quantityInStock <= :threshold")
    List<Product> findByQuantityInStockLessThanEqual(@Param("threshold") int threshold);

    @Query("SELECT p FROM Product p WHERE p.expiryDate BETWEEN :startDate AND :endDate")
    List<Product> findByExpiryDateBetween(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(p) FROM Product p")
    long countAllProducts();

    @Query(value = "SELECT p FROM Product p ORDER BY p.quantityInStock ASC LIMIT :limit", nativeQuery = true)
    List<Product> findTopLowStockProducts(@Param("limit") int limit);

    long countByQuantityInStockLessThanEqual(int threshold);

    @Query("SELECT CASE WHEN COUNT(si) > 0 THEN true ELSE false END " +
            "FROM SaleItem si WHERE si.product.id = :productId")
    boolean isProductReferencedInSales(@Param("productId") Long productId);

    boolean existsBySku(String sku);
    boolean existsByBarcode(String barcode);
}