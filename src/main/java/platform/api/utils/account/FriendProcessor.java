package platform.api.utils.account;

import platform.api.system.interfaces.NativeMethodLookup;
import platform.client.utils.lib.json.JSONArray;
import platform.client.utils.lib.json.JSONObject;
import platform.api.system.configs.ConfigProcessor;
import platform.api.utils.account.FriendConstructor;

import platform.api.annotation.Compile;
import java.util.ArrayList;
import java.util.List;

public class FriendProcessor extends ConfigProcessor<FriendConstructor> {
    @Override
    @Compile
    protected List<FriendConstructor> a(String json) throws Exception {
        JSONArray jSONArray = new JSONArray(json);
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < jSONArray.a(); i++) {
            arrayList.add(new FriendConstructor(jSONArray.j(i).l("name")));
        }
        return arrayList;
    }

    @Override
    @Compile
    protected String a(List<FriendConstructor> data) throws Exception {
        JSONArray jSONArray = new JSONArray();
        for (FriendConstructor friendConstructor : data) {
            JSONObject jSONObject = new JSONObject();
            jSONObject.c("name", friendConstructor.a());
            jSONArray.a(jSONObject);
        }
        return jSONArray.E(2);
    }

    static {
        NativeMethodLookup.lookup(FriendProcessor.class, 24);
    }

    @Override
    protected String b() {
        return "friends.json";
    }

    public List<FriendConstructor> a() {
        return new ArrayList(this.d);
    }

    public void b(String str) {
        if (!d(str)) {
            this.d.add(new FriendConstructor(str));
        }
    }

    public void c(String str) {
        this.d.removeIf(friend -> friend.a().equalsIgnoreCase(str));
    }

    public boolean d(String name) {
        return this.d.stream().anyMatch(friend -> {
            return friend.a().equalsIgnoreCase(name);
        });
    }

    public void f() {
        this.d.clear();
    }
}


