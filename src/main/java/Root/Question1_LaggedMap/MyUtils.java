package Root.Question1_LaggedMap;

import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public final class MyUtils {
    private MyUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Better to use "ScheduledExecutorService", much Nicer :D
    public static Thread delayBeforeActionThread(Runnable actionToDo, int draftedSeconds) {
        return new Thread(() -> {
            long startTime = System.currentTimeMillis();

            while (true) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                int seconds = (int) (elapsedTime / 1000);

                if (seconds >= draftedSeconds) {
                    break;
                }

                try {
                    Thread.sleep(1 * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            // Only Run If Not Interrupted
            if (!Thread.currentThread().isInterrupted()) {
                actionToDo.run();
            }
        });
    }

    public static <K, V> Thread cleanerThread(ConcurrentHashMap<K, Stack<V>> valuesHistory) {
        return new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                valuesHistory.forEach((key, history) -> {
                    if (history != null) {
                        synchronized (history) {
                            while (history.size() > Constants.MAX_HISTORY_SIZE) {
                                history.removeFirst();
                            }
                        }
                    }
                });

                try {
                    Thread.sleep(Constants.CLEANER_THREAD_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    public static <K, V> Thread snapshotSaverThread(ConcurrentHashMap<K, V> publishedMap, ConcurrentHashMap<K, V> publishedMapSnapshot) {
        return new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                ConcurrentHashMap<K, V> snapshot = new ConcurrentHashMap<>(publishedMap);
                publishedMapSnapshot.clear();
                publishedMapSnapshot.putAll(snapshot);

                try {
                    Thread.sleep(Constants.SNAPSHOT_SAVER_THREAD_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
}