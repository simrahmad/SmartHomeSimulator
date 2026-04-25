import exceptions.AuthenticationException;
import exceptions.UnauthorizedAccessException;
import java.io.Serializable;

public class User implements Serializable {

    // ── Fields ───────────────────────────────────────────────
    protected String name;
    protected String password;
    protected String role;
    protected boolean loggedIn;
    protected int failedAttempts; // track wrong password attempts
    private static final long serialVersionUID = 1L;

    // ── Constructors ─────────────────────────────────────────
    public User(String name, String password, String role) {
        this.name           = name;
        this.password       = password;
        this.role           = role;
        this.loggedIn       = false;
        this.failedAttempts = 0;
    }

    public User(String name, String password) {
        this(name, password, "Guest");
    }

    // ── Getters & Setters ────────────────────────────────────
    public String getName()         { return name; }
    public void   setName(String n) { this.name = n; }

    public String getRole()         { return role; }
    public void   setRole(String r) { this.role = r; }

    public boolean isLoggedIn()     { return loggedIn; }

    // ── Business methods ─────────────────────────────────────

    // throws exception on wrong password or too many attempts
    public void login(String inputPassword) throws AuthenticationException {
        if (failedAttempts >= 3) {
            throw new AuthenticationException(
                    "[Auth] " + name + " is locked out due to too many failed attempts!"
            );
        }
        if (!this.password.equals(inputPassword)) {
            failedAttempts++;
            throw new AuthenticationException(
                    "[Auth] Incorrect password for " + name + ". "
                            + "Attempts remaining: " + (3 - failedAttempts)
            );
        }
        // correct password
        this.loggedIn       = true;
        this.failedAttempts = 0; // reset failed attempts on success
        System.out.println("[Auth] " + name + " logged in as " + role + ".");
    }

    // throws exception if user is not logged in
    public void logout() throws AuthenticationException {
        if (!loggedIn) {
            throw new AuthenticationException(
                    "[Auth] " + name + " is not logged in!"
            );
        }
        this.loggedIn = false;
        System.out.println("[Auth] " + name + " logged out.");
    }

    // call this before any sensitive action
    public void checkLoggedIn() throws UnauthorizedAccessException {
        if (!loggedIn) {
            throw new UnauthorizedAccessException(
                    "[Auth] " + name + " must be logged in to perform this action!"
            );
        }
    }

    // ── Override toString ────────────────────────────────────
    @Override
    public String toString() {
        return "User{name='" + name + "', role='" + role + "', loggedIn=" + loggedIn + "}";
    }
}