package com.natwest.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@ContextConfiguration(initializers = UserServiceTest1.Initializer.class)
@SpringBootTest()
public class UserServiceTest1 {

  @ClassRule
  public static GenericContainer redis =
      new GenericContainer(DockerImageName.parse("redis:6.2.4-alpine")).withExposedPorts(6379);

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Test
  public void shouldFetchValuesFromRedisCache() {

    userService.save(new User("Mani", 0, 0, 0, 2, 2, 2));

    User user = userService.getUser("Mani");
    assertThat(user).isNotNull();

    user.setLostCounter(user.getLostCounter() + 1);
    userService.save(user);

    user = userService.getUser("Mani");
    assertEquals(1, user.getLostCounter());
  }

  public static class Initializer implements
      ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      redis.start();
      TestPropertyValues values = TestPropertyValues.of(
          "spring.cache.type=redis",
          "spring.redis.host=" + redis.getContainerIpAddress(),
          "spring.redis.port=" + redis.getMappedPort(6379));
      values.applyTo(configurableApplicationContext);
    }
  }
}