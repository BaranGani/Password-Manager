package vault;


import java.io.IOException;
import crypto.CryptoService;
import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class VaultStore {

    private static final byte[] MAGIC = "JVLT1".getBytes(); // file identifier
    private final Path filePath;

    public VaultStore(Path filePath) {
        this.filePath = filePath;
    }

    /** Creates a new vault file (overwrites if exists). */
    public void initNewVault(Vault vault, char[] masterPassword) throws Exception {
        saveEncrypted(vault, masterPassword);
    }

    /** Saves the vault encrypted to disk. */
    public void saveEncrypted(Vault vault, char[] masterPassword) throws Exception {
        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }

        byte[] salt = CryptoService.randomBytes(16);
        byte[] iv = CryptoService.randomBytes(12);

        SecretKey key = CryptoService.deriveKey(masterPassword, salt);

        byte[] payload = VaultCodec.encode(vault);          // plaintext bytes in memory
        byte[] ciphertext = CryptoService.encrypt(key, iv, payload);

        // optional: wipe plaintext payload from memory ASAP
        Arrays.fill(payload, (byte) 0);

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(filePath)))) {
            out.write(MAGIC);

            out.writeInt(salt.length);
            out.write(salt);

            out.writeInt(iv.length);
            out.write(iv);

            out.writeInt(ciphertext.length);
            out.write(ciphertext);
        }
    }

    /** Loads and decrypts vault from disk. Returns empty vault if file doesn't exist. */
    public Vault loadEncrypted(char[] masterPassword) throws Exception {
        if (!Files.exists(filePath)) {
            return new Vault();
        }

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(filePath)))) {

            byte[] magic = in.readNBytes(MAGIC.length);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new IOException("Not a valid vault file (magic mismatch).");
            }

            int saltLen = in.readInt();
            byte[] salt = in.readNBytes(saltLen);

            int ivLen = in.readInt();
            byte[] iv = in.readNBytes(ivLen);

            int ctLen = in.readInt();
            byte[] ciphertext = in.readNBytes(ctLen);

            SecretKey key = CryptoService.deriveKey(masterPassword, salt);

            byte[] payload = CryptoService.decrypt(key, iv, ciphertext); // throws on wrong password/tamper
            try {
                return VaultCodec.decode(payload);
            } finally {
                Arrays.fill(payload, (byte) 0);
            }
        }
    }
}