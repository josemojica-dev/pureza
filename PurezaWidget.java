package com.pureza.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PurezaWidget extends AppWidgetProvider {

    static final String PREFS = "pureza";
    static final String KEY = "db";
    static final String ACTION_CLEAN = "com.pureza.app.ACTION_CLEAN";

    static String loadDb(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "");
    }

    static void saveDb(Context c, String db) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, db).apply();
    }

    /** Misma lógica que la app: días consecutivos sin recaída. */
    static int calcRacha(JSONObject db) {
        try {
            JSONObject dias = db.optJSONObject("dias");
            if (dias == null || dias.length() == 0) return 0;
            List<String> ks = new ArrayList<>();
            Iterator<String> it = dias.keys();
            while (it.hasNext()) ks.add(it.next());
            Collections.sort(ks);
            LocalDate hoy = LocalDate.now();
            String hoyK = hoy.toString();
            if ("fail".equals(dias.optString(hoyK))) return 0;
            String lastFail = null;
            for (String k : ks) if ("fail".equals(dias.optString(k))) lastFail = k;
            LocalDate start = lastFail != null ? LocalDate.parse(lastFail).plusDays(1) : LocalDate.parse(ks.get(0));
            LocalDate end = "clean".equals(dias.optString(hoyK)) ? hoy : hoy.minusDays(1);
            return (int) Math.max(0, ChronoUnit.DAYS.between(start, end) + 1);
        } catch (Exception e) {
            return 0;
        }
    }

    static boolean hoyLimpio(JSONObject db) {
        JSONObject dias = db.optJSONObject("dias");
        return dias != null && "clean".equals(dias.optString(LocalDate.now().toString()));
    }

    static JSONObject parse(Context c) {
        try {
            String s = loadDb(c);
            if (s != null && !s.isEmpty()) return new JSONObject(s);
        } catch (Exception ignored) { }
        try {
            return new JSONObject("{\"dias\":{},\"mejorRacha\":0,\"ultimaRecaida\":null}");
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    static void updateAll(Context c) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(c);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(c, PurezaWidget.class));
        for (int id : ids) render(c, mgr, id);
    }

    static void render(Context c, AppWidgetManager mgr, int id) {
        JSONObject db = parse(c);
        int racha = calcRacha(db);
        boolean limpio = hoyLimpio(db);
        boolean fail = false;
        JSONObject dias = db.optJSONObject("dias");
        if (dias != null) fail = "fail".equals(dias.optString(LocalDate.now().toString()));

        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget_pureza);
        v.setTextViewText(R.id.w_num, String.valueOf(racha));
        v.setTextViewText(R.id.w_label, racha == 1 ? "DÍA LIMPIO" : "DÍAS LIMPIO");
        v.setTextViewText(R.id.w_btn, fail ? "Mañana es nuevo" : (limpio ? "Hoy limpio ✓" : "Marcar hoy limpio"));

        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        Intent open = new Intent(c, MainActivity.class);
        v.setOnClickPendingIntent(R.id.w_root, PendingIntent.getActivity(c, 0, open, flags));

        Intent clean = new Intent(c, PurezaWidget.class).setAction(ACTION_CLEAN);
        v.setOnClickPendingIntent(R.id.w_btn, PendingIntent.getBroadcast(c, 1, clean, flags));

        mgr.updateAppWidget(id, v);
    }

    @Override
    public void onUpdate(Context c, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) render(c, mgr, id);
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        if (ACTION_CLEAN.equals(intent.getAction())) {
            try {
                JSONObject db = parse(c);
                JSONObject dias = db.optJSONObject("dias");
                if (dias == null) { dias = new JSONObject(); db.put("dias", dias); }
                String hoy = LocalDate.now().toString();
                if (!"fail".equals(dias.optString(hoy))) {
                    dias.put(hoy, "clean");
                    int r = calcRacha(db);
                    if (r > db.optInt("mejorRacha", 0)) db.put("mejorRacha", r);
                    saveDb(c, db.toString());
                }
            } catch (Exception ignored) { }
            updateAll(c);
            return;
        }
        super.onReceive(c, intent);
    }
}
