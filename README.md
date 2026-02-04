# CLI Password Manager (Java)

This project is a command-line password manager written in pure Java.  
It stores credentials in a single encrypted vault file and requires a master password to unlock.

The goal of the project is to practice secure storage, cryptography, file formats, and clean CLI design — without relying on external libraries or frameworks.

---

## Features

- Encrypted vault file on disk
- Master password–based access
- AES-GCM authenticated encryption
- PBKDF2 key derivation with per-vault salt
- Binary vault file format (no plaintext on disk)
- Interactive CLI shell
- Add, list, view, and delete entries
- Automatic saving after changes

---

## Security Design

The vault is stored in a single file (e.g. `vault.dat`) with the following layout:


- **MAGIC** – identifies the file format
- **SALT** – random bytes for PBKDF2
- **IV** – random AES-GCM nonce
- **CIPHERTEXT** – encrypted vault contents

The master password is **never stored**.  
Access is verified by attempting to decrypt the vault; if authentication fails, the password is rejected.

Cryptography choices:

- Key derivation: `PBKDF2WithHmacSHA256`
- Encryption: `AES/GCM/NoPadding`
- 256-bit AES key
- 128-bit authentication tag

---

## Project Structure

- `Vault` / `VaultEntry` — in-memory model
- `VaultCodec` — binary serialization
- `CryptoService` — key derivation + encryption
- `VaultStore` — encrypted file persistence
- `CommandHandler` — CLI commands
- `Main` — startup and REPL loop

---

## How to Run

Compile:

```bash
javac -d out $(find src -name "*.java")
java -cp out cli.Main
