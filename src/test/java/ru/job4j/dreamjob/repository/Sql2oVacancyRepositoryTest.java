package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sql2o.Sql2o;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.File;
import ru.job4j.dreamjob.model.Vacancy;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class Sql2oVacancyRepositoryTest {
    private static Sql2oVacancyRepository sql2oVacancyRepository;
    private static Sql2oFileRepository sql2oFileRepository;
    private static File file;

    @BeforeAll
    public static void initRepositories() throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = Sql2oVacancyRepositoryTest.class.getClassLoader().getResourceAsStream("connection.properties")) {
            properties.load(inputStream);
        }
        String url = properties.getProperty("datasource.url");
        String username = properties.getProperty("datasource.username");
        String password = properties.getProperty("datasource.password");
        DatasourceConfiguration configuration = new DatasourceConfiguration();
        DataSource dataSource = configuration.connectionPool(url, username, password);
        Sql2o sql2o = configuration.databaseClient(dataSource);
        sql2oVacancyRepository = new Sql2oVacancyRepository(sql2o);
        sql2oFileRepository = new Sql2oFileRepository(sql2o);
        file = new File("test", "test");
        sql2oFileRepository.save(file);
    }

    @AfterAll
    public static void deleteFile() {
        sql2oFileRepository.deleteById(file.getId());
    }

    @AfterEach
    public void clearVacancies() {
        Collection<Vacancy> vacancies = sql2oVacancyRepository.findAll();
        for (Vacancy vacancy : vacancies) {
            sql2oVacancyRepository.deleteById(vacancy.getId());
        }
    }

    @Test
    public void whenSaveThenGetSame() {
        LocalDateTime creationDate = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Vacancy vacancy = sql2oVacancyRepository.save(new Vacancy(0, "title", "description", 1, file.getId(), true, creationDate));
        Vacancy savedVacancy = sql2oVacancyRepository.findById(vacancy.getId()).orElseThrow();
        assertThat(savedVacancy).usingRecursiveComparison().isEqualTo(vacancy);
    }

    @Test
    public void whenSaveSeveralThenGetAll() {
        LocalDateTime creationDate = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Vacancy vacancy1 = sql2oVacancyRepository.save(new Vacancy(0, "title 1", "description 1", 1, file.getId(), true, creationDate));
        Vacancy vacancy2 = sql2oVacancyRepository.save(new Vacancy(0, "title 2", "description 2", 1, file.getId(), false, creationDate));
        Vacancy vacancy3 = sql2oVacancyRepository.save(new Vacancy(0, "title 3", "description 3", 1, file.getId(), true, creationDate));
        Collection<Vacancy> result = sql2oVacancyRepository.findAll();
        assertThat(result).isEqualTo(List.of(vacancy1, vacancy2, vacancy3));
    }

    @Test
    public void whenDontSaveThenNothingFound() {
        assertThat(sql2oVacancyRepository.findById(0)).isEqualTo(Optional.empty());
        assertThat(sql2oVacancyRepository.findAll()).isEqualTo(Collections.emptyList());
    }

    @Test
    public void whenDeleteThenGetEmptyOptional() {
        LocalDateTime creationDate = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Vacancy vacancy = sql2oVacancyRepository.save(new Vacancy(0, "title", "description", 1, file.getId(), true, creationDate));
        boolean isDeleted = sql2oVacancyRepository.deleteById(vacancy.getId());
        Optional<Vacancy> savedVacancy = sql2oVacancyRepository.findById(vacancy.getId());
        assertThat(isDeleted).isTrue();
        assertThat(savedVacancy).isEqualTo(Optional.empty());
    }

    @Test
    public void whenDeleteByInvalidIdThenGetFalse() {
        assertThat(sql2oVacancyRepository.deleteById(0)).isFalse();
    }

    @Test
    public void whenUpdateThenGetUpdated() {
        LocalDateTime creationDate = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Vacancy vacancy = sql2oVacancyRepository.save(new Vacancy(0, "title", "description", 1, file.getId(), true, creationDate));
        Vacancy updatedVacancy = new Vacancy(vacancy.getId(), "new title", "new description", 1, file.getId(), !vacancy.isVisible(), creationDate.plusDays(1));
        boolean isUpdated = sql2oVacancyRepository.update(updatedVacancy);
        Vacancy savedVacancy = sql2oVacancyRepository.findById(updatedVacancy.getId()).orElseThrow();
        assertThat(isUpdated).isTrue();
        assertThat(savedVacancy).usingRecursiveComparison().isEqualTo(savedVacancy);
    }

    @Test
    public void whenUpdateUnExistingVacancyThenGetFalse() {
        LocalDateTime creationDate = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Vacancy vacancy = new Vacancy(0, "title", "description", 1, file.getId(), true, creationDate);
        boolean isUpdated = sql2oVacancyRepository.update(vacancy);
        assertThat(isUpdated).isFalse();
    }
}
