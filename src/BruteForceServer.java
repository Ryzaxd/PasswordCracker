import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.Base64;

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
    public static String decodedPassword;

    public Master(BlockingQueue<String> passwordQueue) {
        this.passwordQueue = passwordQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Take a guess from the queue
                String guess = passwordQueue.take();

                // Check if the guess is correct
                try (BufferedReader reader = new BufferedReader(new FileReader("src/password.txt"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(":");
                        if (parts.length == 2) {
                            // Decode the Base64 encoded password
                            byte[] decodedBytes = Base64.getDecoder().decode(parts[1]);
                            String decodedPassword = new String(decodedBytes);

                            if (decodedPassword.equals(guess)) {
                                System.out.println("Password found for " + targetUsername + ": " + guess);
                                // Notify other components about the successful match
                                Master.decodedPassword = decodedPassword;
                                return;
                            }
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
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
        // Read the dictionary file line by line and add each line as a guess to the queue
        try (BufferedReader reader = new BufferedReader(new FileReader("src/webster-dictionary.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String encodedGuess = Base64.getEncoder().encodeToString(line.getBytes());
    
                try {
                    passwordQueue.put(encodedGuess);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BlockingQueue<String> passwordQueue;

    public ClientHandler(Socket clientSocket, BlockingQueue<String> passwordQueue) {
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

                // Add the username to the queue for brute force
                passwordQueue.put(Master.targetUsername);

                // Send a message back to the client indicating that the username was received
                out.writeUTF("Username received: " + Master.targetUsername);

                if (Master.decodedPassword != null && !Master.decodedPassword.isEmpty()) {
                    out.writeUTF("Password found for " + Master.targetUsername + ": " + Master.decodedPassword);
                } else {
                    out.writeUTF("Password not found for " + Master.targetUsername);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
