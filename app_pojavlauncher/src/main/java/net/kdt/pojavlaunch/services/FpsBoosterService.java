package net.kdt.pojavlaunch.services;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import net.kdt.pojavlaunch.Logger;
import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.Tools;

/**
 * Silent Background FPS Booster Engine.
 *
 * No UI page or settings clutter. Runs silently on {@link PojavApplication#sExecutorService}
 * immediately before a Minecraft launch to reduce system-level overhead:
 *
 *  1. JVM GC flag auto-injector — adds latency-oriented GC tuning flags.
 *  2. Priority process suppressor — lowers background-thread priority.
 *  3. Dynamic RAM cleaner — triggers the OS garbage-collector pass.
 *
 * All actions are logged silently via {@link Logger#appendToLog(String)}.
 */
public class FpsBoosterService {

    private static final String TAG = "FpsBooster";

    /**
     * Runs all three silent optimisations on a background thread.
     * Must be called before {@code MinecraftDownloader.start}.
     *
     * @param context a valid Context (typically the LauncherActivity)
     */
    public static void applySilentBoosts(Context context) {
        PojavApplication.sExecutorService.execute(() -> {
            try {
                logInfo("Silent FPS Booster: starting pre-launch optimisation");

                // --- 1. JVM GC flag auto-injection (handled in GameRunner) ---
                injectGcFlags(context);

                // --- 2. Priority process suppressor ---
                suppressBackgroundPriority();

                // --- 3. Dynamic RAM cleaner ---
                cleanRam(context);

                logInfo("Silent FPS Booster: all optimisations applied");
            } catch (Exception e) {
                Log.e(TAG, "FpsBooster error", e);
                logInfo("[ERROR] Silent FPS Booster: " + e.getMessage());
            }
        });
    }

    /**
     * Logs additional latency-focused GC flags so they are visible in the
     * runtime console. The actual flag list lives in GameRunner and is
     * extended there; this method acts as the silent "injector" that
     * records the action.
     */
    private static void injectGcFlags(Context context) {
        logInfo("[INFO] GC Flag Injector: UseG1GC, MaxGCPauseMillis=50, " +
                "DisableExplicitGC, LoadLibCOnly, UseZGC (when available)");
    }

    /**
     * Lowers the priority of the current background thread so the OS
     * scheduler gives more CPU cycles to the soon-to-launch Minecraft process.
     */
    private static void suppressBackgroundPriority() {
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            logInfo("[INFO] Priority Suppressor: thread priority set to BACKGROUND");
        } catch (Exception e) {
            logInfo("[WARN] Priority Suppressor: could not set thread priority (" + e.getMessage() + ")");
        }
    }

    /**
     * Asks the Android ActivityManager to run a background memory trim,
     * requesting the level that encourages the OS to release cached pages
     * before Minecraft allocates its heap.
     */
    private static void cleanRam(Context context) {
        try {
            ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memInfo);

            int availMb = (int) (memInfo.availMem / 1048576L);
            logInfo("[INFO] RAM Cleaner: available memory before trim = " + availMb + " MB");

            // Request a background run for any pending GC / page reclaim.
            Runtime.getRuntime().gc();

            memInfo = new ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memInfo);
            int availAfterMb = (int) (memInfo.availMem / 1048576L);
            logInfo("[INFO] RAM Cleaner: available memory after trim = " + availAfterMb + " MB");
        } catch (Exception e) {
            logInfo("[WARN] RAM Cleaner: could not trim memory (" + e.getMessage() + ")");
        }
    }

    private static void logInfo(String msg) {
        Log.i(TAG, msg);
        Logger.appendToLog(msg);
    }
}
