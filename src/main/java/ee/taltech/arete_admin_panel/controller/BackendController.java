package ee.taltech.arete_admin_panel.controller;

import ee.taltech.arete_admin_panel.algorithms.SHA512;
import ee.taltech.arete_admin_panel.domain.Job;
import ee.taltech.arete_admin_panel.domain.Submission;
import ee.taltech.arete_admin_panel.domain.User;
import ee.taltech.arete_admin_panel.exception.UserNotFoundException;
import ee.taltech.arete_admin_panel.exception.UserWrongCredentials;
import ee.taltech.arete_admin_panel.pojo.abi.users.UserPostDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.UserResponseIdToken;
import ee.taltech.arete_admin_panel.repository.JobRepository;
import ee.taltech.arete_admin_panel.repository.SubmissionRepository;
import ee.taltech.arete_admin_panel.service.TokenService;
import ee.taltech.arete_admin_panel.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController()
@RequestMapping("/admin")
public class BackendController {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final UserService userService;
    private final TokenService tokenService;
    private final SubmissionRepository submissionRepository;
    private final JobRepository jobRepository;

    public BackendController(UserService userService, TokenService tokenService, SubmissionRepository submissionRepository, JobRepository jobRepository) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.submissionRepository = submissionRepository;
        this.jobRepository = jobRepository;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/auth")
    public UserResponseIdToken getHome(@RequestBody UserPostDto userDto) {
        User user = userService.getUser(userDto.getUsername());

        SHA512 sha512 = new SHA512();
        String passwordHash = sha512.get_SHA_512_SecurePassword(userDto.getPassword(), user.getSalt());

        if (!user.getPasswordHash().equals(passwordHash)) {
            throw new UserWrongCredentials("Wrong login.");
        }

        return tokenService.createResponse(userService.getHome(user.getUsername()));
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/submissions")
    public List<Submission> getSubmissions() {

        return submissionRepository.findAll();

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/submission/{hash}")
    public List<Job> getSubmission(@PathVariable("hash") String hash) {

        return jobRepository.findByHash(hash);

    }
}
