package net.f1v.fileway.file.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.f1v.fileway.crypto.FileEncrypterDecrypter;
import net.f1v.fileway.crypto.model.HashFile;
import net.f1v.fileway.file.entity.File;
import net.f1v.fileway.file.entity.FileLink;
import net.f1v.fileway.file.error.FileUploadException;
import net.f1v.fileway.file.repository.FileLinkRepository;
import net.f1v.fileway.file.repository.FileRepository;
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
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class FileService {
    private static final String DATA_PATH = "data/files/";

    private final FileEncrypterDecrypter fileEncrypterDecrypter;
    private final FileRepository fileRepository;
    private final FileLinkRepository fileLinkRepository;

    @Transactional
    public String saveFile(MultipartFile file) {
        UUID uuid = UUID.randomUUID();
        Path filePath = Paths.get(DATA_PATH + uuid + ".enc");
        HashFile hashFile;


        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW)) {

             hashFile = fileEncrypterDecrypter.encrypt(inputStream, outputStream);

        } catch (IOException | InvalidKeyException | NoSuchPaddingException |
                 NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new FileUploadException("Error saving file");
        }

        FileLink fileLink = new FileLink(
                uuid,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1)
        );
        fileLinkRepository.save(fileLink);

        File fileEntity = new File(
                file.getSize(),
                uuid + ".enc",
                hashFile.hashFileHex(),
                LocalDateTime.now(),
                file.getOriginalFilename(),
                fileLink
        );
        fileRepository.save(fileEntity);

        return "http://localhost:8080/download-stream/" + fileLink.getId();
    }


    public void streamFile(HttpServletResponse response, UUID fileLinkId) {
        FileLink fileLink = fileLinkRepository.findById(fileLinkId).orElseThrow(() -> new RuntimeException(fileLinkId + " not found"));

        Path filePath = Paths.get(DATA_PATH + fileLink.getFile().getStorageName());


        response.setHeader("Content-Disposition", "attachment; filename="+fileLink.getFile().getOriginalName());

        HashFile hashFile = new HashFile(fileLink.getFile().getFileHash());
        try (FileInputStream inputStream = new FileInputStream(filePath.toFile())) {
            fileEncrypterDecrypter.decrypt(response.getOutputStream(), inputStream, hashFile);
        } catch (Exception e) {
            throw new RuntimeException("Error streaming file", e);
        }

        fileLink.setUseCount(fileLink.getUseCount() + 1);
        fileLinkRepository.save(fileLink);
    }
}
