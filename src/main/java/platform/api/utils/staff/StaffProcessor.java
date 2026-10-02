package platform.api.utils.staff;

import platform.api.system.interfaces.NativeMethodLookup;
import platform.client.utils.lib.json.JSONArray;
import platform.client.utils.lib.json.JSONObject;
import platform.api.system.configs.ConfigProcessor;
import platform.api.utils.staff.StaffConstructor;

import platform.api.annotation.Compile;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class StaffProcessor extends ConfigProcessor<StaffConstructor> {
    @Override
    @Compile
    public List<StaffConstructor> a(String json) throws Exception {
        JSONArray jSONArray = new JSONArray(json);
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < jSONArray.a(); i++) {
            arrayList.add(new StaffConstructor(jSONArray.j(i).l("name")));
        }
        return arrayList;
    }

    @Override
    @Compile
    public String a(List<StaffConstructor> data) throws Exception {
        JSONArray jSONArray = new JSONArray();
        for (StaffConstructor staffConstructor : data) {
            JSONObject jSONObject = new JSONObject();
            jSONObject.c("name", staffConstructor.a());
            jSONArray.a(jSONObject);
        }
        return jSONArray.E(2);
    }

    static {
        NativeMethodLookup.lookup(StaffProcessor.class, 37);
    }

    public String b() {
        return "staff.json";
    }

    public void b(String str) {
        if (!d(str)) {
            this.d.add(new StaffConstructor(str));
        }
    }

    public void c(String str) {
        this.d.removeIf(staff -> staff.a().equalsIgnoreCase(str));
    }

    public boolean d(String name) {
        return this.d.stream().anyMatch(staff -> {
            return staff.a().equalsIgnoreCase(name);
        });
    }

    public void f() {
        this.d.clear();
    }
}



