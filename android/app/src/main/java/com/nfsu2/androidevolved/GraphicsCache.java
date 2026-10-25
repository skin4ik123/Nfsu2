package com.nfsu2.androidevolved;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.os.Process;
import java.io.File;
import java.util.List;

/** Only disposable graphics caches in this application's private cache directory. */
final class GraphicsCache {
    // Keep in sync with configurePipelineCache() in main.cpp. Epoch 2 excludes
    // caches from the previous DXVK preparation; those files remain untouched.
    static final String DIRECTORY = "dxvk-v2";

    static void prompt(Activity activity) {
        new AlertDialog.Builder(activity).setTitle("Сбросить кэш графики")
                .setMessage("Текущая сессия игры будет закрыта. Шейдеры заново скомпилируются во время игры; сохранения и настройки останутся.")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сбросить", (dialog, which) -> reset(activity)).show();
    }

    private static void reset(Activity activity) {
        try {
            ActivityManager manager = (ActivityManager) activity.getSystemService(Activity.ACTIVITY_SERVICE);
            if (manager == null) throw new IllegalStateException("Не удалось проверить процесс игры.");
            List<ActivityManager.RunningAppProcessInfo> running = manager.getRunningAppProcesses();
            if (running != null) for (ActivityManager.RunningAppProcessInfo process : running)
                if (process.uid == Process.myUid() && process.processName.equals(activity.getPackageName() + ":game"))
                    Process.killProcess(process.pid);
            File cache = new File(activity.getCacheDir(), DIRECTORY);
            if (cache.exists()) {
                File backup = new File(activity.getCacheDir(), DIRECTORY + "-reset-" + System.currentTimeMillis());
                if (!cache.isDirectory() || !cache.renameTo(backup))
                    throw new IllegalStateException("Не удалось сбросить кэш графики.");
            }
            new AlertDialog.Builder(activity).setTitle("Кэш графики сброшен")
                    .setMessage("При следующем запуске будет создан новый кэш. Старый сохранён как резервная копия в личном кэше приложения.")
                    .setPositiveButton("ОК", null).show();
        } catch (RuntimeException error) {
            new AlertDialog.Builder(activity).setTitle("Кэш графики").setMessage(error.getMessage())
                    .setPositiveButton("ОК", null).show();
        }
    }
}
