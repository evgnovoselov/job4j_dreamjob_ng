package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sql2o.Sql2o;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.User;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class Sql2oUserRepositoryTest {
    private static Sql2oUserRepository sql2oUserRepository;

    @BeforeAll
    public static void initRepositories() throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = Sql2oUserRepositoryTest.class.getClassLoader().getResourceAsStream("connection.properties")) {
            properties.load(inputStream);
        }
        String url = properties.getProperty("datasource.url");
        String username = properties.getProperty("datasource.username");
        String password = properties.getProperty("datasource.password");
        DatasourceConfiguration configuration = new DatasourceConfiguration();
        DataSource dataSource = configuration.connectionPool(url, username, password);
        Sql2o sql2o = configuration.databaseClient(dataSource);
        sql2oUserRepository = new Sql2oUserRepository(sql2o);
    }

    @AfterEach
    public void clearUsers() {
        Collection<User> users = sql2oUserRepository.findAll();
        for (User user : users) {
            sql2oUserRepository.deleteById(user.getId());
        }
    }

    @Test
    public void whenSaveThenGetSame() {
        String email = "name@example.com";
        String password = "password";
        User user = sql2oUserRepository.save(new User(0, email, "name", password)).orElseThrow();
        User savedUser = sql2oUserRepository.findByEmailAndPassword(email, password).orElseThrow();
        assertThat(savedUser).usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    public void whenSaveSeveralWithSameEmailThenSaveOnlyFirst() {
        String email = "name@example.com";
        String name = "name";
        String password = "password";
        User user1 = new User(0, email, name, password);
        User user2 = new User(0, email, name, password);
        User user3 = new User(0, email, name, password);
        Optional<User> savedOptionalUser1 = sql2oUserRepository.save(user1);
        Optional<User> savedOptionalUser2 = sql2oUserRepository.save(user2);
        Optional<User> savedOptionalUser3 = sql2oUserRepository.save(user3);
        assertThat(savedOptionalUser1).isEqualTo(Optional.of(user1));
        assertThat(savedOptionalUser2).isEqualTo(Optional.empty());
        assertThat(savedOptionalUser3).isEqualTo(Optional.empty());
    }

    @Test
    public void whenSaveSeveralThenGetAll() {
        String name = "name";
        String password = "password";
        User user1 = sql2oUserRepository.save(new User(0, "name1@example.com", name, password)).orElseThrow();
        User user2 = sql2oUserRepository.save(new User(0, "name2@example.com", name, password)).orElseThrow();
        User user3 = sql2oUserRepository.save(new User(0, "name3@example.com", name, password)).orElseThrow();
        Collection<User> users = sql2oUserRepository.findAll();
        assertThat(users).isEqualTo(List.of(user1, user2, user3));
    }

    @Test
    public void whenSaveSeveralWithSameEmailThenGetOnlyFirst() {
        String email = "name@example.com";
        String name = "name";
        String password = "password";
        User user1 = new User(0, email, name, password);
        User user2 = new User(0, email, name, password);
        User user3 = new User(0, email, name, password);
        sql2oUserRepository.save(user1);
        sql2oUserRepository.save(user2);
        sql2oUserRepository.save(user3);
        Collection<User> users = sql2oUserRepository.findAll();
        assertThat(users).isEqualTo(List.of(user1));
    }

    @Test
    public void whenDontSaveThenNothingFound() {
        assertThat(sql2oUserRepository.findByEmailAndPassword("name@example.com", "password")).isEqualTo(Optional.empty());
        assertThat(sql2oUserRepository.findAll()).isEqualTo(Collections.emptyList());
    }

    @Test
    public void whenDeleteThenGetEmptyOptional() {
        String email = "name@example.com";
        String password = "password";
        User user = sql2oUserRepository.save(new User(0, email, "name", password)).orElseThrow();
        boolean isDeleted = sql2oUserRepository.deleteById(user.getId());
        Optional<User> savedUser = sql2oUserRepository.findByEmailAndPassword(email, password);
        assertThat(isDeleted).isTrue();
        assertThat(savedUser).isEqualTo(Optional.empty());
    }

    @Test
    public void whenDeleteByInvalidIdThenGetFalse() {
        assertThat(sql2oUserRepository.deleteById(0)).isFalse();
    }
}
