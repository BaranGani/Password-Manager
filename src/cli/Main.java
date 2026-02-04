package cli;

import java.io.Console;
import java.nio.file.Path;
import java.util.Scanner;

import vault.*;
import crypto.*;

public class Main {

    public static void main (String[]args) throws Exception {

        Path vaultPath = Path.of("vault.dat");
        VaultStore store = new VaultStore(vaultPath);

        Scanner scanner = new Scanner(System.in);

        // --- read master password ---
        char[] masterPassword;
        Console console = System.console();
        if (console != null) {
            masterPassword = console.readPassword("Master password: ");
        } else {
            System.out.print("Master password: ");
            masterPassword = scanner.nextLine().toCharArray();
        }

        Vault vault = store.loadEncrypted(masterPassword);

        System.out.println("Vault unlocked. Type 'help'.");

        CommandHandler handler = new CommandHandler(vault, store, masterPassword, scanner);

        // --- main loop ---
        while (true) {
            System.out.print("vault> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) continue;
            boolean shouldExit = handler.handle(line);
            if (shouldExit) break;
        }

        scanner.close();

    }
}

