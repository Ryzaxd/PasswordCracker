import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.Queue;
import java.util.Scanner;

public class BruteForceServer {

    public static void main(String[] args) {
        // Create a shared memory queue between Master and Slaves
        BlockingQueue<String> passwordQueue = new LinkedBlockingQueue<>();

        // Start Master thread
        Thread masterThread = new Thread(new Master(passwordQueue));
        masterThread.start();

        // Start Slave threads
        final int numSlaves = 4;
        for (int i = 0; i < numSlaves; i++) {
            Thread slaveThread = new Thread(new Slave(passwordQueue));
            slaveThread.start();
        }

        // Start the server
        try {
            ServerSocket serverSocket = new ServerSocket(1978);
            System.out.println("Server is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Start a thread to handle the client
                Thread clientThread = new Thread(new ClientHandler(clientSocket, passwordQueue));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Master implements Runnable {
    private final BlockingQueue<String> passwordQueue;
    public static String targetUsername;

    public Master(BlockingQueue<String> passwordQueue) {
        this.passwordQueue = passwordQueue;
    }

    @Override
    public void run() {
        // Nothing needs to be done here since client threads handle username/password retrieval
    }
}

class Slave implements Runnable {
    private final BlockingQueue<String> passwordQueue;

    public Slave(BlockingQueue<String> passwordQueue) {
        this.passwordQueue = passwordQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String password = passwordQueue.take();
                // Perform brute force operation here
                bruteForce(password);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void bruteForce(String targetPassword) {
        // Read passwords from password.txt and compare with targetPassword
        try (BufferedReader reader = new BufferedReader(new FileReader("src/password.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[1].equals(targetPassword)) {
                    System.out.println("Password found for " + Master.targetUsername + ": " + parts[1]);
                    // Notify Master about the successful match
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private Queue<String> passwordQueue;

    public ClientHandler(Socket clientSocket, Queue<String> passwordQueue) {
        this.clientSocket = clientSocket;
        this.passwordQueue = passwordQueue;
    }

    @Override
    public void run() {
        try (
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            while (true) {
                Master.targetUsername = in.readUTF();
    
                if ("exit".equalsIgnoreCase(Master.targetUsername)) {
                    break;
                }
    
                // Find the password associated with the entered username
                try (Scanner scanner = new Scanner(new FileReader("src/password.txt"))) {
                    boolean usernameFound = false;
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        String[] parts = line.split(":");
                        if (parts.length == 2 && parts[0].equals(Master.targetUsername)) {
                            ((BlockingQueue<String>) passwordQueue).put(parts[1]); // Add password to queue for brute force
                            out.writeUTF("Password found for " + Master.targetUsername + ": " + parts[1]);
                            usernameFound = true;
                            break; // Stop searching for the username once found
                        }
                    }
                    if (!usernameFound) {
                        out.writeUTF("Username not found.");
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}