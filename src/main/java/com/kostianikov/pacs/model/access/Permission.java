package com.kostianikov.pacs.model.access;

public enum Permission {
    READ_SELF("read:self"),
    WRIRE_SELF("write:self"),
    READ_ALL("read:all"),
    UPLOAD_FILE("write:file");
    private final String permision;

    Permission(String permision) {
        this.permision = permision;
    }

    public String getPermision() {
        return permision;
    }
}
