import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;

public class Slave implements Runnable {
    private final BlockingQueue<String> passwordQueue;

    public Slave(BlockingQueue<String> passwordQueue) {
        this.passwordQueue = passwordQueue;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/password.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String username = parts[0];
                    String hashedPassword = parts[1];
                    passwordQueue.put(username);
                    passwordQueue.put(hashedPassword);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                String hashedPassword = passwordQueue.take();
                // Perform brute force operation here
                bruteForce(hashedPassword);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void bruteForce(String targetPassword) {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/webster-dictionary.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Check if the hashed dictionary word matches the target password
                if (hashSHA(line).equals(targetPassword)) {
                    // Put the correct password into the queue
                    passwordQueue.put(line);
                    return;
                }

                // Try Base64 decoding
                try {
                    byte[] decodedBytes = Base64.getDecoder().decode(targetPassword);
                    String decodedPassword = new String(decodedBytes);

                    // Check if the decoded password matches the guessed password
                    if (line.equals(decodedPassword)) {
                        passwordQueue.put(line);
                        return;
                    }
                } catch (IllegalArgumentException e) {
                    // Invalid Base64 string, continue to next line
                    continue;
                }
            }
        } catch (IOException | NoSuchAlgorithmException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String hashSHA(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = md.digest(input.getBytes());

        // Convert the byte array to a hexadecimal string
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
