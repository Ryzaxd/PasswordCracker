import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Master {
    private static final int PORT = 8080;
    private static final Logger LOGGER = Logger.getLogger("passwordCracker");

    public static void main(String[] args) {
        new Master().start();
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Master started. Listening on port " + PORT);

            while (true) {
                Socket slaveSocket = serverSocket.accept();
                System.out.println("Slave connected: " + slaveSocket.getInetAddress());

                ObjectOutputStream outputStream = new ObjectOutputStream(slaveSocket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(slaveSocket.getInputStream());

                new Thread(() -> {
                    try {
                        while (true) {
                            List<UserInfoClearText> result = (List<UserInfoClearText>) inputStream.readObject();
                            System.out.println("Received result from Slave:");
                            for (UserInfoClearText userInfo : result) {
                                System.out.println(userInfo.getUsername() + ": " + userInfo.getPassword());
                            }
                            final long startTime = System.currentTimeMillis();
                            final long endTime = System.currentTimeMillis();
                            final long usedTime = endTime - startTime;
                            System.out.println("Used time: " + usedTime / 1000 + " seconds = " + usedTime / 60000.0 + " minutes");
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage());
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}