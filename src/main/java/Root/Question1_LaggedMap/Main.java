package Root.Question1_LaggedMap;

import net.datafaker.Faker;
import java.util.Random;

public class Main {
    // ANSI Console Color Codes for scannable terminal logs 🎨
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String PURPLE = "\u001B[35m";

    public static void main(String[] args) {
        System.out.println("Hello, World :D");

        // For Testing, Remove Later
        System.out.println(CYAN + "🚀 Starting Infinite Live-Data Stress Test..." + RESET);

        // 1. Initialize LaggedMap with a 3-second publishing delay
        LaggedMap<String, String> laggedMap = new LaggedMap<>(3);

        // 2. Initialize Faker and Random generators
        Faker faker = new Faker();
        Random random = new Random();

        // 3. Keep the key pool small so updates and removals constantly cross paths
        String[] trackedKeys = {"User_Post_A", "User_Post_B", "User_Post_C"};

        // 4. Infinite execution block
        while (true) {
            try {
                int actionSelector = random.nextInt(100);
                String randomKey = trackedKeys[random.nextInt(trackedKeys.length)];

                if (actionSelector < 60) {
                    // 60% chance to draft an update
                    String fakeBookTitle = faker.book().title();
                    System.out.println(GREEN + "📝 [ACTION] PUT -> Drafting title \"" + fakeBookTitle + "\" for " + randomKey + RESET);
                    laggedMap.put(randomKey, fakeBookTitle);

                } else if (actionSelector < 80) {
                    // 20% chance to draft a removal
                    boolean fullDelete = random.nextBoolean();
                    System.out.println(RED + "🗑️ [ACTION] REMOVE -> Drafting " + (fullDelete ? "FULL" : "PARTIAL") + " deletion for " + randomKey + RESET);
                    laggedMap.remove(randomKey, fullDelete);

                } else if (actionSelector < 93) {
                    // 13% chance to fire a manual intercept
                    System.out.println(YELLOW + "🛑 [ACTION] ABORT! Attempting to intercept all active drafts..." + RESET);
                    laggedMap.abort();

                } else {
                    // 7% chance to execute a system recovery
                    System.out.println(PURPLE + "🔄 [ACTION] CRITICAL ROLLBACK! Restoring system snapshot..." + RESET);
                    laggedMap.rollback();
                }

                // 5. Print the complete system state across all maps
                printSystemState(laggedMap);

                // Sleep between 500ms and 1500ms before triggering the next action event
                Thread.sleep(random.nextInt(1000) + 500);

            } catch (InterruptedException e) {
                System.out.println("Simulation thread interrupted. Exiting loop.");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("💥 Loop Execution Error: " + e.getMessage());
            }
        }
    }

    /**
     * Prints a comprehensive visual report of the map architectures
     */
    private static void printSystemState(LaggedMap<String, String> laggedMap) {
        System.out.println(CYAN + "\n=================== CURRENT MAP DUMP ===================" + RESET);

        // 1. Fetch live published contents
        System.out.println("🌍 Live Published Map:");
        if (laggedMap.getPublishedMap().isEmpty()) {
            System.out.println("   (Empty - everything is currently drafting or empty)");
        } else {
            laggedMap.getPublishedMap().forEach((k, v) -> System.out.println("   • " + k + " => " + v));
        }

        // 2. Fetch the automatic snapshot map state
        System.out.println("\n💾 Minute-Snapshot Map Status:");
        if (laggedMap.getPublishedMapSnapshot().isEmpty()) {
            System.out.println("   (Empty - snapshot cleaner hasn't run yet)");
        } else {
            laggedMap.getPublishedMapSnapshot().forEach((k, v) -> System.out.println("   • [Backup] " + k + " => " + v));
        }

        // 3. Display history stack counts
        System.out.println("\n⏳ History Stack Sizes:");
        if (laggedMap.getValuesHistory().isEmpty()) {
            System.out.println("   (No historic undo data tracked yet)");
        } else {
            laggedMap.getValuesHistory().forEach((k, stack) ->
                    System.out.println("   • " + k + ": " + stack.size() + " versions deep (Top: " + (stack.isEmpty() ? "None" : stack.peek()) + ")")
            );
        }

        // 4. Thread Counter
        System.out.println("\n🧵 Pending Background Countdown Threads: " + laggedMap.getActiveThreads().size());
        System.out.println(CYAN + "========================================================" + RESET + "\n");
    }
}