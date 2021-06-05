package ee.taltech.arete_admin_panel.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping({"services/arete/api/v2", "services/arete/docs/v2"})
public class WebController {

    // services/arete/api/v2/swagger-ui.html is reserved by swagger
    // services/arete/api/v2/docs/swagger-config is also reserved by swagger

}

