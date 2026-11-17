package model;

/** Data pengguna yang sedang login. */
public class User {
    private final int id;
    private final String name;
    private final String role; // contoh: admin, rt, rw (samakan dengan tab Role/Hak Akses)

    public User(int id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
}
