package net.f1v.fileway.file.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.f1v.fileway.auth.UserDetailsImpl;
import net.f1v.fileway.file.entity.File;
import net.f1v.fileway.file.entity.FileLink;
import net.f1v.fileway.file.service.FileService;
import net.f1v.fileway.user.entity.User;
import net.f1v.fileway.user.repository.UserRepository;
import net.f1v.fileway.utils.FileSizeFormatter;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;


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

        log.info("upload file {}", file.getOriginalFilename());
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
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        model.addAttribute("isPremium", "ROLE_PREMIUM".equals(user.getRole().name()));
        model.addAttribute("FileSizeFormatter", FileSizeFormatter.class);

        return "files";
    }

    @GetMapping("/api/files/{fileId}/links")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getFileLinks(
            @PathVariable Long fileId,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        File file = fileService.getFileById(fileId);
        if (!file.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        List<Map<String, Object>> links = file.getLinks().stream().map(link -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", link.getId());
            map.put("createdAt", link.getCreatedAt());
            map.put("expiresAt", link.getExpiresAt());
            map.put("maxUses", link.getMaxUses());
            map.put("useCount", link.getUseCount());
            map.put("isExpired", link.getExpiresAt().isBefore(LocalDateTime.now()));
            map.put("url", "http://localhost:8080/download-stream/" + link.getId());
            return map;
        }).toList();

        return ResponseEntity.ok(links);
    }

    @PostMapping("/api/files/{fileId}/links")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createFileLink(
            @PathVariable Long fileId,
            @RequestParam(defaultValue = "1") int maxUses,
            @RequestParam(defaultValue = "1") int expirationHours,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        File file = fileService.getFileById(fileId);
        if (!file.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        FileLink newLink = fileService.createNewLink(file, maxUses, expirationHours);

        Map<String, Object> response = new HashMap<>();
        response.put("id", newLink.getId());
        response.put("createdAt", newLink.getCreatedAt());
        response.put("expiresAt", newLink.getExpiresAt());
        response.put("maxUses", newLink.getMaxUses());
        response.put("useCount", newLink.getUseCount());
        response.put("url", "http://localhost:8080/download-stream/" + newLink.getId());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/files/{fileId}/links/{linkId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteFileLink(
            @PathVariable Long fileId,
            @PathVariable UUID linkId,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        File file = fileService.getFileById(fileId);
        if (!file.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        fileService.deleteLink(linkId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Link deleted successfully");
        return ResponseEntity.ok(response);
    }
}
