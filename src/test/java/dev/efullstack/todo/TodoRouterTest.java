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
import static org.springframework.http.MediaType.*;

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
                //new tag
                () -> webTestClient
                        .post()
                        .uri("/todo/{userId}/tags", 1)
                        .contentType(APPLICATION_JSON)
                        .bodyValue("""
                          {
                              "name": "shopping"
                          }
                        """)
                        .exchange()
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .json("""
                            {
                                "id":1,
                                "name":"shopping"
                            }
                        """),
                () -> webTestClient
                        .post()
                        .uri("/todo/{userId}/tasks", 1)
                        .contentType(APPLICATION_JSON)
                        .bodyValue("""
                          {
                              "title": "Buy grocery",
                              "description": "Task 1",
                              "dueDate": "2021-08-01T00:00:00",
                              "tags": [
                                {
                                    "tagId": 1
                                }
                              ]
                          }
                        """)
                        .exchange()
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .json("""
                            {
                                "taskId":1,
                                "userId":1,
                                "title":"Buy grocery",
                                "description":"Task 1",
                                "status": "PENDING",
                                "dueDate":"2021-08-01T00:00:00",
                                "tags":[{"taskId":null,"tagId":1}]
                            }
                        """)

        );
    }

}
