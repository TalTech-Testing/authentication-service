package ee.taltech.arete_admin_panel.controller;

import arete.java.response.AreteResponse;
import ee.taltech.arete_admin_panel.algorithms.SHA512;
import ee.taltech.arete_admin_panel.configuration.jwt.JwtTokenProvider;
import ee.taltech.arete_admin_panel.domain.User;
import ee.taltech.arete_admin_panel.exception.UserWrongCredentials;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.UserPostDto;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.UserResponseIdToken;
import ee.taltech.arete_admin_panel.service.AreteService;
import ee.taltech.arete_admin_panel.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("services/arete/api/v1")
public class ApiController {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final UserService userService;
    private final AuthenticationManager authenticationManager; // dont delete <- this bean is used here for authentication
    private final JwtTokenProvider jwtTokenProvider;
    private final AreteService areteService;

    public ApiController(UserService userService, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, AreteService areteService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.areteService = areteService;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/docs")
    public String docs() {
        return "Some docs here.";
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/auth")
    public UserResponseIdToken getHome(@RequestBody UserPostDto userDto) throws AuthenticationException {
        try {
            LOG.info("Authenticating user {}", userDto.getUsername());
            User user = userService.getUser(userDto.getUsername());

            SHA512 sha512 = new SHA512();
            String passwordHash = sha512.get_SHA_512_SecurePassword(userDto.getPassword(), user.getSalt());

            if (!user.getPasswordHash().equals(passwordHash)) {
                throw new UserWrongCredentials("Wrong login.");
            }

            return UserResponseIdToken.builder()
                    .username(user.getUsername())
                    .color(user.getColor())
                    .id(user.getId())
                    .roles(user.getRoles())
                    .token(jwtTokenProvider.createToken(user.getUsername(), user.getRoles().stream().map(Enum::toString).collect(Collectors.toList())))
                    .build();

        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/job")
    public void parseJob(@RequestBody AreteResponse areteResponse) throws AuthenticationException {
        try {
            if (!areteResponse.getReturnExtra().get("shared_secret").asText().equals(System.getenv().getOrDefault("SHARED_SECRET", "Please make sure that shared_secret is set up properly"))) {
                throw new AuthenticationException("Authentication failed for submission ran for " + areteResponse.getUniid() + " with hash " + areteResponse.getHash());
            }

            LOG.info("Saving job {} into DB", areteResponse.getHash());
            areteService.enqueueAreteResponse(areteResponse);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AuthenticationException("Not authorized.");
        }
    }

}
