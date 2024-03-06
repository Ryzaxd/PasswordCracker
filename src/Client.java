import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {

        try (Socket socket = new Socket("localhost", 1978);
             DataInputStream inputStream = new DataInputStream(socket.getInputStream());
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println("Enter 'start' or 'exit' to quit: ");
                String input = scanner.nextLine();

                if ("start".equalsIgnoreCase(input)) {
                    outputStream.writeUTF(input);
                    outputStream.flush();
                    inputStream.readUTF();

                } else if ("exit".equalsIgnoreCase(input)) {
                    outputStream.writeUTF(input);
                    outputStream.flush();
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
