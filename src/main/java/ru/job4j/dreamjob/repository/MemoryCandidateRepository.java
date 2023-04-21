package ru.job4j.dreamjob.repository;

import org.springframework.stereotype.Repository;
import ru.job4j.dreamjob.model.Candidate;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class MemoryCandidateRepository implements CandidateRepository {
    private final ConcurrentMap<Integer, Candidate> candidates = new ConcurrentHashMap<>();
    private final AtomicInteger id = new AtomicInteger();

    public MemoryCandidateRepository() {
        save(new Candidate(0, "Evgeny", "Description Middle Java Developer", 1, 0, LocalDateTime.now()));
        save(new Candidate(0, "Petr", "Description Middle+ Java Developer", 2, 0, LocalDateTime.now()));
        save(new Candidate(0, "Julia", "Description Senior Java Developer", 3, 0, LocalDateTime.now()));
        save(new Candidate(0, "Andrey", "Description Junior Java Developer", 1, 0, LocalDateTime.now()));
        save(new Candidate(0, "Igor", "Description Middle Java Developer", 2, 0, LocalDateTime.now()));
        save(new Candidate(0, "Ivan", "Description Senior Java Developer", 3, 0, LocalDateTime.now()));
        save(new Candidate(0, "Sergey", "Description Middle+ Java Developer", 1, 0, LocalDateTime.now()));
    }

    @Override
    public Candidate save(Candidate candidate) {
        candidate.setId(id.incrementAndGet());
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
                new Candidate(oldCandidate.getId(), candidate.getName(), candidate.getDescription(), candidate.getCityId(),
                        candidate.getFileId(), candidate.getCreationDate())) != null;
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
