package ee.taltech.arete_admin_panel.controller;

import ee.taltech.arete_admin_panel.pojo.abi.users.UserResponseIdToken;
import ee.taltech.arete_admin_panel.service.TokenService;
import ee.taltech.arete_admin_panel.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/admin")
public class BackendController {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final UserService userService;
    private final TokenService tokenService;

    public BackendController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/auth")
    public UserResponseIdToken getHome(@RequestParam() String username) {
        LOG.info(String.format("Getting ID for user %s", username));
        return tokenService.createResponse(userService.getHome(username));
    }
}
