package vault;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

public class VaultCodec {

    public static byte[] encode(Vault vault) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);

        Map<String, VaultEntry> entries = vault.getEntries();
        out.writeInt(entries.size());

        for (var e : entries.entrySet()) {
            String service = e.getKey();
            VaultEntry ve = e.getValue();

            writeString(out, service);
            writeString(out, ve.getUsername());
            writeString(out, ve.getPassword());
            writeString(out, ve.getNote() == null ? "" : ve.getNote());

            out.writeLong(ve.getCreatedAt().toEpochMilli());
            out.writeLong(ve.getUpdatedAt().toEpochMilli());
        }

        out.flush();
        return bos.toByteArray();
    }

    public static Vault decode(byte[] payload) throws IOException {
        Vault vault = new Vault();
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));

        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            String service = readString(in);
            String username = readString(in);
            String password = readString(in);
            String note = readString(in);

            Instant createdAt = Instant.ofEpochMilli(in.readLong());
            Instant updatedAt = Instant.ofEpochMilli(in.readLong());

            VaultEntry entry = new VaultEntry(username, password, note, createdAt, updatedAt);
            vault.addEntry(service, entry);
        }

        return vault;
    }

    private static void writeString(DataOutputStream out, String s) throws IOException {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        out.writeInt(b.length);
        out.write(b);
    }

    private static String readString(DataInputStream in) throws IOException {
        int len = in.readInt();
        if (len < 0 || len > 10_000_000) {
            throw new IOException("Invalid string length: " + len);
        }
        byte[] b = in.readNBytes(len);
        return new String(b, StandardCharsets.UTF_8);
    }
}