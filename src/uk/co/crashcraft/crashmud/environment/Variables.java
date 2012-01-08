package uk.co.crashcraft.crashmud.environment;

public class Variables {
    public enum Environment {
        DOOR_OPEN       (1),
        DOOR_CLOSED     (2),
        DOOR_LOCKED     (3),
        DOOR_PROTECTED  (4),
        DOOR_NPCBARRED  (5);

        private int code;

        private Environment(int c) {
            code = c;
        }

        public int getCode() {
            return code;
        }
    }

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
}
