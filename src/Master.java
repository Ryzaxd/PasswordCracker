import java.util.concurrent.BlockingQueue;

public class Master implements Runnable {
    private final BlockingQueue<String> passwordQueue;
    private final BlockingQueue<String> finalPasswordQueue;

    public Master(BlockingQueue<String> passwordQueue, BlockingQueue<String> finalPasswordQueue) {
        this.passwordQueue = passwordQueue;
        this.finalPasswordQueue = finalPasswordQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Take a password from the initial queue
                String username = passwordQueue.take();
                String password = passwordQueue.take();

                // Put the password into the final queue
                finalPasswordQueue.put(username);
                finalPasswordQueue.put(password);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}