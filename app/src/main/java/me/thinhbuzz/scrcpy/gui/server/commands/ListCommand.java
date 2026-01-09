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

import me.thinhbuzz.scrcpy.gui.server.CliArgs;
import me.thinhbuzz.scrcpy.gui.server.Ln;

public class ListCommand {
    public ListCommand(CliArgs cliArgs) {
        this.cliArgs = cliArgs;
    }

    private final CliArgs cliArgs;

    @SuppressLint("QueryPermissionsNeeded")
    public void run() {
        if (cliArgs.get("list").equals("app")) {
            handleListApps();
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    public void handleListApps() {
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
            int flags = PackageManager.MATCH_UNINSTALLED_PACKAGES;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packages = pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(flags));
            } else {
                packages = pm.getInstalledPackages(flags);
            }

            JSONArray jsonArray = new JSONArray();
            String listType = cliArgs.get("list-type", "all"); // all, system, user
            for (PackageInfo packageInfo : packages) {
                if (packageInfo.applicationInfo == null) {
                    continue;
                }
                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                boolean isSystemApp = isSystemPackage(applicationInfo);
                if ((listType.equals("system")) && !isSystemApp || (listType.equals("user") && isSystemApp)) {
                    continue;
                }
                String appName = applicationInfo.loadLabel(pm).toString();
                boolean isInstalledForUser = (applicationInfo.flags & ApplicationInfo.FLAG_INSTALLED) != 0;
                String base64Icon = getAppIconAsBase64(applicationInfo, pm);
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("name", appName);
                jsonObject.put("packageName", packageInfo.packageName);
                jsonObject.put("versionName", packageInfo.versionName);
                jsonObject.put("versionCode", packageInfo.versionCode);
                jsonObject.put("isDisabled", !applicationInfo.enabled);
                jsonObject.put("isSystemApp", isSystemApp);
                jsonObject.put("isInstalledForUser", isInstalledForUser);
                jsonObject.put("base64Icon", base64Icon);

                jsonArray.put(jsonObject);
            }
            Ln.i("ListCommand successfully: " + jsonArray);
        } catch (Exception | Error e) {
            Ln.e("ListCommand failed", e);
        }
    }

    private static boolean isSystemPackage(ApplicationInfo applicationInfo) {
        boolean isSystem = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        boolean isUpdatedSystem = (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
        return isSystem || isUpdatedSystem;
    }

    private static String getAppIconAsBase64(ApplicationInfo applicationInfo, PackageManager pm) {
        try {
            Drawable drawable = pm.getApplicationIcon(applicationInfo);
            Bitmap bitmap = toBitmap(drawable);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP);
        } catch (Exception | Error e) {
            return "";
        }
    }

    private static Bitmap toBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return Bitmap.createScaledBitmap(((BitmapDrawable) drawable).getBitmap(), 32, 32, true);
        }

        Rect bounds = drawable.getBounds();
        int oldLeft = bounds.left;
        int oldTop = bounds.top;
        int oldRight = bounds.right;
        int oldBottom = bounds.bottom;


        Bitmap bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
        drawable.setBounds(0, 0, 32, 32);
        drawable.draw(new Canvas(bitmap));

        drawable.setBounds(oldLeft, oldTop, oldRight, oldBottom);
        return bitmap;
    }
}
