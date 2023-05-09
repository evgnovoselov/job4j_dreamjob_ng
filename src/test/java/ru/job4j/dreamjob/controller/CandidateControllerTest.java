package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CandidateControllerTest {
    private CandidateService candidateService;
    private CityService cityService;
    private CandidateController candidateController;
    private MultipartFile testFile;

    @BeforeEach
    public void initServices() {
        candidateService = mock(CandidateService.class);
        cityService = mock(CityService.class);
        candidateController = new CandidateController(candidateService, cityService);
        testFile = new MockMultipartFile("testFile.jpg", new byte[]{1, 2, 3});
    }

    @Test
    public void whenRequestCandidateListPageThenGetPageWithCandidates() {
        Candidate candidate1 = new Candidate(1, "name 1", "description 1", 1, 2, LocalDateTime.now());
        Candidate candidate2 = new Candidate(2, "name 2", "description 2", 3, 4, LocalDateTime.now());
        List<Candidate> expectedCandidates = List.of(candidate1, candidate2);
        when(candidateService.findAll()).thenReturn(expectedCandidates);

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.getAll(model);
        Collection<Candidate> actualCandidates = (Collection<Candidate>) model.getAttribute("candidates");

        assertThat(view).isEqualTo("candidates/list");
        assertThat(actualCandidates).isEqualTo(expectedCandidates);
    }

    @Test
    public void whenRequestCandidateCreationPageThenGetPageWithCities() {
        City city1 = new City(1, "Moscow");
        City city2 = new City(2, "Санкт-Петербург");
        List<City> expectedCities = List.of(city1, city2);
        when(cityService.findAll()).thenReturn(expectedCities);

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.getCreationPage(model);
        Collection<City> actualCities = (Collection<City>) model.getAttribute("cities");

        assertThat(view).isEqualTo("candidates/create");
        assertThat(actualCities).isEqualTo(expectedCities);
    }

    @Test
    public void whenPostCandidateWithFileThenSameDataAndRedirectToCandidatesPage() throws IOException {
        Candidate candidate = new Candidate(1, "name 1", "description 1", 1, 2, LocalDateTime.now());
        FileDto fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        ArgumentCaptor<Candidate> candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.save(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(candidate);

        String view = candidateController.create(candidate, testFile, new ConcurrentModel());
        Candidate actualCandidate = candidateArgumentCaptor.getValue();
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidate).usingRecursiveComparison().isEqualTo(candidate);
        assertThat(actualFileDto).usingRecursiveComparison().isEqualTo(fileDto);
    }

    @Test
    public void whenPostCandidateSomeExceptionThrownThenGetErrorPageWithMessage() {
        RuntimeException expectedException = new RuntimeException("Filed to save candidate");
        when(candidateService.save(any(), any())).thenThrow(expectedException);

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.create(new Candidate(), testFile, model);
        String actualExceptionMessage = (String) model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenGetCandidateByIdThenGetCandidateAndCities() {
        City city1 = new City(1, "Moscow");
        City city2 = new City(2, "Санкт-Петербург");
        List<City> cities = List.of(city1, city2);
        when(cityService.findAll()).thenReturn(cities);
        Candidate candidate = new Candidate(1, "name 1", "description 1", 1, 2, LocalDateTime.now());
        when(candidateService.findById(anyInt())).thenReturn(Optional.of(candidate));

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.getById(model, 1);
        Candidate actualCandidate = (Candidate) model.getAttribute("candidate");
        Collection<City> actualCities = (Collection<City>) model.getAttribute("cities");

        assertThat(view).isEqualTo("candidates/one");
        assertThat(actualCandidate).isEqualTo(candidate);
        assertThat(actualCities).isEqualTo(cities);
    }

    @Test
    public void whenGetCandidateByIdButThereNotHaveThenGetErrorPageWithMessage() {
        when(candidateService.findById(anyInt())).thenReturn(Optional.empty());

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.getById(model, 1);
        String actualMessage = (String) model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualMessage).isEqualTo("Резюме с указаным идентификатором не найдено.");
    }

    @Test
    public void whenUpdateCandidateWithFileNullThenRedirectCandidates() {
        Candidate candidate = new Candidate(1, "name 1", "description 1", 1, 2, LocalDateTime.now());
        FileDto fileDto = new FileDto("", new byte[0]);
        ArgumentCaptor<Candidate> candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.update(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(true);

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.update(candidate, null, model);
        Candidate actualCandidate = candidateArgumentCaptor.getValue();
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidate).usingRecursiveComparison().isEqualTo(candidate);
        assertThat(actualFileDto).usingRecursiveComparison().isEqualTo(fileDto);
    }

    @Test
    public void whenUpdateCandidateWithFileEmptyThenRedirectCandidates() {
        Candidate candidate = new Candidate(1, "name 1", "description 1", 1, 2, LocalDateTime.now());
        FileDto fileDto = new FileDto("", new byte[0]);
        ArgumentCaptor<Candidate> candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.update(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(true);

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.update(candidate, new MockMultipartFile("test", new byte[0]), model);
        Candidate actualCandidate = candidateArgumentCaptor.getValue();
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidate).usingRecursiveComparison().isEqualTo(candidate);
        assertThat(actualFileDto).usingRecursiveComparison().isEqualTo(fileDto);
    }


    @Test
    public void whenUpdateCandidateWithFileThenRedirectCandidates() throws IOException {
        Candidate candidate = new Candidate(1, "name 1", "description 1", 1, 2, LocalDateTime.now());
        FileDto fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        ArgumentCaptor<Candidate> candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.update(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(true);

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.update(candidate, testFile, model);
        Candidate actualCandidate = candidateArgumentCaptor.getValue();
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidate).usingRecursiveComparison().isEqualTo(candidate);
        assertThat(actualFileDto).usingRecursiveComparison().isEqualTo(fileDto);
    }

    @Test
    public void whenUpdatedCandidateIsNotUpdatedThenGetErrorPageWithMessage() throws IOException {
        Candidate candidate = new Candidate(1, "name 1", "description 1", 1, 2, LocalDateTime.now());
        FileDto fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        ArgumentCaptor<Candidate> candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.update(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture())).thenReturn(false);

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.update(candidate, testFile, model);
        String actualMessage = (String) model.getAttribute("message");
        Candidate actualCandidate = candidateArgumentCaptor.getValue();
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualMessage).isEqualTo("Резюме с указаным идентификатором не найдено.");
        assertThat(actualCandidate).usingRecursiveComparison().isEqualTo(candidate);
        assertThat(actualFileDto).usingRecursiveComparison().isEqualTo(fileDto);
    }

    @Test
    public void whenUpdatedCandidateExceptionThenGetErrorPageWithMessage() {
        RuntimeException exception = new RuntimeException("Error updated candidate");
        when(candidateService.update(any(), any())).thenThrow(exception);

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.update(new Candidate(), testFile, model);
        String actualExceptionMessage = (String) model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(exception.getMessage());
    }

    @Test
    public void whenDeletedCandidateThenRedirectVacancies() {
        when(candidateService.deleteById(anyInt())).thenReturn(true);

        String view = candidateController.delete(new ConcurrentModel(), 1);

        assertThat(view).isEqualTo("redirect:/candidates");
    }

    @Test
    public void whenDeletedCandidateNotHaveIdThenGetErrorPageWithMessage() {
        when(candidateService.deleteById(anyInt())).thenReturn(false);

        ConcurrentModel model = new ConcurrentModel();
        String view = candidateController.delete(model, 1);
        String message = (String) model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(message).isEqualTo("Резюме с указаным идентификатором не найдено.");
    }
}
