package Root.Question1_LaggedMap;

public final class Constants {
    private Constants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final int MAX_HISTORY_SIZE = 3;
    public static final int CLEANER_THREAD_INTERVAL = 1 * 1000;
    public static final int SNAPSHOT_SAVER_THREAD_INTERVAL = 60 * 1000;
}