package platform.api.utils.macros;

import platform.client.utils.input.KeyUtil;
import platform.api.system.interfaces.NativeMethodLookup;
import platform.api.module.Interface;

import platform.client.utils.lib.json.JSONArray;
import platform.client.utils.lib.json.JSONObject;
import static platform.api.module.Interface.aM_;
import platform.client.Delta;

import platform.api.system.configs.ConfigProcessor;
import platform.api.event.interfaces.EventTarget;
import platform.api.event.events.player.KeyEvent;
import platform.api.utils.macros.MacrosConstructor;

import platform.api.annotation.Compile;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class MacrosProcessor extends ConfigProcessor<MacrosConstructor> {
    @Override
    @Compile
    public List<MacrosConstructor> a(String json) throws Exception {
        JSONArray jSONArray = new JSONArray(json);
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < jSONArray.a(); i++) {
            JSONObject jSONObjectJ = jSONArray.j(i);
            arrayList.add(new MacrosConstructor(jSONObjectJ.l("key"), jSONObjectJ.l("command")));
        }
        return arrayList;
    }

    @Override
    @Compile
    public String a(List<MacrosConstructor> data) throws Exception {
        JSONArray jSONArray = new JSONArray();
        for (MacrosConstructor macrosConstructor : data) {
            JSONObject jSONObject = new JSONObject();
            if (!(macrosConstructor instanceof MacrosConstructor)) {
                throw new ClassCastException();
            }
            MacrosConstructor macrosConstructor2 = macrosConstructor;
            jSONObject.c("key", macrosConstructor2.a());
            jSONObject.c("command", macrosConstructor2.b());
            jSONArray.a(jSONObject);
        }
        return jSONArray.E(2);
    }

    static {
        NativeMethodLookup.lookup(MacrosProcessor.class, 32);
    }

    public String b() {
        return "macros.json";
    }

    @EventTarget
    public void a(KeyEvent event) {
        if (event.d() == 1 && aM_.gui.screen() == null) {
            for (MacrosConstructor constructor : Delta.h().d().d().e()) {
                if (KeyUtil.a(event.b()) == KeyUtil.a(constructor.a())) {
                    aM_.player.connection.sendChat(constructor.b());
                }
            }
        }
    }

    public void a(String str, String str2) {
        this.d.add(new MacrosConstructor(str, str2));
    }

    public void b(String str) {
        this.d.removeIf(macro -> macro.a().equals(str));
    }

    public void f() {
        this.d.clear();
    }
}



