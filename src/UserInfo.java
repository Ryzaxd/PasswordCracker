import java.io.Serializable;
import java.util.Base64;

public class UserInfo implements Serializable {

    private final String username;
    private final String entryptedPasswordBASE64;
    private final byte[] entryptedPassword;

    public UserInfo(final String username, final String entryptedPasswordBASE64) {
        if (username == null) {
            throw new IllegalArgumentException("username is null");
        }
        if (entryptedPasswordBASE64 == null) {
            throw new IllegalArgumentException("entryptedPasswordBASE64 is null");
        }
        this.username = username;
        this.entryptedPasswordBASE64 = entryptedPasswordBASE64;
        this.entryptedPassword = Base64.getDecoder().decode(entryptedPasswordBASE64);
    }

    public String getEntryptedPasswordBASE64() {
        return entryptedPasswordBASE64;
    }

    public byte[] getEntryptedPassword() {
        return entryptedPassword;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return username + ":" + entryptedPasswordBASE64;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserInfo other = (UserInfo) obj;
        if ((this.username == null) ? (other.username != null) : !this.username.equals(other.username)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + (this.username != null ? this.username.hashCode() : 0);
        return hash;
    }

}
