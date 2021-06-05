package ee.taltech.arete_admin_panel.controller;

import ee.taltech.arete.java.response.arete.SystemStateDTO;
import ee.taltech.arete_admin_panel.service.AreteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@SecurityScheme(name = "Authorization", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER)
@Tag(name = "state", description = "server status")
@RestController()
@RequestMapping("services/arete/api/v2/state")
public class StateController {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final AreteService areteService;
    private final AuthenticationManager authenticationManager; // dont delete <- this bean is used here for authentication


    public StateController(AreteService areteService, AuthenticationManager authenticationManager) {
        this.areteService = areteService;
        this.authenticationManager = authenticationManager;
    }

    @Operation(security = {@SecurityRequirement(name = "Authorization")}, summary = "Return backends' state", tags = {"state"})
    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("")
    public SystemStateDTO getState() {
        LOG.info("Reading state");
        return new SystemStateDTO();
    }


    @Operation(security = {@SecurityRequirement(name = "Authorization")}, summary = "Return testers' state", tags = {"state"})
    @ResponseStatus(HttpStatus.ACCEPTED)
    @GetMapping("/tester")
    public SystemStateDTO getTesterState() {
        return areteService.getTesterState();
    }

}
