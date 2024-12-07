package com.example.reader;

public enum MenuAction {
    OPEN_FILE(R.id.btnOpenFile),
    ZOOM_IN(R.id.btnZoomIn),
    ZOOM_OUT(R.id.btnZoomOut),
    TOGGLE_NIGHT_MODE(R.id.menu_toggle_mode);

    private final int id;

    MenuAction(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static MenuAction fromId(int id) {
        for (MenuAction action : values()) {
            if (action.getId() == id) {
                return action;
            }
        }
        return null;
    }
}
