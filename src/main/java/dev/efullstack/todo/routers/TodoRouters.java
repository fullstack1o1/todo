package dev.efullstack.todo.routers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@Slf4j
public class TodoRouters {
    @Bean
    RouterFunction<ServerResponse> routes(TodoHandler todoHandler) {
        return RouterFunctions
                .route()
                .before(request -> {
                    log.info("REQUEST {} {}", request.method(), request.path());
                    return request;
                })
                .path("/todo/{userId}", builder -> builder
                        .POST("/tasks", todoHandler::createTask)
                        .GET("/tasks/{taskId}", todoHandler::findTaskById)
                        .GET("/tasks", todoHandler::tasks)
                        .PATCH("/tasks/{taskId}", todoHandler::updateTask)
                        .DELETE("/tasks/{taskId}", todoHandler::deleteTask)
                        .POST("/tags", todoHandler::newTags)
                        .GET("/tags/{tagId}", todoHandler::tagById)
                        .GET("/tags/{tagId}/tasks", todoHandler::tasksByTagId)
                        .PATCH("/tags/{tagId}", todoHandler::updateTag)
                        .GET("/tags", todoHandler::tags)
                        .DELETE("/tags/{tagId}", request -> ServerResponse.noContent().build())
                )
                .after((request, response) -> {
                    log.info("RESPONSE {} {} {}", request.method(), request.path(), response.statusCode());
                    return response;
                })
                .build();
    }
}
