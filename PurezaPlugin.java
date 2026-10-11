package com.pureza.app;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/** Puente JS <-> nativo: guarda los datos donde el widget también los lee. */
@CapacitorPlugin(name = "Pureza")
public class PurezaPlugin extends Plugin {

    @PluginMethod
    public void get(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("db", PurezaWidget.loadDb(getContext()));
        call.resolve(ret);
    }

    @PluginMethod
    public void set(PluginCall call) {
        String db = call.getString("db", "");
        PurezaWidget.saveDb(getContext(), db);
        PurezaWidget.updateAll(getContext());
        call.resolve();
    }
}
