package com.nihat.springwebfluxdemo.controllers.mvc;

import com.nihat.springwebfluxdemo.model.ProductDTO;
import com.nihat.springwebfluxdemo.services.ProductService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ProductService productService;

    @Test
    @Order(1)
    void testGetAllProducts() {
        // When
        webTestClient.get().uri(ProductController.PRODUCT_BASE_URL)
                .exchange()
                // Then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductDTO.class).hasSize(4); // initially loaded + testProduct
    }

    @Test
    @Order(2)
    void testGetProductByIdSuccess() {
        // Given
        String productId = getSavedTestProduct().getId();

        // When
        webTestClient.get().uri(ProductController.PRODUCT_ID_URL, productId)
                .exchange()
                // Then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ProductDTO.class);
    }

    @Test
    @Order(3)
    void testGetProductByIdNotFound() {
        // Given
        String nonExistentProductId = "non_existent_id";

        // When
        webTestClient.get().uri(ProductController.PRODUCT_ID_URL, nonExistentProductId)
                .exchange()
                // Then
                .expectStatus().isNotFound();
    }

    @Test
    @Order(4)
    void testCreateProduct() {
        // Given
        ProductDTO newProductDTO = new ProductDTO();
        newProductDTO.setName("New Product");
        newProductDTO.setDescription("New Product");
        newProductDTO.setImgUrl("https://image.com/image.jpeg");
        newProductDTO.setPrice(BigDecimal.TEN);
        // Set other fields as needed

        // When
        webTestClient.post().uri(ProductController.PRODUCT_BASE_URL)
                .body(Mono.just(newProductDTO), ProductDTO.class)
                .exchange()
                // Then
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ProductDTO.class)
                .value(created -> {
                    assertThat(created.getName().equals(newProductDTO.getName())).isTrue();
                }); // Define expectedProductDTO based on test data
    }

    @Test
    void testCreateProductBadRequest() {
        // Given
        ProductDTO invalidProductDTO = new ProductDTO();
        invalidProductDTO.setName(null); // Set name to null to simulate a bad request
        invalidProductDTO.setImgUrl("https://image.com/image.jpeg");
        invalidProductDTO.setDescription("New Product");
        invalidProductDTO.setPrice(BigDecimal.TEN);
        // Set other fields as needed

        // When
        webTestClient.post().uri(ProductController.PRODUCT_BASE_URL)
                .body(Mono.just(invalidProductDTO), ProductDTO.class)
                .exchange()
                // Then
                .expectStatus().isBadRequest();
    }

    @Test
    void testUpdateProductSuccess() {
        // Given
        ProductDTO updatedProductDTO = getSavedTestProduct();
        updatedProductDTO.setName("UPDATED name");

        String productId = updatedProductDTO.getId();


        // When
        webTestClient.put().uri(ProductController.PRODUCT_ID_URL, productId)
                .body(Mono.just(updatedProductDTO), ProductDTO.class)
                .exchange()
                // Then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ProductDTO.class)
                .value(dto -> {
                    assertThat(dto.getName().equals(updatedProductDTO.getName())).isTrue();
                });
    }

    @Test
    void testUpdateProductNotFound() {
        // Given
        String nonExistentProductId = "non_existent_id";
        ProductDTO updatedProductDTO = new ProductDTO();
        updatedProductDTO.setName("Updated Product");
        updatedProductDTO.setDescription("Updated Product");
        updatedProductDTO.setImgUrl("https://image.com/image.jpeg");
        updatedProductDTO.setPrice(BigDecimal.TEN);


        // When
        webTestClient.put().uri(ProductController.PRODUCT_ID_URL, nonExistentProductId)
                .body(Mono.just(updatedProductDTO), ProductDTO.class)
                .exchange()
                // Then
                .expectStatus().isNotFound();
    }

    @Test
    @Order(99)
    void testDeleteProduct() {
        // Given
        String productIdToDelete = "1"; // Existing product ID

        // When
        webTestClient.delete().uri(ProductController.PRODUCT_ID_URL, productIdToDelete)
                .exchange()
                // Then
                .expectStatus().isNoContent();
    }

    private ProductDTO getSavedTestProduct() {
        return productService.getAllProducts().next().block();
    }


}