package cotede.interns.project.guessify.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
//
    @GetMapping("/")
    public String signInPage() {
        return "sign-in";
    }

    @GetMapping("/register.html")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }
}
