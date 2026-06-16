package net.f1v.fileway.file.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.f1v.fileway.auth.UserDetailsImpl;
import net.f1v.fileway.crypto.FileEncrypterDecrypter;
import net.f1v.fileway.crypto.model.HashFile;
import net.f1v.fileway.file.entity.File;
import net.f1v.fileway.file.entity.FileLink;
import net.f1v.fileway.file.error.FileUploadException;
import net.f1v.fileway.file.repository.FileLinkRepository;
import net.f1v.fileway.file.repository.FileRepository;
import net.f1v.fileway.user.entity.User;
import net.f1v.fileway.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    private static final String DATA_PATH = "data/files/";

    private final FileEncrypterDecrypter fileEncrypterDecrypter;
    private final FileRepository fileRepository;
    private final FileLinkRepository fileLinkRepository;
    private final UserRepository userRepository;

    @Transactional
    public String saveFile(MultipartFile file, UserDetailsImpl userDetails) {
        UUID uuid = UUID.randomUUID();
        Path filePath = Paths.get(DATA_PATH + uuid + ".enc");
        HashFile hashFile;

        User user = null;
        if(userDetails != null) {
            user = userRepository.findById(userDetails.getId()).orElse(null);
        }

        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW)) {

             hashFile = fileEncrypterDecrypter.encrypt(inputStream, outputStream);

        } catch (IOException | InvalidKeyException | NoSuchPaddingException |
                 NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new FileUploadException("Error saving file");
        }



        File fileEntity = new File(
                file.getSize(),
                uuid + ".enc",
                hashFile.hashFileHex(),
                LocalDateTime.now(),
                file.getOriginalFilename(),
                user
        );

        fileRepository.save(fileEntity);

        FileLink fileLink = new FileLink(
                uuid,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                user,
                fileEntity
        );

        fileLinkRepository.save(fileLink);



        return "http://localhost:8080/download-stream/" + fileLink.getId();
    }


    public void streamFile(HttpServletResponse response, UUID fileLinkId) {
        Optional<FileLink> fileLinkOptional = fileLinkRepository.findById(fileLinkId);
        if (fileLinkOptional.isEmpty()) {
            log.error("FileLink record does not exist in DATABASE, by UUID: {}", fileLinkId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        FileLink fileLink = fileLinkOptional.get();

        Path filePath = Paths.get(DATA_PATH + fileLink.getFile().getStorageName());

        if (!Files.exists(filePath)) {
            log.error("File does not exist, by UUID: {}", fileLinkId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setHeader("Content-Disposition", "attachment; filename="+fileLink.getFile().getOriginalName());

        HashFile hashFile = new HashFile(fileLink.getFile().getFileHash());
        try (FileInputStream inputStream = new FileInputStream(filePath.toFile())) {
            fileEncrypterDecrypter.decrypt(response.getOutputStream(), inputStream, hashFile);
        } catch (Exception  e) {
            log.error("Error streaming file, by UUID: {}, error message: {}", fileLinkId, e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        fileLink.setUseCount(fileLink.getUseCount() + 1);
        fileLinkRepository.save(fileLink);
    }

    public File getFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));
    }

    @Transactional
    public FileLink createNewLink(File file, int maxUses, int expirationHours) {
        UUID linkId = UUID.randomUUID();
        FileLink fileLink = new FileLink(
                linkId,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(expirationHours),
                file.getUser(),
                file
        );
        fileLink.setMaxUses(maxUses);
        fileLink.setFile(file);
        return fileLinkRepository.save(fileLink);
    }

    @Transactional
    public void deleteLink(UUID linkId) {
        fileLinkRepository.deleteById(linkId);
    }
}
