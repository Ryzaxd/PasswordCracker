import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class BruteForceServer {

    public static void main(String[] args) {

        BlockingQueue<String> passwordQueue = new LinkedBlockingQueue<>();
        BlockingQueue<String> finalPasswordQueue = new LinkedBlockingQueue<>();

        Thread masterThread = new Thread(new Master(passwordQueue, finalPasswordQueue));
        masterThread.start();

        final int numSlaves = 6;
        for (int i = 0; i < numSlaves; i++) {
            Thread slaveThread = new Thread(new Slave(passwordQueue));
            slaveThread.start();
        }

        try {
            ServerSocket serverSocket = new ServerSocket(1978);
            System.out.println("Server is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                Thread clientThread = new Thread(new ClientHandler(clientSocket, passwordQueue));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

