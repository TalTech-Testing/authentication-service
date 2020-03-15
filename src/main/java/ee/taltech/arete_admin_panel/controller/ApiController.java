package ee.taltech.arete_admin_panel.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("services/arete/api/v1")
public class ApiController {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/docs")
    public String docs() {
        return "Some docs here.";
    }

}
