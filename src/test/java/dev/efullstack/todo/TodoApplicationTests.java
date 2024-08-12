package dev.efullstack.todo;

import dev.efullstack.todo.models.Tag;
import dev.efullstack.todo.models.Task;
import dev.efullstack.todo.models.TaskTag;
import dev.efullstack.todo.repositories.TagRepository;
import dev.efullstack.todo.repositories.TaskRepository;
import dev.efullstack.todo.repositories.TaskTagRepository;
import dev.efullstack.todo.repositories.UserRepository;
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

	@Autowired
	private TaskTagRepository taskTagRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void repositoryTest() {
		assertAll(
				//task assertion
				() -> {
					//Tag
					Tag tag = new Tag();
					tag.setName("TAG");
					tag.setUserId(1L);

					Tag tag1 = new Tag();
					tag1.setName("TAG1");
					tag1.setUserId(1L);
					var tags = tagRepository.saveAll(List.of(tag, tag1));

					//Task
					Task task = new Task();
					task.setUserId(1L);
					task.setTitle("TITLE");
					task.setDescription("DESCRIPTION");
					task.setStatus(Task.TaskStatus.PENDING);
					task.setDueDate(LocalDateTime.now());

					task.setTags(
							Set.of(
								new TaskTag(null, tags.get(0).getId()),
								new TaskTag(null, tags.get(1).getId())
							)
					);
					taskRepository.save(task);
				},
				() -> {
					userRepository.findAll().forEach(System.out::println);
					tagRepository.findAll().forEach(System.out::println);
					taskRepository.findAll().forEach(System.out::println);
					taskTagRepository.findAll().forEach(System.out::println);
				},
				() -> {
					//Update task
					var task = taskRepository.findById(1L).orElseThrow();
					task.setStatus(Task.TaskStatus.COMPLETED);
					var tags = task.getTags();
					task.setTags(Set.of(tags.stream().findFirst().get()));
					taskRepository.save(task);
				},
				() -> {
					//Find task
					var task = taskRepository.findById(1L).orElseThrow();
					assertNotNull(task);
					assertEquals(task.getStatus(), Task.TaskStatus.COMPLETED);
					assertEquals(task.getTags().size(), 1);
				},
				() -> {
					taskRepository.findAll().forEach(System.out::println);
					taskTagRepository.findAll().forEach(System.out::println);
				}

		);
	}
}
