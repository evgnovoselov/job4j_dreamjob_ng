package ru.job4j.dreamjob.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

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

        Assertions.assertThat(view).isEqualTo("candidates/list");
        Assertions.assertThat(actualCandidates).isEqualTo(expectedCandidates);
    }
}
