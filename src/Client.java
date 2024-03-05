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
            while(true) {
                outputStream.writeUTF(scanner.nextLine());
                outputStream.flush();
                System.out.println(inputStream.readUTF());

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}