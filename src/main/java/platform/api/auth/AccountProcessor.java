package platform.api.auth;

import platform.client.utils.lib.json.JSONArray;
import platform.client.utils.lib.json.JSONObject;
import platform.api.system.configs.ConfigProcessor;
import java.util.ArrayList;
import java.util.List;

public class AccountProcessor extends ConfigProcessor<AccountConstructor> {
    private AccountConstructor selected;

    @Override
    protected String b() {
        return "accounts.json";
    }

    @Override
    protected List<AccountConstructor> a(String json) throws Exception {
        JSONArray jSONArray = new JSONArray(json);
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < jSONArray.a(); i++) {
            JSONObject jSONObject = jSONArray.j(i);
            String name = jSONObject.l("name");
            if (name == null || name.isEmpty()) {
                continue;
            }
            arrayList.add(new AccountConstructor(name, jSONObject.b("selected"), jSONObject.b("favorited")));
        }
        return arrayList;
    }

    @Override
    protected String a(List<AccountConstructor> data) throws Exception {
        JSONArray jSONArray = new JSONArray();
        for (AccountConstructor accountConstructor : data) {
            JSONObject jSONObject = new JSONObject();
            jSONObject.c("name", accountConstructor.b());
            jSONObject.b("selected", accountConstructor.c());
            jSONObject.b("favorited", accountConstructor.d());
            jSONArray.a(jSONObject);
        }
        return jSONArray.E(2);
    }

    @Override
    public void setup() {
        super.setup();
        this.selected = this.d.stream().filter(AccountConstructor::c).findFirst()
                .orElse(this.d.isEmpty() ? null : this.d.get(0));
        if (this.selected != null) {
            this.a(this.selected);
        }
    }

    @Override
    public void unSetup() {
        if (this.selected != null && this.d.contains(this.selected)) {
            this.a(this.selected);
        }
        super.unSetup();
    }

    public List<AccountConstructor> e() {
        return this.d;
    }

    public AccountConstructor a() {
        return this.selected;
    }

    public void a(AccountConstructor account) {
        this.d.forEach(other -> other.a(other == account));
        this.selected = account;
    }
}
