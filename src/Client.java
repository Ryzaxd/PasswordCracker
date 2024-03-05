import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {

        try {
            Socket socket = new Socket("localhost", 1978);
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Welcome to the password checker");
                System.out.println("Enter username: ");
                String targetUsername = scanner.nextLine();
                outputStream.writeUTF(targetUsername);

                String response = inputStream.readLine();
                System.out.println(response);

                if (response.startsWith("Password found")) {
                    String[] parts = response.split(":");
                    System.out.println("Password found for " + targetUsername + ": " + parts[1]);
                    System.out.println("Do you want to continue? (Type 'exit' to quit)");
                    String choice = scanner.nextLine();
                    if (choice.equals("exit")) {
                        break;
                    }
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}