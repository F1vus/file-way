package net.f1v.fileway.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        try {
            authService.authenticate(email, password, request, response);
            return "redirect:/";
        } catch (BadCredentialsException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password");
            return "redirect:/login?error=true";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/registration";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email, @RequestParam String password, @RequestParam String confirmPassword, Model model) {
        try {
            authService.register(email, password, confirmPassword);
            model.addAttribute("success", "Registration successful! Please log in.");
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/registration";
        }
    }
}
