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
                        .GET("/notification", todoHandler::notification)
                        .POST("/tasks", todoHandler::createTask)
                        .GET("/tasks/{taskId}", todoHandler::findTaskById)
                        .GET("/tasks", todoHandler::tasks)
                        .PUT("/tasks/{taskId}", todoHandler::updateTask)
                        .PATCH("/tasks/{taskId}", todoHandler::patchTask)
                        .DELETE("/tasks/{taskId}", todoHandler::deleteTask)
                        .POST("/tags", todoHandler::newTags)
                        .GET("/tags/{tagId}", todoHandler::tagById)
                        .GET("/tags/{tagId}/tasks", todoHandler::tasksByTagId)
                        .PUT("/tags/{tagId}",todoHandler::updateTag)
                        .PATCH("/tags/{tagId}", todoHandler::patchTag)
                        .GET("/tags", todoHandler::tags)
                        .DELETE("/tags/{tagId}", todoHandler::deleteTag)
                )
                .after((request, response) -> {
                    log.info("RESPONSE {} {} {}", request.method(), request.path(), response.statusCode());
                    return response;
                })
                .build();
    }
}
