package model;

/** Data pengguna yang sedang login. */
public class User {
    private final int id;
    private final String name;
    private final String role;
    private final String organizationName;
    private final String region;

    public User(int id, String name, String role, String organizationName, String region) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.organizationName = organizationName;
        this.region = region;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getOrganizationName() { return organizationName; }
    public String getRegion() { return region; }
}
