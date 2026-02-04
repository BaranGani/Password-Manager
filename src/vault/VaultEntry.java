package vault;

import java.time.Instant;

public class VaultEntry {
    private String username;
    private String password;
    private String note;
    private Instant createdAt;
    private Instant updatedAt;

    public VaultEntry(){}
    public VaultEntry(String username, String password, String note){
        this.username = username;
        this.password = password;
        this.note = note;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    public VaultEntry(String username, String password, String note, Instant createdAt, Instant updatedAt) {
        this.username = username;
        this.password = password;
        this.note = note;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void touch(){
        this.updatedAt = Instant.now();
    }
    public Instant getCreatedAt(){
        return this.createdAt;
    }
    public Instant getUpdatedAt(){
        return this.updatedAt;
    }
    public String getUsername(){
        return this.username;
    }
    public String getPassword(){
        return this.password;
    }
    public String getNote(){
        return this.note;
    }
}
