package dev.efullstack.todo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TodoApplicationTests {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TaskRepository taskRepository;


	@Test
	void contextLoads() {
	}

	@Test
	void dbEntityTest() {
		assertAll(
				() -> {
					var allUser = userRepository.findAll();
					System.out.println(allUser);
					assertNotNull(allUser);
					assertEquals(2, allUser.size());
				},
				() -> {
					var allTask = taskRepository.findAll();
					System.out.println(allTask);
					assertNotNull(allTask);
					assertEquals(0, allTask.size());
				}
		);
	}
}
