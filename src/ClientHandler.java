import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BlockingQueue<String> finalPasswordQueue;

    public ClientHandler(Socket clientSocket, BlockingQueue<String> passwordQueue) {
        this.clientSocket = clientSocket;
        this.finalPasswordQueue = passwordQueue;
    }

    @Override
    public void run() {
        try (
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            while (true) {
                // Read exit command from client
                if ("exit".equalsIgnoreCase(in.readUTF())) {
                    break;
                }

                // Send confirmation to client
                out.writeUTF("Request received. Processing...");

                // Check if any passwords have been found
                boolean passwordsFound = false;

                // Loop through the passwordQueue
                String username;
                while ((username = finalPasswordQueue.poll()) != null) {
                    // Check if the password is available for this username
                    String decryptedPassword = finalPasswordQueue.poll();
                    if (decryptedPassword != null && !decryptedPassword.isEmpty()) {
                        // Send the username and its decrypted password to the client
                        out.writeUTF("Username: " + username + ", Password: " + decryptedPassword);
                        passwordsFound = true;
                    }
                }

                // If no passwords found, inform the client
                if (!passwordsFound) {
                    out.writeUTF("No passwords found.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
