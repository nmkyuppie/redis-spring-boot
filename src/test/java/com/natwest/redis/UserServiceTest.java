package com.natwest.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

@SpringBootTest()
@RunWith(SpringRunner.class)
class UserServiceTest{

  private static GenericContainer<?> redis;

  @Autowired
  private UserService userService;

//  @DynamicPropertySource
//  public static void configureProperties(DynamicPropertyRegistry registry) {
//    registry.add("spring.data.redis.host", redis::getHost);
//    registry.add("spring.data.redis.port", redis::getFirstMappedPort);
//  }

  @BeforeAll
  public static void beforeAll() {
    redis = new GenericContainer<>(DockerImageName.parse("redis:6.2.4-alpine")).withExposedPorts(6379);
    redis.setPortBindings(List.of("6379:6379"));
    redis.start();
    System.setProperty("spring.redis.host", redis.getHost());
    System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% "+ redis.getFirstMappedPort());
    System.setProperty("spring.redis.port", redis.getFirstMappedPort().toString());
  }

  @AfterAll
  public static void afterAll() {
    redis.stop();
  }

  @Test
  public void testUpdateStoredUser() {
    userService.save(new User("Manikandan", 0, 0, 0, 2, 2, 2));

    User user = userService.getUser("Manikandan");
    assertThat(user).isNotNull();

    user.setLostCounter(user.getLostCounter() + 1);
    userService.save(user);

    user = userService.getUser("Manikandan");
    assertEquals(1, user.getLostCounter());
  }
}
