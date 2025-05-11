package dev.efullstack.todo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class TodoRouterTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Router Test")
    void routerTest() {
        var userId = 1L;

        assertAll(
                //TAG - POST
                () -> webTestClient
                        .post()
                        .uri("/todo/{userId}/tags", userId)
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
                //TAG - GET
                () -> webTestClient
                        .get()
                        .uri("/todo/{userId}/tags/{tagId}", userId, 1)
                        .exchange()
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .json("""
                            {
                                "id":1,
                                "userId": 1,
                                "name":"shopping"
                            }
                        """),
                //TAG - PUT
                () -> webTestClient
                        .put()
                        .uri("/todo/{userId}/tags/{tagId}", userId, 1)
                        .contentType(APPLICATION_JSON)
                        .bodyValue("""
                                {
                                    "id":1,
                                    "userId": 1,
                                    "name":"shopping-u"
                                }
                                """)
                        .exchange()
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .json("""
                            {
                                "id":1,
                                "userId": 1,
                                "name":"shopping-u"
                            }
                        """),
                //TAG - PATCH
                () -> webTestClient
                        .patch()
                        .uri("/todo/{userId}/tags/{tagId}", userId, 1)
                        .contentType(APPLICATION_JSON)
                        .bodyValue("""
                                {
                                    "name":"Shopping"
                                }
                                """)
                        .exchange()
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .json("""
                            {
                                "id":1,
                                "userId": 1,
                                "name":"Shopping"
                            }
                        """),
                //TAG - GET
                () -> webTestClient
                        .get()
                        .uri("/todo/{userId}/tags/{tagId}", userId, 2)
                        .exchange()
                        .expectStatus()
                        .isNotFound(),
                //TAG delete
                /*() -> webTestClient
                        .delete()
                        .uri("/todo/{userId}/tags/{tagId}", userId, 1)
                        .exchange()
                        .expectStatus()
                        .isOk(),*/
                //TASK - POST
                () -> webTestClient
                        .post()
                        .uri("/todo/{userId}/tasks", userId)
                        .contentType(APPLICATION_JSON)
                        .bodyValue("""
                          {
                              "title": "Buy grocery",
                              "description": "Task 1",
                              "date": "2021-08-01",
                              "time": "00:00:15",
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
                                "date":"2021-08-01",
                                "time": "00:00:15",
                                "tags": [{"taskId":null,"tagId":1}]
                            }
                        """),
                //TASK by tagId
                () -> webTestClient
                        .get()
                        .uri("/todo/{userId}/tags/{tagId}/tasks", userId, 1)
                        .exchange()
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .json("""
                            [
                              {
                                "taskId":1,
                                "userId":1,
                                "title":"Buy grocery",
                                "description":"Task 1",
                                "status":"PENDING",
                                "date":"2021-08-01",
                                "time": "00:00:15",
                                "tags":[{"taskId":1,"tagId":1}]
                              }
                            ]
                        """),
                //TASK - PUT
                () -> webTestClient
                        .put()
                        .uri("/todo/{userId}/tasks/{taskId}", userId, 1)
                        .contentType(APPLICATION_JSON)
                        .bodyValue("""
                          {
                            "taskId":1,
                            "title":"Buy grocery-u",
                            "description":"Task 1",
                            "status":"PENDING",
                            "date":"2021-08-01",
                            "time": "00:00:15",
                            "tags":[{"taskId":1,"tagId":1}]
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
                                "title":"Buy grocery-u",
                                "description":"Task 1",
                                "status": "PENDING",
                                "date":"2021-08-01",
                                "time": "00:00:15",
                                "tags":[{"taskId":1,"tagId":1}]
                            }
                        """),
                //TASK - PATCH
                () -> webTestClient
                        .patch()
                        .uri("/todo/{userId}/tasks/{taskId}", userId, 1)
                        .contentType(APPLICATION_JSON)
                        .bodyValue("""
                          {
                              "title": "Buy Grocery"
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
                                "title":"Buy Grocery",
                                "description":"Task 1",
                                "status": "PENDING",
                                "date":"2021-08-01",
                                "time": "00:00:15",
                                "tags":[{"taskId":1,"tagId":1}]
                            }
                        """),
                //TASK - PATCH
                () -> webTestClient
                        .patch()
                        .uri("/todo/{userId}/tasks/{taskId}", userId, 2)
                        .contentType(APPLICATION_JSON)
                        .bodyValue("""
                          {
                              "title": "no content"
                          }
                        """)
                        .exchange()
                        .expectStatus()
                        .isNotFound()
                        .expectBody()
                        .jsonPath("$.path").isEqualTo("/todo/1/tasks/2")
                        .jsonPath("$.status").isEqualTo(404),
                //TASK - byId
                () -> webTestClient
                        .get()
                        .uri("/todo/{userId}/tasks/{taskId}", userId, 1)
                        .exchange()
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .json("""
{
                                "taskId":1,
                                "userId":1,
                                "title":"Buy Grocery",
                                "description":"Task 1",
                                "status":"PENDING",
                                "date":"2021-08-01",
                                "time": "00:00:15",
                                "tags":[{"taskId":1,"tagId":1}]
                            }
                         """),
                //TASK - GET
                () -> webTestClient
                        .get()
                        .uri("/todo/{userId}/tasks", userId)
                        .exchange()
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .json("""
                          [
                            {
                                "taskId":1,
                                "userId":1,
                                "title":"Buy Grocery",
                                "description":"Task 1",
                                "status":"PENDING",
                                "date":"2021-08-01",
                                "time": "00:00:15",
                                "tags":[{"taskId":1,"tagId":1}]
                            }
                          ]
                        """)

        );
    }
}
