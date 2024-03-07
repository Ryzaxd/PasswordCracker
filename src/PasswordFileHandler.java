import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

public class PasswordFileHandler {

    public static final String MESSAGE_DIGEST_ALGORITHM = "SHA";

    public static void writePasswordFile(final String filename, final String[] usernames, final String[] passwords) throws NoSuchAlgorithmException, IOException {
        final MessageDigest messageDigest = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM);
        if (usernames.length != passwords.length) {
            throw new IllegalArgumentException("usernames and passwords must be same lengths");
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filename);
            final Base64.Encoder base64Encoder = Base64.getEncoder();
            for (int i = 0; i < usernames.length; i++) {
                final byte[] encryptedPassword = messageDigest.digest(passwords[i].getBytes());
                final String line = usernames[i] + ":" + base64Encoder.encode(encryptedPassword) + "\n";
                fos.write(line.getBytes());
            }
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    public static List<UserInfo> readPasswordFile(final String filename) throws IOException {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(filename);
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            final List<UserInfo> result = new ArrayList<UserInfo>();
            while (true) {
                final String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                final String[] parts = line.split(":");
                final UserInfo userInfo = new UserInfo(parts[0], parts[1]);
                result.add(userInfo);
            }
            return result;
        } finally {
            if (fileReader != null) {
                fileReader.close();
            }
        }
    }
}
