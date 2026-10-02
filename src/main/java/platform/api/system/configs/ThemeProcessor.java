package platform.api.system.configs;

import platform.api.system.interfaces.NativeMethodLookup;
import platform.client.utils.lib.json.JSONObject;
import platform.api.system.configs.ConfigProcessor;
import platform.api.system.configs.ThemeConstructor;
import platform.api.system.configs.ThemeInfo;
import platform.api.system.configs.ThemeType;

import platform.api.annotation.Compile;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;

public class ThemeProcessor extends ConfigProcessor<ThemeConstructor> {
    private ThemeType f = ThemeType.DARK;

    @Override
    @Compile
    protected List<ThemeConstructor> a(String json) throws Exception {
        if (json == null || json.isBlank() || json.trim().startsWith("[")) {
            return createDefaultThemes();
        }
        JSONObject jSONObject = new JSONObject(json);
        this.f = ThemeType.valueOf(jSONObject.a("type", ThemeType.DARK.name()));
        List<ThemeConstructor> listE = e();
        if (listE == null) {
            throw new NullPointerException();
        }
        listE.clear();
        ThemeInfo[] themeInfoArrValues = ThemeInfo.values();
        if (themeInfoArrValues == null) {
            throw new NullPointerException();
        }
        for (ThemeInfo themeInfo : themeInfoArrValues) {
            if (themeInfo == null) {
                throw new NullPointerException();
            }
            ThemeConstructor themeConstructorA = themeInfo.a(this.f);
            List<ThemeConstructor> listE2 = e();
            if (themeConstructorA == null) {
                throw new NullPointerException();
            }
            ThemeConstructor themeConstructor = new ThemeConstructor(themeConstructorA.c(), themeConstructorA.d(), themeConstructorA.e(), themeConstructorA.f(), themeConstructorA.g());
            if (listE2 == null) {
                throw new NullPointerException();
            }
            listE2.add(themeConstructor);
        }
        if (!jSONObject.m("primary")) {
            return null;
        }
        ThemeConstructor themeConstructorA2 = a(ThemeInfo.PRIMARY);
        int iH = jSONObject.h("primary");
        if (themeConstructorA2 == null) {
            throw new NullPointerException();
        }
        themeConstructorA2.a(iH);
        return null;
    }

    @Override
    @Compile
    protected String a(List<ThemeConstructor> data) throws Exception {
        JSONObject jSONObject = new JSONObject();
        jSONObject.c("type", this.f.name());
        jSONObject.b("primary", a(ThemeInfo.PRIMARY).a());
        return jSONObject.a(2);
    }

    static {
        NativeMethodLookup.lookup(ThemeProcessor.class, 38);
    }

    @Generated
    public ThemeType a() {
        return this.f;
    }

    public void a(ThemeType type) {
        if (this.f == type) {
            return;
        }
        int primary = a(ThemeInfo.PRIMARY).a();
        this.f = type;
        List<ThemeConstructor> list = e();
        list.clear();
        for (ThemeInfo themeInfo : ThemeInfo.values()) {
            ThemeConstructor c = themeInfo.a(this.f);
            list.add(new ThemeConstructor(c.c(), c.d(), c.e(), c.f(), c.g()));
        }
        a(ThemeInfo.PRIMARY).a(primary);
    }

    @Override
    protected String b() {
        return "theme.json";
    }

    public ThemeConstructor a(ThemeInfo type) {
        return (ThemeConstructor) this.d.stream().filter(constructor -> {
            return constructor.c().equalsIgnoreCase(type.a().c());
        }).findFirst().orElse(type.a(this.f));
    }

    private List<ThemeConstructor> createDefaultThemes() {
        List<ThemeConstructor> themes = new ArrayList<>();
        for (ThemeInfo themeInfo : ThemeInfo.values()) {
            ThemeConstructor defaults = themeInfo.a(this.f);
            themes.add(new ThemeConstructor(defaults.c(), defaults.d(), defaults.e(), defaults.f(), defaults.g()));
        }
        return themes;
    }
}



