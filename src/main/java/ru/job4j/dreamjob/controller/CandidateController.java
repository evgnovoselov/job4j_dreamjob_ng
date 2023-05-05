package ru.job4j.dreamjob.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;
import ru.job4j.dreamjob.utility.Utility;

import java.util.Optional;

@Controller
@RequestMapping("/candidates")
public class CandidateController {
    private final CandidateService candidateService;
    private final CityService cityService;

    public CandidateController(CandidateService candidateService, CityService cityService) {
        this.candidateService = candidateService;
        this.cityService = cityService;
    }

    @GetMapping
    public String getAll(Model model, HttpSession session) {
        model.addAttribute("candidates", candidateService.findAll());
        Utility.addUserFromSessionInModel(model, session);
        return "candidates/list";
    }

    @GetMapping("/create")
    public String getCreationPage(Model model, HttpSession session) {
        model.addAttribute("cities", cityService.findAll());
        Utility.addUserFromSessionInModel(model, session);
        return "candidates/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Candidate candidate,
                         @RequestParam MultipartFile file,
                         Model model,
                         HttpSession session) {
        try {
            candidateService.save(candidate, new FileDto(file.getOriginalFilename(), file.getBytes()));
            return "redirect:/candidates";
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
            Utility.addUserFromSessionInModel(model, session);
            return "errors/404";
        }
    }

    @GetMapping("/{id}")
    public String getById(Model model, @PathVariable int id, HttpSession session) {
        Optional<Candidate> candidateOptional = candidateService.findById(id);
        Utility.addUserFromSessionInModel(model, session);
        if (candidateOptional.isEmpty()) {
            model.addAttribute("message", "Резюме с указаным идентификатором не найдено.");
            return "errors/404";
        }
        model.addAttribute("candidate", candidateOptional.get());
        model.addAttribute("cities", cityService.findAll());
        return "candidates/one";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Candidate candidate,
                         @RequestParam(required = false) MultipartFile file,
                         Model model,
                         HttpSession session) {
        Utility.addUserFromSessionInModel(model, session);
        try {
            FileDto fileDto;
            if (file.isEmpty()) {
                fileDto = new FileDto("", new byte[0]);
            } else {
                fileDto = new FileDto(file.getOriginalFilename(), file.getBytes());
            }
            boolean isUpdated = candidateService.update(candidate, fileDto);
            if (!isUpdated) {
                model.addAttribute("message", "Резюме с указаным идентификатором не найдено.");
                return "errors/404";
            }
            return "redirect:/candidates";
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
            return "errors/404";
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(Model model, @PathVariable int id, HttpSession session) {
        boolean isDeleted = candidateService.deleteById(id);
        Utility.addUserFromSessionInModel(model, session);
        if (!isDeleted) {
            model.addAttribute("message", "Резюме с указаным идентификатором не найдено.");
            return "errors/404";
        }
        return "redirect:/candidates";
    }
}
