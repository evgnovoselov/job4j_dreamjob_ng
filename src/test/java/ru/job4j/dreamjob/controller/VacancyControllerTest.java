package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.model.Vacancy;
import ru.job4j.dreamjob.service.CityService;
import ru.job4j.dreamjob.service.VacancyService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VacancyControllerTest {
    private VacancyService vacancyService;
    private CityService cityService;
    private VacancyController vacancyController;
    private MultipartFile testFile;

    @BeforeEach
    public void initServices() {
        vacancyService = mock(VacancyService.class);
        cityService = mock(CityService.class);
        vacancyController = new VacancyController(vacancyService, cityService);
        testFile = new MockMultipartFile("testFile.jpg", new byte[]{1, 2, 3});
    }

    @Test
    public void whenRequestVacancyListPageThenGetPageWithVacancies() {
        Vacancy vacancy1 = new Vacancy(1, "title 1", "description 1", 1, 2, true, LocalDateTime.now());
        Vacancy vacancy2 = new Vacancy(2, "title 2", "description 2", 3, 4, true, LocalDateTime.now());
        List<Vacancy> expectedVacancies = List.of(vacancy1, vacancy2);
        when(vacancyService.findAll()).thenReturn(expectedVacancies);

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.getAll(model);
        Collection<Vacancy> actualVacancies = (Collection<Vacancy>) model.getAttribute("vacancies");

        assertThat(view).isEqualTo("vacancies/list");
        assertThat(actualVacancies).isEqualTo(expectedVacancies);
    }

    @Test
    public void whenRequestVacancyCreationPageThenGetPageWithCities() {
        City city1 = new City(1, "Moscow");
        City city2 = new City(2, "Санкт-Петербург");
        List<City> expectedCities = List.of(city1, city2);
        when(cityService.findAll()).thenReturn(expectedCities);

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.getCreationPage(model);
        List<City> actualCities = (List<City>) model.getAttribute("cities");

        assertThat(view).isEqualTo("vacancies/create");
        assertThat(actualCities).isEqualTo(expectedCities);
    }

    @Test
    public void whenPostVacancyWithFileThenSameDataAndRedirectToVacanciesPage() throws IOException {
        Vacancy vacancy = new Vacancy(1, "title 1", "description 1", 1, 2, true, LocalDateTime.now());
        FileDto fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        ArgumentCaptor<Vacancy> vacancyArgumentCaptor = ArgumentCaptor.forClass(Vacancy.class);
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(vacancyService.save(vacancyArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(vacancy);

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.create(vacancy, testFile, model);
        Vacancy actualVacancy = vacancyArgumentCaptor.getValue();
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualVacancy).isEqualTo(vacancy);
        assertThat(actualFileDto).usingRecursiveComparison().isEqualTo(fileDto);
    }

    @Test
    public void whenSomeExceptionThrownThenGetErrorPageWithMessage() {
        RuntimeException expectedException = new RuntimeException("Failed to write file");
        when(vacancyService.save(any(), any())).thenThrow(expectedException);

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.create(new Vacancy(), testFile, model);
        String actualExceptionMessage = (String) model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenGetVacancyByIdThenGetVacancyAndCities() {
        City city1 = new City(1, "Moscow");
        City city2 = new City(2, "Санкт-Петербург");
        List<City> cities = List.of(city1, city2);
        when(cityService.findAll()).thenReturn(cities);
        Vacancy vacancy = new Vacancy(1, "title 1", "description 1", 1, 2, true, LocalDateTime.now());
        when(vacancyService.findById(any(Integer.class))).thenReturn(Optional.of(vacancy));

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.getById(model, 1);
        Vacancy actualVacancy = (Vacancy) model.getAttribute("vacancy");
        List<City> actualCities = (List<City>) model.getAttribute("cities");

        assertThat(view).isEqualTo("vacancies/one");
        assertThat(actualVacancy).isEqualTo(vacancy);
        assertThat(actualCities).isEqualTo(cities);
    }

    @Test
    public void whenGetVacancyByIdButThereNotHaveThenGetErrorPageWithMessage() {
        when(vacancyService.findById(any(Integer.class))).thenReturn(Optional.empty());

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.getById(model, 1);
        String message = (String) model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(message).isEqualTo("Вакансия с указаным идентификатором не найдена.");
    }

    @Test
    public void whenUpdateVacancyWithFileNullThenRedirectVacancies() {
        Vacancy vacancy = new Vacancy(1, "title 1", "description 1", 1, 2, true, LocalDateTime.now());
        FileDto fileDto = new FileDto("", new byte[0]);
        ArgumentCaptor<Vacancy> vacancyArgumentCaptor = ArgumentCaptor.forClass(Vacancy.class);
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(vacancyService.update(vacancyArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(true);

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.update(vacancy, null, model);
        Vacancy actualVacancy = vacancyArgumentCaptor.getValue();
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualVacancy).usingRecursiveComparison().isEqualTo(vacancy);
        assertThat(actualFileDto).usingRecursiveComparison().isEqualTo(fileDto);
    }

    @Test
    public void whenUpdateVacancyWithFileEmptyThenRedirectVacancies() {
        Vacancy vacancy = new Vacancy(1, "title 1", "description 1", 1, 2, true, LocalDateTime.now());
        FileDto fileDto = new FileDto("", new byte[0]);
        ArgumentCaptor<Vacancy> vacancyArgumentCaptor = ArgumentCaptor.forClass(Vacancy.class);
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(vacancyService.update(vacancyArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(true);

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.update(vacancy, new MockMultipartFile("test", new byte[0]), model);
        Vacancy actualVacancy = vacancyArgumentCaptor.getValue();
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualVacancy).usingRecursiveComparison().isEqualTo(vacancy);
        assertThat(actualFileDto).usingRecursiveComparison().isEqualTo(fileDto);
    }

    @Test
    public void whenUpdateVacancyWithFileThenRedirectVacancies() throws IOException {
        ConcurrentModel model = new ConcurrentModel();
        FileDto fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(vacancyService.update(any(), fileDtoArgumentCaptor.capture())).thenReturn(true);

        String view = vacancyController.update(new Vacancy(), testFile, model);
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualFileDto).usingRecursiveComparison().isEqualTo(fileDto);
    }

    @Test
    public void whenUpdatedVacancyIsNotUpdatedThenGetErrorPageWithMessage() {
        when(vacancyService.update(any(), any())).thenReturn(false);

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.update(new Vacancy(), testFile, model);
        String message = (String) model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(message).isEqualTo("Вакансия с указаным идентификатором не найдена.");
    }

    @Test
    public void whenUpdatedVacancyExceptionThenGetErrorPageWithMessage() {
        RuntimeException exception = new RuntimeException("Error updated vacancy");
        when(vacancyService.update(any(), any())).thenThrow(exception);

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.update(new Vacancy(), testFile, model);
        String actualExceptionMessage = (String) model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(exception.getMessage());
    }

    @Test
    public void whenDeletedVacancyThenRedirectVacancies() {
        when(vacancyService.deleteById(any(Integer.class))).thenReturn(true);

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.delete(model, 1);

        assertThat(view).isEqualTo("redirect:/vacancies");
    }

    @Test
    public void whenDeletedVacancyNotHaveIdThenGetErrorPageWithMessage() {
        when(vacancyService.deleteById(any(Integer.class))).thenReturn(false);

        ConcurrentModel model = new ConcurrentModel();
        String view = vacancyController.delete(model, 1);
        String message = (String) model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(message).isEqualTo("Вакансия с указаным идентификатором не найдена.");
    }
}
