package ee.taltech.arete_admin_panel.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.arete.java.LoadBalancerClient;
import ee.taltech.arete.java.request.AreteRequestDTO;
import ee.taltech.arete.java.request.hook.AreteTestUpdateDTO;
import ee.taltech.arete.java.request.hook.CommitDTO;
import ee.taltech.arete.java.response.arete.AreteResponseDTO;
import ee.taltech.arete.java.response.arete.SystemStateDTO;
import ee.taltech.arete_admin_panel.domain.Job;
import ee.taltech.arete_admin_panel.domain.Submission;
import ee.taltech.arete_admin_panel.repository.JobRepository;
import ee.taltech.arete_admin_panel.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class AreteService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final CacheService cacheService;
    private final SubmissionRepository submissionRepository;
    private final JobRepository jobRepository;
    private final LoadBalancerClient areteClient;

    @SneakyThrows
    public void parseAreteResponseDTO(AreteResponseDTO response) {
        logger.info("Saving job into DB for user: {} with hash: {} in: {}", response.getUniid(), response.getHash(), response.getRoot());

        setDefaultValuesIfNull(response);
        Submission submission = saveSubmission(response);
        saveJob(response);
        cacheService.enqueueSubmission(submission);
    }

    private void setDefaultValuesIfNull(AreteResponseDTO response) {
        if (response.getUniid() == null) {
            response.setUniid("NaN");
        }

        if (response.getGitStudentRepo() == null) {
            response.setGitStudentRepo("no repo");
        }

        if (response.getErrors() == null) {
            response.setErrors(new ArrayList<>());
        }

        if (response.getFiles() == null) {
            response.setFiles(new ArrayList<>());
        }

        if (response.getTestFiles() == null) {
            response.setTestFiles(new ArrayList<>());
        }

        if (response.getTestSuites() == null) {
            response.setTestSuites(new ArrayList<>());
        }

        if (response.getOutput() == null) {
            response.setOutput("no output");
        }
        response.setOutput(response.getOutput().replace("\n", "<br>"));

        if (response.getConsoleOutputs() == null) {
            response.setConsoleOutputs("no output");
        }
        response.setConsoleOutputs(response.getConsoleOutputs().replace("\n", "<br>"));

        if (response.getRoot() == null) {
            response.setRoot(response.getSlug());
        }

        if (response.getTimestamp() == null) {
            response.setTimestamp(System.currentTimeMillis());
        }
    }

    public Submission saveSubmission(AreteResponseDTO response) {
        Submission submission = Submission.builder()
                .uniid(response.getUniid())
                .slug(response.getSlug())
                .hash(response.getHash())
                .testingPlatform(response.getTestingPlatform())
                .root(response.getRoot())
                .timestamp(response.getTimestamp())
                .style(response.getStyle())
                .diagnosticErrors(response.getErrors().size())
                .testsPassed(response.getTotalPassedCount())
                .testsRan(response.getTotalCount())
                .gitStudentRepo(response.getGitStudentRepo())
                .gitTestSource(response.getGitTestRepo())
                .failed(response.getFailed())
                .build();

        updateSubmissions(submission, submission.getHash());
        return submission;
    }

    @SneakyThrows
    private void saveJob(AreteResponseDTO response) {
        Job job = mapper.readValue(mapper.writeValueAsString(response), Job.class);
        logger.info("Saving job {}", response.getHash());
        jobRepository.save(job);
    }

    public void updateSubmissions(Submission submission, String hash) {
        logger.info("Updating submission cache: {}", hash);
        submissionRepository.saveAndFlush(submission);
    }

    public AreteResponseDTO makeRequestSync(AreteRequestDTO areteRequest) {
        logger.info("Forwarding a sync submission: {}", areteRequest);
        return areteClient.requestSync(areteRequest);
    }

    public void makeRequestWebhook(AreteTestUpdateDTO update, String testRepository) {
        CommitDTO latest = update.getCommits().get(0);

        Set<String> slugs = new HashSet<>();
        slugs.addAll(latest.getAdded());
        slugs.addAll(latest.getModified());

        AreteRequestDTO request = AreteRequestDTO.builder()
                .eMail(latest.getAuthor().getEmail())
                .uniid(latest.getAuthor().getName())
                .gitStudentRepo(update.getProject().getUrl())
                .gitTestRepo(testRepository)
                .slugs(slugs)
                .build();

        makeRequestAsync(request);
    }

    public void makeRequestAsync(AreteRequestDTO areteRequest) {
        logger.info("Forwarding a async submission: {}", areteRequest);
        areteClient.requestAsync(areteRequest);
    }

    public void updateImage(String image) {
        logger.info("Updating image: {}", image);
        areteClient.updateImage(image);
    }

    public void updateTests(AreteTestUpdateDTO areteTestUpdate) {
        logger.info("Updating tests: {}", areteTestUpdate);
        areteClient.updateTests(areteTestUpdate);
    }

    public SystemStateDTO getTesterState() {
        logger.info("Reading tester state");
        return areteClient.requestState();
    }

}
