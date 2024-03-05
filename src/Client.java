import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {

        try {
            Socket socket = new Socket("localhost", 1978);
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Enter a username to get the password or 'exit' to quit:");
                String input = scanner.nextLine();

                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }

                outputStream.writeUTF(input);
                outputStream.flush();
                System.out.println(inputStream.readUTF());
            }

            scanner.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}