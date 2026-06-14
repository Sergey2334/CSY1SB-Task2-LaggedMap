package Root;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class LaggedMap<K, V> {
    private ConcurrentHashMap<K, V> publishedMap; // ConcurrentHashMap and Not HashMap For Thread Safety :D
    private ConcurrentHashMap<K, V> publishedMapSnapshot; // Snapshot of publishedMap, Each Minute
    private ConcurrentHashMap<K, Stack<V>> valuesHistory;
    private final List<Thread> activeThreads = new CopyOnWriteArrayList<>(); // For abort()
    private int draftedSeconds;

    public LaggedMap(int draftedSeconds) {
        this.publishedMap = new ConcurrentHashMap<>();
        this.publishedMapSnapshot = new ConcurrentHashMap<>();
        this.valuesHistory = new ConcurrentHashMap<>();
        this.draftedSeconds = draftedSeconds;

        MyUtils.cleanerThread(this.valuesHistory).start(); // Starts The History Cleaner Thread
        MyUtils.snapshotSaverThread(this.publishedMap, this.publishedMapSnapshot).start(); // Starts The Snapshot Saver Thread
    }

    public void put(K key, V value) {
        if (key == null || value == null) {
            return;
        }

        Stack<V> historyStack = this.valuesHistory.get(key);
        if (historyStack == null) {
            historyStack = new Stack<>();
        }

        synchronized (historyStack) {
            historyStack.push(value);
            this.valuesHistory.put(key, historyStack);
        }

        Thread putThread = MyUtils.delayBeforeActionThread(this.publishAction(key, value), this.draftedSeconds);
        this.activeThreads.add(putThread);
        putThread.start();
    }

    public V get(K key) {
        return this.publishedMap.get(key);
    }

    public void abort() {
        for (Thread thread : this.activeThreads) {
            if (thread.isAlive()) {
                thread.interrupt();
            }
        }
        this.activeThreads.clear();
    }

    public void remove(K key, boolean full) {
        Thread removeThread = MyUtils.delayBeforeActionThread(this.removeAction(key, full), this.draftedSeconds);
        this.activeThreads.add(removeThread);
        removeThread.start();
    }

    public void rollback() {
        this.abort();
        this.publishedMap.clear();
        this.publishedMap.putAll(this.publishedMapSnapshot);
        this.resetHistory();
    }

    private void resetHistory() {
        this.valuesHistory.clear();
    }

    private Runnable publishAction(K key, V value) {
        return () -> {
            try {
                // Only modify if abort() didn't interrupt
                if (!Thread.currentThread().isInterrupted()) {
                    this.publishedMap.put(key, value);
                }
            } finally {
                // If Thread got Interrupted
                this.activeThreads.remove(Thread.currentThread());
            }
        };
    }

    private Runnable removeAction(final K key, final boolean full) {
        return () -> {
            try {
                // Only modify if abort() didn't interrupt
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                if (full) {
                    this.publishedMap.remove(key);
                    this.valuesHistory.remove(key);
                } else {
                    Stack<V> history = this.valuesHistory.get(key);
                    if (history == null || history.size() <= 1) {
                        this.publishedMap.remove(key);
                        this.valuesHistory.remove(key);
                    } else {
                        synchronized (history) {
                            history.pop();
                            V currentValue = history.peek();
                            this.publishedMap.put(key, currentValue);
                        }
                    }
                }
            } finally {
                // If Thread got Interrupted
                this.activeThreads.remove(Thread.currentThread());
            }
        };
    }

    // For Testing, Remove Later
    public ConcurrentHashMap<K, V> getPublishedMap() { return this.publishedMap; }
    public ConcurrentHashMap<K, V> getPublishedMapSnapshot() { return this.publishedMapSnapshot; }
    public ConcurrentHashMap<K, Stack<V>> getValuesHistory() { return this.valuesHistory; }
    public List<Thread> getActiveThreads() { return this.activeThreads; }

/*
Question 8:
    The 2-Second Showdown 🤠🌵
    Two threads walk into a map at the exact same microsecond...
    Option 1 (remove() wins the draw):
        It pulls the trigger first → History popped → "publishedMap" updated
    Option 2 (abort() executes a sneak attack):
        It tackles the thread → Action canceled → Status quo preserved
    May the fastest CPU core win.
*/
}


/*
LaggedMap<K,V>
K = Some Element    (ID     , Name      , Mail)
V = Value           (Score  , Birthday  , Password)

Enum State {DRAFT, PUBLISHED}
Enum Change {PUT, UPDATE, REMOVE}

Constructor:
    drafterSeconds

Methods:
    put(K key, V value) : Returns Boolean ?
    Update(K key, V value) : Returns Boolean ?
    Remove(K key, boolean full) : Returns boolean ?
                                If full = true -> Remove Key and All History Value
                                If full = false -> Remove last in History and Update Now Last to be Current
                                If Key is Empty , Remove Key
    get(K key) : Returns V value
    abort() : Return boolean ?
            Removes all Changes in Draft

Before any Change to the Collection (Add, Update, Remove) it gets into the Drafted State, and you cannot Read it
After it Changes to Published it can be Read

After drafterSeconds the "Publish" Phase begins

While in "Drafter" State... "abort()" (Cancel) can occur, all Changes that were Made are Canceled and are Not Continuing into the Publish Phase

Each Element Has a History of What it has been (Up to Last 3 Changes Made)
For Each Key Element in the LaggedMap there will be a Running Thread that Deleted Old History of the Elements (Checks Once Per Second)
*/