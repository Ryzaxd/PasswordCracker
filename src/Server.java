import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private static Map<String, String> passwords = new HashMap<>();

    public static void main(String[] args) {

        loadPasswordsFromFile("src/password.txt");

        try {
            ServerSocket serverSocket = new ServerSocket(1978);
            System.out.println("Accepting connection on port 1978");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connection established from Client: " + socket.getRemoteSocketAddress());
                handleClient(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())
        ) {
            while (true) {
                String incomingText = inputStream.readUTF();
                System.out.println("Text received: " + incomingText);

                // Check if incoming request is for a password
                if (incomingText.startsWith("PASSWORD:")) {
                    String[] parts = incomingText.split(":");
                    if (parts.length == 2) {
                        String username = parts[1];
                        String password = passwords.get(username);
                        if (password != null) {
                            outputStream.writeUTF("Password for " + username + ": " + password);
                        } else {
                            outputStream.writeUTF("No password found for " + username);
                        }
                        outputStream.flush();
                    } else {
                        outputStream.writeUTF("Invalid request format");
                        outputStream.flush();
                    }
                } else {
                    outputStream.writeUTF("Invalid request");
                    outputStream.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void loadPasswordsFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String username = parts[0];
                    String password = parts[1];
                    passwords.put(username, password);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}