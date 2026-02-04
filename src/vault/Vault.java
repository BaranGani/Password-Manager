package vault;

import java.util.*;

public class Vault {

    private Map<String, VaultEntry> entryMap;

    public Vault(){
        this.entryMap = new HashMap<>();
    }

    public boolean addEntry(String service, VaultEntry entry){
        validate(service, entry);
        if(this.entryMap.containsKey(service)){
            return false;
        }
        this.entryMap.put(service, entry);
        return true;
    }

    public Optional<VaultEntry> getEntry(String service){
        if(service == null) return Optional.empty();
        return Optional.ofNullable(entryMap.get(service));
    }
    public boolean removeEntry(String service){
        if (service == null) return false;
        return entryMap.remove(service) != null;
    }

    public boolean containsEntry(String service){
        return this.entryMap.containsKey(service);
    }

    public Set<String> listServices(){
        return Collections.unmodifiableSet(entryMap.keySet());
    }
    public Map<String, VaultEntry> getEntries() {
        return this.entryMap;
    }

    private static void validate(String service, VaultEntry entry) {
        if (service == null || service.isBlank()) {
            throw new IllegalArgumentException("Service must not be null/blank.");
        }
        if (entry == null) {
            throw new IllegalArgumentException("Entry must not be null.");
        }
    }
}
