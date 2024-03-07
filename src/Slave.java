import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.StringUtilities;

public class Slave {

    private static MessageDigest messageDigest;
    private static final String MASTER_HOST = "localhost";
    private static final int MASTER_PORT = 8080;
    private static final Logger LOGGER = Logger.getLogger("passwordCracker");

    static {
        try {
            messageDigest = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public static void main(final String[] args) throws IOException {
        final long startTime = System.currentTimeMillis();

        final List<UserInfo> userInfos = PasswordFileHandler.readPasswordFile("src/passwords.txt");
        final List<UserInfoClearText> result = new ArrayList<>();
        FileReader fileReader = null;
        try {
            Socket socket = new Socket(MASTER_HOST, MASTER_PORT);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            fileReader = new FileReader("src/webster-dictionary.txt");
            final BufferedReader dictionary = new BufferedReader(fileReader);

            int numThreads = 8;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            List<Future<List<UserInfoClearText>>> futures = new ArrayList<>();

            List<String> dictionaryEntries = new ArrayList<>();
            String line;
            while ((line = dictionary.readLine()) != null) {
                dictionaryEntries.add(line);
            }
            int chunkSize = dictionaryEntries.size() / numThreads;

            for (int i = 0; i < numThreads; i++) {
                int start = i * chunkSize;
                int end = (i == numThreads - 1) ? dictionaryEntries.size() : (i + 1) * chunkSize;
                List<String> subDictionary = dictionaryEntries.subList(start, end);
                Callable<List<UserInfoClearText>> task = () -> {
                    List<UserInfoClearText> partialResult = new ArrayList<>();
                    for (String dictionaryEntry : subDictionary) {
                        List<UserInfoClearText> partialResultSingle = checkWordWithVariations(dictionaryEntry, userInfos);
                        partialResult.addAll(partialResultSingle);
                    }
                    return partialResult;
                };
                futures.add(executor.submit(task));

                try {
                    List<UserInfoClearText> partialResult = futures.get(i).get();
                    result.addAll(partialResult);
                    outputStream.writeObject(result);
                    outputStream.flush();
                    System.out.println("Sent result to Master");
                    System.out.println(result);
                    
                    final long endTime = System.currentTimeMillis();
                    final long usedTime = endTime - startTime;
                    System.out.println("Used time: " + usedTime / 1000 + " seconds = " + usedTime / 60000.0 + " minutes");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            outputStream.close();
            executor.shutdown();
        } finally {
            if (fileReader != null) {
                fileReader.close();
            }
        }
    } 

    static List<UserInfoClearText> checkWordWithVariations(final String dictionaryEntry, final List<UserInfo> userInfos) {
        final List<UserInfoClearText> result = new ArrayList<UserInfoClearText>();

        final String possiblePassword = dictionaryEntry;
        final List<UserInfoClearText> partialResult = checkSingleWord(userInfos, possiblePassword);
        result.addAll(partialResult);

        final String possiblePasswordUpperCase = dictionaryEntry.toUpperCase();
        final List<UserInfoClearText> partialResultUpperCase = checkSingleWord(userInfos, possiblePasswordUpperCase);
        result.addAll(partialResultUpperCase);

        final String possiblePasswordCapitalized = StringUtilities.capitalize(dictionaryEntry);
        final List<UserInfoClearText> partialResultCapitalized  = checkSingleWord(userInfos, possiblePasswordCapitalized);
        result.addAll(partialResultCapitalized);

        final String possiblePasswordReverse = new StringBuilder(dictionaryEntry).reverse().toString();
        final List<UserInfoClearText> partialResultReverse = checkSingleWord(userInfos, possiblePasswordReverse);
        result.addAll(partialResultReverse);

        for (int i = 0; i < 100; i++) {
            final String possiblePasswordEndDigit = dictionaryEntry + i;
            final List<UserInfoClearText> partialResultEndDigit= checkSingleWord(userInfos, possiblePasswordEndDigit);
            result.addAll(partialResultEndDigit);
        }

        for (int i = 0; i < 100; i++) {
            final String possiblePasswordStartDigit = i + dictionaryEntry;
            final List<UserInfoClearText> partialResultStartDigit = checkSingleWord(userInfos, possiblePasswordStartDigit);
            result.addAll(partialResultStartDigit);
        }

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 100; j++) {
                final String possiblePasswordStartEndDigit = i + dictionaryEntry + j;
                final List<UserInfoClearText> partialResultStartEndDigit = checkSingleWord(userInfos, possiblePasswordStartEndDigit);
                result.addAll(partialResultStartEndDigit);
            }
        }

        return result;
    }

    static List<UserInfoClearText> checkSingleWord(final List<UserInfo> userInfos, final String possiblePassword) {
        final byte[] digest = messageDigest.digest(possiblePassword.getBytes());
        final List<UserInfoClearText> results = new ArrayList<UserInfoClearText>();
        for (UserInfo userInfo : userInfos) {
            if (Arrays.equals(userInfo.getEntryptedPassword(), digest)) {
                results.add(new UserInfoClearText(userInfo.getUsername(), possiblePassword));
            }
        }
        return results;
    }
}
