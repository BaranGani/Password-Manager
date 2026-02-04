package cli;

import vault.Vault;
import vault.VaultEntry;
import vault.VaultStore;

import java.io.Console;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

public class CommandHandler {

    private final Vault vault;
    private final VaultStore store;
    private final char[] masterPassword;
    private final Scanner scanner;

    public CommandHandler(Vault vault, VaultStore store, char[] masterPassword, Scanner scanner) {
        this.vault = vault;
        this.store = store;
        this.masterPassword = masterPassword;
        this.scanner = scanner;
    }

    /**
     * Handles one command line.
     * @return true if the app should exit, false otherwise
     */
    public boolean handle(String line) {
        String[] parts = tokenize(line);
        if (parts.length == 0) return false;

        String cmd = parts[0].toLowerCase();

        try {
            switch (cmd) {
                case "help" -> {
                    printHelp();
                    return false;
                }
                case "list" -> {
                    handleList();
                    return false;
                }
                case "add" -> {
                    if (parts.length < 2) {
                        System.out.println("Usage: add <service-key>");
                        return false;
                    }
                    handleAdd(parts[1]);
                    return false;
                }
                case "get" -> {
                    if (parts.length < 2) {
                        System.out.println("Usage: get <service-key>");
                        return false;
                    }
                    handleGet(parts[1]);
                    return false;
                }
                case "delete" -> {
                    if (parts.length < 2) {
                        System.out.println("Usage: delete <service-key>");
                        return false;
                    }
                    handleDelete(parts[1]);
                    return false;
                }
                case "exit", "quit" -> {
                    System.out.println("Bye.");
                    // Optional: wipe masterPassword from memory on exit
                    Arrays.fill(masterPassword, '\0');
                    return true;
                }
                default -> {
                    System.out.println("Unknown command: " + cmd + " (type 'help')");
                    return false;
                }
            }
        } catch (Exception e) {
            // Keep this user-friendly
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    /* ---------------- commands ---------------- */

    private void handleList() {
        Set<String> services = vault.listServices();
        if (services.isEmpty()) {
            System.out.println("(empty vault)");
            return;
        }
        for (String s : services) {
            System.out.println("- " + s);
        }
    }

    private void handleGet(String serviceKey) {
        Optional<VaultEntry> opt = vault.getEntry(serviceKey);
        if (opt.isEmpty()) {
            System.out.println("No entry found for: " + serviceKey);
            return;
        }

        VaultEntry e = opt.get();
        System.out.println("Service:  " + serviceKey);
        System.out.println("Username: " + e.getUsername());
        System.out.println("Password: " + e.getPassword());
        if (e.getNote() != null && !e.getNote().isBlank()) {
            System.out.println("Note:     " + e.getNote());
        }
        System.out.println("Created:  " + e.getCreatedAt());
        System.out.println("Updated:  " + e.getUpdatedAt());
    }

    private void handleAdd(String serviceKey) throws Exception {
        if (vault.containsEntry(serviceKey)) {
            System.out.println("Entry already exists for: " + serviceKey);
            System.out.println("Tip: choose a unique key like 'gmail-work' or delete the old one.");
            return;
        }

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        char[] passwordChars = readPassword("Password: ");
        String password = new String(passwordChars);
        Arrays.fill(passwordChars, '\0'); // wipe temp password chars

        System.out.print("Note (optional): ");
        String note = scanner.nextLine();

        VaultEntry entry = new VaultEntry(username, password, note);
        boolean ok = vault.addEntry(serviceKey, entry);
        if (!ok) {
            System.out.println("Could not add entry (already exists).");
            return;
        }

        // Persist immediately
        store.saveEncrypted(vault, masterPassword);
        System.out.println("Saved entry: " + serviceKey);
    }

    private void handleDelete(String serviceKey) throws Exception {
        if (!vault.containsEntry(serviceKey)) {
            System.out.println("No entry found for: " + serviceKey);
            return;
        }

        System.out.print("Delete '" + serviceKey + "'? (y/n): ");
        String answer = scanner.nextLine().trim().toLowerCase();
        if (!answer.equals("y") && !answer.equals("yes")) {
            System.out.println("Cancelled.");
            return;
        }

        boolean removed = vault.removeEntry(serviceKey);
        if (!removed) {
            System.out.println("Nothing removed.");
            return;
        }

        store.saveEncrypted(vault, masterPassword);
        System.out.println("Deleted: " + serviceKey);
    }

    private void printHelp() {
        System.out.println("""
                Commands:
                  help                  Show this help
                  list                  List all service keys
                  add <service-key>     Add a new entry (e.g., gmail-work)
                  get <service-key>     Show an entry
                  delete <service-key>  Delete an entry
                  exit                  Exit the program
                """);
    }

    /* ---------------- helpers ---------------- */

    private static String[] tokenize(String line) {
        // Simple split by whitespace; you can upgrade later to support quotes.
        return line.trim().isEmpty() ? new String[0] : line.trim().split("\\s+");
    }

    private char[] readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            return console.readPassword(prompt);
        }
        // Fallback if running in IDE
        System.out.print(prompt);
        return scanner.nextLine().toCharArray();
    }
}