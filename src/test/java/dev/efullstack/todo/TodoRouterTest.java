package dev.efullstack.todo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertAll;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class TodoRouterTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Router Test")
    void routerTest() {
        assertAll(
                () -> {
                    webTestClient
                            .post()
                            .uri("/todo/1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue("""
                            """)
                            .exchange()
                            .expectStatus()
                            .isNoContent();
                },
                () -> {
                    webTestClient
                            .get()
                            .uri("/todo/1/tasks/1")
                            .exchange()
                            .expectStatus()
                            .isNoContent();
                },
                () -> {
                    webTestClient
                            .get()
                            .uri("/todo/1/tasks")
                            .exchange()
                            .expectStatus()
                            .isNoContent();
                },
                () -> {
                    webTestClient
                            .patch()
                            .uri("/todo/1/tasks/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue("""
                            """)
                            .exchange()
                            .expectStatus()
                            .isNoContent();
                },
                () -> {
                    webTestClient
                            .delete()
                            .uri("/todo/1/tasks/1")
                            .exchange()
                            .expectStatus()
                            .isNoContent();
                },
                () -> {
                    webTestClient
                            .get()
                            .uri("/todo/1/tags/1")
                            .exchange()
                            .expectStatus()
                            .isNoContent();
                },
                () -> {
                    webTestClient
                            .get()
                            .uri("/todo/1/tags")
                            .exchange()
                            .expectStatus()
                            .isNoContent();
                },
                () -> {
                    webTestClient
                            .delete()
                            .uri("/todo/1/tags/1")
                            .exchange()
                            .expectStatus()
                            .isNoContent();
                }
        );
    }

}
