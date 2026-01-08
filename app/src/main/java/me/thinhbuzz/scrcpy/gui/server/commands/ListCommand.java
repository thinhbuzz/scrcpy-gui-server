package me.thinhbuzz.scrcpy.gui.server.commands;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Looper;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;

import me.thinhbuzz.scrcpy.gui.server.CliArgs;
import me.thinhbuzz.scrcpy.gui.server.Ln;

public class ListCommand {
    public ListCommand(CliArgs cliArgs) {
        this.cliArgs = cliArgs;
    }

    private final CliArgs cliArgs;

    @SuppressLint("QueryPermissionsNeeded")
    public void run() {
        if (Looper.myLooper() == null) {
            Looper.prepareMainLooper();
        }

        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");

            PrintStream errPrintStream = System.err;
            System.setErr(new PrintStream(new FileOutputStream("/dev/null")));

            Object activityThread = activityThreadClass
                    .getMethod("systemMain")
                    .invoke(null);
            Context context = (Context) activityThreadClass
                    .getMethod("getSystemContext")
                    .invoke(activityThread);
            System.setErr(errPrintStream);
            assert context != null;
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> packages;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packages = pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0));
            } else {
                packages = pm.getInstalledPackages(0);
            }

            JSONArray jsonArray = new JSONArray();
            boolean onlySystemApps = Objects.equals(cliArgs.get("list-type"), "system");
            for (PackageInfo packageInfo : packages) {
                if (packageInfo.packageName.equals(context.getPackageName())) {
                    continue;
                }

                assert packageInfo.applicationInfo != null;
                boolean isSystemApp = (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                if (onlySystemApps != isSystemApp) {
                    continue;
                }
                String appName = packageInfo.applicationInfo.loadLabel(pm).toString();
                String base64Icon = getAppIconAsBase64(packageInfo, pm);
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("name", appName);
                jsonObject.put("packageName", packageInfo.packageName);
                jsonObject.put("isSystemApp", isSystemApp);

                jsonObject.put("base64Icon", base64Icon);

                jsonArray.put(jsonObject);
            }
            Ln.i("ListCommand successfully: " + jsonArray);
        } catch (Exception | Error e) {
            Ln.e("ListCommand failed", e);
        }
    }

    private static String getAppIconAsBase64(PackageInfo packageInfo, PackageManager pm) {
        try {
            Drawable drawable = pm.getApplicationIcon(packageInfo.applicationInfo);
            Bitmap bitmap = toBitmap(drawable, 32, 32);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP);
        } catch (Exception | Error e) {
            return "";
        }
    }

    private static Bitmap toBitmap(Drawable drawable, Integer width, Integer height) {
        if (drawable instanceof BitmapDrawable) {
            return Bitmap.createScaledBitmap(((BitmapDrawable) drawable).getBitmap(), width, height, true);
        }

        Rect bounds = drawable.getBounds();
        int oldLeft = bounds.left;
        int oldTop = bounds.top;
        int oldRight = bounds.right;
        int oldBottom = bounds.bottom;


        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(new Canvas(bitmap));

        drawable.setBounds(oldLeft, oldTop, oldRight, oldBottom);
        return bitmap;
    }
}
