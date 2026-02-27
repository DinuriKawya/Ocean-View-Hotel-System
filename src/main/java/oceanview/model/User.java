package oceanview.model;

public class User {

    private int userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private Role role;
    private UserStatus status;

    public User() {}

    public User(int userId, String username, String passwordHash,
                String fullName, Role role, UserStatus status) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
    }

    public int getUserId()             { return userId; }
    public String getUsername()        { return username; }
    public String getPasswordHash()    { return passwordHash; }
    public String getFullName()        { return fullName; }
    public Role getRole()              { return role; }
    public UserStatus getStatus()      { return status; }

    public void setUserId(int userId)              { this.userId = userId; }
    public void setUsername(String username)        { this.username = username; }
    public void setPasswordHash(String passwordHash){ this.passwordHash = passwordHash; }
    public void setFullName(String fullName)        { this.fullName = fullName; }
    public void setRole(Role role)                  { this.role = role; }
    public void setStatus(UserStatus status)        { this.status = status; }

    public boolean isAdmin()  { return Role.ADMIN == this.role; }
    public boolean isActive() { return UserStatus.ACTIVE == this.status; }
}
