package kryz.samlspringangularintegrationtestapp.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public Map<String, Object> home(@AuthenticationPrincipal Saml2AuthenticatedPrincipal principal) {
        var res = new HashMap<String, Object>();
        res.put("name", principal.getName());
        res.put("emailAddress", principal.getFirstAttribute("email"));
        res.put("userAttributes", principal.getAttributes());

        return res;
    }

    @GetMapping("/authenticated")
    public Boolean authenticated(@AuthenticationPrincipal Saml2AuthenticatedPrincipal principal) {
        return principal != null;
    }

}
