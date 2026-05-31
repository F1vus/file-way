package net.f1v.fileway.file.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.f1v.fileway.auth.UserDetailsImpl;
import net.f1v.fileway.file.service.FileService;
import net.f1v.fileway.user.entity.User;
import net.f1v.fileway.user.repository.UserRepository;
import net.f1v.fileway.utils.FileSizeFormatter;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;


@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    private final FileService fileService;
    private final KieContainer kieContainer;
    private final UserRepository userRepository;

    @GetMapping
    public String index(@AuthenticationPrincipal UserDetailsImpl principal, Model model) throws Exception {
        System.out.println(principal);
        if (principal != null) {
            User user = userRepository.findById(principal.getId())
                    .orElseThrow(() -> new Exception("User not found"));
            model.addAttribute("user", user);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("isPremium", "ROLE_PREMIUM".equals(user.getRole().name()));
        } else {
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("isPremium", false);
        }
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @AuthenticationPrincipal UserDetailsImpl principal,
                             RedirectAttributes redirectAttributes) {


        KieSession kieSession = kieContainer.newKieSession();
        kieSession.setGlobal("redirectAttributes", redirectAttributes);
        kieSession.setGlobal("fileService", fileService);
        kieSession.setGlobal("logger", log);

        if (principal != null) kieSession.insert(principal);
        kieSession.insert(file);

        kieSession.fireAllRules();
        kieSession.dispose();

        return "redirect:/";
    }

    @GetMapping("/download-stream/{file_id}")
    public void downloadFileStream(@PathVariable("file_id") String fileLinkId, HttpServletResponse response)  {
        response.setContentType("application/octet-stream");

        try {
            UUID uuid = UUID.fromString(fileLinkId);
            fileService.streamFile(response, uuid);
        } catch (IllegalArgumentException e){
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

    }

    @GetMapping("/my-files")
    public String myFiles(@AuthenticationPrincipal UserDetailsImpl principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("isPremium", "ROLE_PREMIUM".equals(user.getRole().name()));
        model.addAttribute("FileSizeFormatter", FileSizeFormatter.class);
        return "files";
    }
}
