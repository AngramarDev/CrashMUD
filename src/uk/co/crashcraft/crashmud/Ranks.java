package uk.co.crashcraft.crashmud;

public enum Ranks {
    MORTAL          (0),
    IMMORTAL        (1),
    WIZARD          (2),
    GOD             (3);

    private int code;

    private Ranks(int c) {
        code = c;
    }

    public int getCode() {
        return code;
    }
}
