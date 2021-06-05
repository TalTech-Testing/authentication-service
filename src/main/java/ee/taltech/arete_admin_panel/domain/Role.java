package ee.taltech.arete_admin_panel.domain;

public enum Role {
    ADMIN("ADMIN"), USER("USER"), DEVELOPER("DEVELOPER"), HOOK("HOOK"), TESTER("TESTER");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public static Role fromValue(String value) {
        if (value != null) {
            for (Role role : values()) {
                if (role.value.equals(value)) {
                    return role;
                }
            }
        }

        throw new IllegalArgumentException("Invalid role: " + value);
    }

    public String toValue() {
        return value;
    }
}
