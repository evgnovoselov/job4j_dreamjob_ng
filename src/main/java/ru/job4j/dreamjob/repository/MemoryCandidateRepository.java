package ru.job4j.dreamjob.repository;

import org.springframework.stereotype.Repository;
import ru.job4j.dreamjob.model.Candidate;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class MemoryCandidateRepository implements CandidateRepository {
    private final Map<Integer, Candidate> candidates = new HashMap<>();
    private int nextId = 1;

    private MemoryCandidateRepository() {
        save(new Candidate(0, "Evgeny", "Description Middle Java Developer", LocalDateTime.now()));
        save(new Candidate(0, "Petr", "Description Middle+ Java Developer", LocalDateTime.now()));
        save(new Candidate(0, "Julia", "Description Senior Java Developer", LocalDateTime.now()));
        save(new Candidate(0, "Andrey", "Description Junior Java Developer", LocalDateTime.now()));
        save(new Candidate(0, "Igor", "Description Middle Java Developer", LocalDateTime.now()));
        save(new Candidate(0, "Ivan", "Description Senior Java Developer", LocalDateTime.now()));
        save(new Candidate(0, "Sergey", "Description Middle+ Java Developer", LocalDateTime.now()));
    }

    @Override
    public Candidate save(Candidate candidate) {
        candidate.setId(nextId++);
        candidates.put(candidate.getId(), candidate);
        return candidate;
    }

    @Override
    public boolean deleteById(int id) {
        return candidates.remove(id) != null;
    }

    @Override
    public boolean update(Candidate candidate) {
        return candidates.computeIfPresent(candidate.getId(), (id, oldCandidate) ->
                new Candidate(oldCandidate.getId(), candidate.getName(), candidate.getDescription(), candidate.getCreationDate())) != null;
    }

    @Override
    public Optional<Candidate> findById(int id) {
        return Optional.ofNullable(candidates.get(id));
    }

    @Override
    public Collection<Candidate> findAll() {
        return candidates.values();
    }
}
