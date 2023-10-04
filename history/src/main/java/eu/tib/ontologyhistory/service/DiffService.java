package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.model.Diff;
import eu.tib.ontologyhistory.repository.DiffRepository;
import eu.tib.ontologyhistory.repository.InvalidDiffRepository;
import eu.tib.ontologyhistory.utils.StringOntologyUtils;
import org.obolibrary.robot.CommandState;
import org.obolibrary.robot.DiffCommand;
import org.springframework.stereotype.Service;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DiffService {

    private final DiffRepository diffRepository;

    private final InvalidDiffRepository invalidDiffRepository;

    public DiffService(DiffRepository diffRepository, InvalidDiffRepository invalidDiffRepository) {
        this.diffRepository = diffRepository;
        this.invalidDiffRepository = invalidDiffRepository;
    }

    public List<Diff> findAll() {
        return diffRepository.findAll();
    }

    public Diff findById(String id) {
        return diffRepository.findById(id).orElse(null);
    }

    public Diff insert(Diff diff) {
        return diffRepository.insert(diff);
    }

    public void deleteById(String id) {
        diffRepository.deleteById(id);
    }

    public Diff makeDiffFromGit(String gitIriLeft, String gitIriRight, String commitSha, String parentCommitSha, Instant parentTime, Instant shaTime, Instant commitDate, String message) throws Exception {
        File outputGit = new File("outputGit.txt");
        DiffCommand diffCommand = new DiffCommand();
        diffCommand.execute(new CommandState(), new String[] {"--left-iri", gitIriLeft,
                "--right-iri", gitIriRight,
                "--output", outputGit.getName(),
                "--format", "html"});

        List<String> lines = Files.readAllLines(outputGit.toPath(), StandardCharsets.UTF_8);

        Map<Boolean, List<String>> addedDeletedMap = StringOntologyUtils.addedDeletedMap(lines);

        List<String> editedLines = StringOntologyUtils.editedLines(addedDeletedMap);

        Diff diff = Diff.builder()
                .children(editedLines)
                .ontologyId("default")
                .timestamp(commitDate)
                .sha(commitSha)
                .parentSha(parentCommitSha)
                .parentOffsetDateTime(parentTime)
                .shaOffsetDateTime(shaTime)
                .value(lines)
                .message(message)
                .build();

        if (diff != null) {
            return diffRepository.insert(diff);
        } else {
            return null;
        }
    }

    public void assignOntologyId(List<Diff> diffIds, String ontologyId) {
        for (Diff d : diffIds) {
            Diff diff = diffRepository.findById(d.getId()).orElse(null);
            if (diff != null) {
                diff.setOntologyId(ontologyId);
                diffRepository.save(diff);
            }
        }
    }

}
