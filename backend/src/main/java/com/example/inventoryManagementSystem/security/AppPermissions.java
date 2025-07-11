package com.example.inventoryManagementSystem.security;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class AppPermissions {
    // Dashboard
    public static final String DASHBOARD_VIEW = "dashboard_view";

    // Customer permissions
    public static final String CUSTOMER_CREATE = "customer_create";
    public static final String CUSTOMER_VIEW = "customer_view";
    public static final String CUSTOMER_UPDATE = "customer_update";
    public static final String CUSTOMER_DELETE = "customer_delete";
    public static final String CUSTOMER_SALES = "customer_sales";

    // Supplier permissions
    public static final String SUPPLIER_VIEW = "supplier_view";
    public static final String SUPPLIER_CREATE = "supplier_create";
    public static final String SUPPLIER_UPDATE = "supplier_update";
    public static final String SUPPLIER_DELETE = "supplier_delete";

    // Product permissions
    public static final String PRODUCT_CREATE = "product_create";
    public static final String PRODUCT_VIEW = "product_view";
    public static final String PRODUCT_UPDATE = "product_update";
    public static final String PRODUCT_DELETE = "product_delete";
    public static final String PRODUCT_IMPORT = "product_import";
    public static final String PRODUCT_PURCHASE = "product_purchase";

    // Brand permissions
    public static final String BRAND_CREATE = "brand_create";
    public static final String BRAND_VIEW = "brand_view";
    public static final String BRAND_UPDATE = "brand_update";
    public static final String BRAND_DELETE = "brand_delete";

    // Category permissions
    public static final String CATEGORY_CREATE = "category_create";
    public static final String CATEGORY_VIEW = "category_view";
    public static final String CATEGORY_UPDATE = "category_update";
    public static final String CATEGORY_DELETE = "category_delete";

    // Unit permissions
    public static final String UNIT_CREATE = "unit_create";
    public static final String UNIT_VIEW = "unit_view";
    public static final String UNIT_UPDATE = "unit_update";
    public static final String UNIT_DELETE = "unit_delete";

    // Sale permissions
    public static final String SALE_CREATE = "sale_create";
    public static final String SALE_VIEW = "sale_view";
    public static final String SALE_UPDATE = "sale_update";
    public static final String SALE_DELETE = "sale_delete";
    public static final String SALE_EDIT = "sale_edit";

    // Purchase permissions
    public static final String PURCHASE_CREATE = "purchase_create";
    public static final String PURCHASE_VIEW = "purchase_view";
    public static final String PURCHASE_UPDATE = "purchase_update";
    public static final String PURCHASE_DELETE = "purchase_delete";

    // Report permissions
    public static final String REPORTS_SUMMARY = "reports_summary";
    public static final String REPORTS_SALES = "reports_sales";
    public static final String REPORTS_INVENTORY = "reports_inventory";


    // Role permissions
    public static final String ROLE_CREATE = "role_create";
    public static final String ROLE_VIEW = "role_view";
    public static final String ROLE_UPDATE = "role_update";
    public static final String ROLE_DELETE = "role_delete";

    // Permission permissions
    public static final String PERMISSION_VIEW = "permission_view";

    // User permissions
    public static final String USER_CREATE = "user_create";
    public static final String USER_VIEW = "user_view";
    public static final String USER_UPDATE = "user_update";
    public static final String USER_DELETE = "user_delete";
    public static final String USER_SUSPEND = "user_suspend";

    // Settings permissions
    public static final String WEBSITE_SETTINGS = "website_settings";
    public static final String CONTACT_SETTINGS = "contact_settings";
    public static final String SOCIALS_SETTINGS = "socials_settings";
    public static final String STYLE_SETTINGS = "style_settings";
    public static final String CUSTOM_SETTINGS = "custom_settings";
    public static final String NOTIFICATION_SETTINGS = "notification_settings";
    public static final String WEBSITE_STATUS_SETTINGS = "website_status_settings";
    public static final String INVOICE_SETTINGS = "invoice_settings";

    public static Set<String> getAllPermissions() {
        return Set.of(
                DASHBOARD_VIEW,
                CUSTOMER_CREATE, CUSTOMER_VIEW, CUSTOMER_UPDATE, CUSTOMER_DELETE, CUSTOMER_SALES,
                SUPPLIER_VIEW, SUPPLIER_CREATE, SUPPLIER_UPDATE, SUPPLIER_DELETE,
                PRODUCT_CREATE, PRODUCT_VIEW, PRODUCT_UPDATE, PRODUCT_DELETE, PRODUCT_IMPORT, PRODUCT_PURCHASE,
                BRAND_CREATE, BRAND_VIEW, BRAND_UPDATE, BRAND_DELETE,
                CATEGORY_CREATE, CATEGORY_VIEW, CATEGORY_UPDATE, CATEGORY_DELETE,
                UNIT_CREATE, UNIT_VIEW, UNIT_UPDATE, UNIT_DELETE,
                SALE_CREATE, SALE_VIEW, SALE_UPDATE, SALE_DELETE, SALE_EDIT,
                PURCHASE_CREATE, PURCHASE_VIEW, PURCHASE_UPDATE, PURCHASE_DELETE,
                REPORTS_SUMMARY, REPORTS_SALES, REPORTS_INVENTORY,
                ROLE_CREATE, ROLE_VIEW, ROLE_UPDATE, ROLE_DELETE,
                PERMISSION_VIEW,
                USER_CREATE, USER_VIEW, USER_UPDATE, USER_DELETE, USER_SUSPEND,
                WEBSITE_SETTINGS, CONTACT_SETTINGS, SOCIALS_SETTINGS, STYLE_SETTINGS,
                CUSTOM_SETTINGS, NOTIFICATION_SETTINGS, WEBSITE_STATUS_SETTINGS, INVOICE_SETTINGS
        );
    }

    public static Set<String> getCashierPermissions() {
        return Set.of(
                DASHBOARD_VIEW,
                CUSTOMER_VIEW,
                PRODUCT_VIEW,
                SALE_CREATE, SALE_VIEW,
                REPORTS_SALES
        );
    }

    public static Set<String> getManagerPermissions() {
        return Set.of(
                DASHBOARD_VIEW,
                CUSTOMER_CREATE, CUSTOMER_VIEW, CUSTOMER_UPDATE,
                PRODUCT_CREATE, PRODUCT_VIEW, PRODUCT_UPDATE,
                SALE_CREATE, SALE_VIEW, SALE_UPDATE,
                PURCHASE_VIEW,
                REPORTS_SUMMARY, REPORTS_SALES, REPORTS_INVENTORY,
                USER_VIEW
        );
    }

    public static Set<String> getReceivingClerkPermissions() {
        return Set.of(
                PRODUCT_VIEW, PRODUCT_CREATE, PRODUCT_UPDATE,
                PURCHASE_CREATE, PURCHASE_VIEW,
                SUPPLIER_VIEW
        );
    }

    public static Set<String> getAdminPermissions() {
        return getAllPermissions(); // Admin has all permissions
    }
}