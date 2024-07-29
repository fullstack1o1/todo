package dev.efullstack.todo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class TodoApplicationTests {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private TagRepository tagRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void repositoryTest() {
		assertAll(
				//user assertion
				() -> {
					var allUser = userRepository.findAll();
					System.out.println(allUser);
					assertNotNull(allUser);
					assertEquals(2, allUser.size());
				},
				//task assertion
				() -> {
					Task task = new Task();
					task.setUserId(1L);
					task.setTitle("TITLE");
					task.setDescription("DESCRIPTION");
					task.setStatus(Task.TaskStatus.PENDING);
					task.setDueDate(LocalDateTime.now());

					Tag tag = new Tag();
					tag.setName("TAG");
					tag.setUserId(1L);

//					Tag tag1 = new Tag();
//					tag1.setName("TAG1");
//					tag1.setUserId(1L);

					task.setTags(
							Set.of(tag)
					);

					var newTask = taskRepository.save(task);
					System.out.println(newTask);
					assertNotNull(newTask);
					assertNotNull(newTask.getTaskId());
				},
				() -> {
					var task = taskRepository.findById(1L);
					assertNotNull(task);
					System.out.println(task);
				},
				() -> {
					var allTask = taskRepository.findAll();
					System.out.println(allTask);
					assertNotNull(allTask);
					assertEquals(1, allTask.size());
				},
				//tag assertion
				() -> {
					var allTag = tagRepository.findAll();
					System.out.println(allTag);
				}

		);
	}
}
