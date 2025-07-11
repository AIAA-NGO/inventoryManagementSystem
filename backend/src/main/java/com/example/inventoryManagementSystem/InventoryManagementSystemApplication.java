package com.example.inventoryManagementSystem;

import com.example.inventoryManagementSystem.service.RolePermissionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class InventoryManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryManagementSystemApplication.class, args);
	}

	@Bean
	CommandLineRunner init(RolePermissionService rolePermissionService) {
		return args -> {
			rolePermissionService.initializeRolesAndPermissions();
		};
	}

}
