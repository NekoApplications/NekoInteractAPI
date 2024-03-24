package icu.takeneko.interact.mcdr;

public enum PermissionLevel {
    OWNER(4,"owner"),
    ADMIN(3,"admin"),
    HELPER(2,"helper"),
    USER(1,"user"),
    GUEST(0,"guest");

    private final int level;
    private final String descriptor;

    PermissionLevel(int level,String descriptor) {
        this.level = level;
        this.descriptor = descriptor;
    }

    public static PermissionLevel of(int i){
        for (PermissionLevel value : PermissionLevel.values()) {
            if (value.level == i)return value;
        }
        return GUEST;
    }
}
