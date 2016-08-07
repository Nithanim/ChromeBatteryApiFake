package me.nithanim.chromebatteryapifake;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class XposedHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.chrome") && !lpparam.packageName.equals("com.chrome.dev")) {
            return;
        }
        log(Log.INFO, "Hooking " + lpparam.packageName + "...");

        try {
            findAndHookMethod(
                    "org.chromium.device.battery.BatteryMonitorImpl",
                    lpparam.classLoader,
                    "didChange", "org.chromium.mojom.device.BatteryStatus",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Object batteryStatus = param.args[0];

                            //log(Log.DEBUG, "-----\nBefore rewrite:");
                            //printBatteryStatus(batteryStatus);

                            BatteryStatus bs = getBatteryStatusFromPrefs();
                            if (bs != null) {
                                applyBatteryValues(batteryStatus, bs);
                                //log(Log.DEBUG, "After rewrite:");
                                //printBatteryStatus(batteryStatus);
                            } else {
                                //log(Log.DEBUG, "No override specified, passing real battery level!");
                            }
                        }
                    });

            log(Log.INFO, "Hooking successful!");
        } catch (Exception ex) {
            log(Log.ERROR, "Unable to hook: ");
            log(Log.ERROR, ex);
        }
    }

    private void applyBatteryValues(Object original, BatteryStatus fake) throws Exception {
        Class<?> c = original.getClass();

        c.getField("charging").setBoolean(original, fake.isCharging());
        c.getField("chargingTime").setDouble(original, fake.getChargingTime());
        c.getField("dischargingTime").setDouble(original, fake.getDischargingTime());
        c.getField("level").setDouble(original, fake.getLevel());
    }

    private static void printBatteryStatus(Object batteryStatus) {
        printField(batteryStatus, "charging");
        printField(batteryStatus, "chargingTime");
        printField(batteryStatus, "dischargingTime");
        printField(batteryStatus, "level");
    }

    private static void printField(Object o, String name) {
        try {
            log(Log.DEBUG, name + ": " + o.getClass().getField(name).get(o));
        } catch (Exception ex) {
            log(Log.WARN, "Error printing field " + name + " of " + o.getClass().getName());
        }
    }

    private static BatteryStatus getBatteryStatusFromPrefs() {
        XSharedPreferences pref = new XSharedPreferences("me.nithanim.chromebatteryapifake", "pref");
        pref.makeWorldReadable();
        pref.reload();

        if (pref.getBoolean("pref_enabled", false)) {
            boolean charging = pref.getBoolean("pref_charging", false);

            double chargingTime = getFloatFromPref(pref, "pref_chargingTime", Float.POSITIVE_INFINITY);
            if (chargingTime < 0) {
                chargingTime = Float.POSITIVE_INFINITY;
            }

            double dischargingTime = getFloatFromPref(pref, "pref_dischargingTime", Float.POSITIVE_INFINITY);
            if (dischargingTime < 0) {
                dischargingTime = Float.POSITIVE_INFINITY;
            }

            double level = getFloatFromPref(pref, "pref_level", 50);
            level /= 100;

            return new BatteryStatus(charging, chargingTime, dischargingTime, level);
        } else {
            return null;
        }
    }

    private static float getFloatFromPref(XSharedPreferences pref, String key, float def) {
        String raw = pref.getString(key, null);
        if (raw == null) {
            return def;
        } else {
            return Float.parseFloat(raw);
        }
    }

    private static void log(int level, Object msg) {
        String prefix = "ChromeBatteryApiFake: ";

        if (msg instanceof Exception) {
            XposedBridge.log(prefix + "logged:");
            XposedBridge.log((Exception) msg);
            Log.e(XposedHook.class.getName(), "logged", (Exception) msg);
        } else {
            String s = prefix + msg;
            XposedBridge.log(s);
            Log.println(level, XposedHook.class.getName(), s);
        }
    }
}
