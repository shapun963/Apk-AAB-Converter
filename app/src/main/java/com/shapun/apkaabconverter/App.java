package com.shapun.apkaabconverter;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.shapun.apkaabconverter.activity.DebugActivity;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.Security;

public class App extends Application {
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
    @SuppressLint("StaticFieldLeak")
    public static Context context;

    @Override
    public void onCreate() {
        context = this;
        try {
            File dir = getExternalFilesDir(null);
            Runtime.getRuntime().exec("logcat -f " + dir.getAbsolutePath() + "/log.txt");
        } catch (IOException ignored) {
        }
        this.uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            Intent intent = new Intent(getApplicationContext(), DebugActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("error", getStackTrace(ex));
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 11111, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, pendingIntent);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(2);
            uncaughtExceptionHandler.uncaughtException(thread, ex);
        });

        super.onCreate();
        addProviders();
    }

    private void addProviders() {
        try {
            Security.removeProvider("BC"); //must remove the old bc provider
        } catch (Exception ignored) {
        }

        try {
            // insert the new bc provider at first
            Security.insertProviderAt(new BouncyCastleProvider(), 1);
        } catch (Exception ignored) {
        }
    }

    private String getStackTrace(Throwable th) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        Throwable cause = th;
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        final String stacktraceAsString = result.toString();
        printWriter.close();
        return stacktraceAsString;
    }
}
