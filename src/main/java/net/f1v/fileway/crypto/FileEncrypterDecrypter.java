package net.f1v.fileway.crypto;

import net.f1v.fileway.crypto.model.HashFile;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.util.HexFormat;


@Component
public class FileEncrypterDecrypter {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_PATH = "aes.key";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    public FileEncrypterDecrypter() throws IOException {
        this.secretKey = new SecretKeySpec(readKey(), ALGORITHM);
        this.secureRandom = new SecureRandom();
    }

    public HashFile encrypt(InputStream inputStream, OutputStream outputStream) throws IOException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        final MessageDigest digest = MessageDigest.getInstance("SHA3-256");
        byte[] iv = getRandomIVWithSize(GCM_IV_LENGTH);

        final GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

        outputStream.write(iv);

        try (CipherOutputStream cipherOut = new CipherOutputStream(outputStream, cipher)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                cipherOut.write(buffer, 0, bytesRead);
                digest.update(buffer, 0, bytesRead);
            }
        }

        byte[] encodedHash = digest.digest();
        return new HashFile(HexFormat.of().formatHex(encodedHash));
    }

    public void decrypt(OutputStream outputStream, FileInputStream fileIn) throws Exception {
        byte[] iv = fileIn.readNBytes(GCM_IV_LENGTH);
        if (iv.length != GCM_IV_LENGTH) {
            throw new IOException("Cannot read IV");
        }

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        try (CipherInputStream cipherIn = new CipherInputStream(fileIn, cipher)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = cipherIn.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    private byte[] getRandomIVWithSize(int size) {
        byte[] nonce = new byte[size];
        secureRandom.nextBytes(nonce);
        return nonce;
    }

    private byte[] readKey() throws IOException {
        return Files.readAllBytes(Path.of(KEY_PATH));
    }
}
