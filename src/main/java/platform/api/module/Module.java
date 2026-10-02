package platform.api.module;

import platform.client.Delta;
import platform.api.event.EventManager;
import platform.api.module.InterfaceC0020Opcode;
import platform.client.utils.render.ColorUtil;

import platform.api.module.Category;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.utils.notification.Notification;
import platform.client.ui.element.Element_2;

import platform.client.utils.render.AnimationUtil;
import platform.api.module.setting.Setting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;

public class Module implements Interface {
    private boolean k;
    private boolean l;
    private boolean m;
    private final List<Element_2<?>> b = new ArrayList();
    private final List<Setting<?>> c = new ObjectArrayList();
    private final AnimationUtil d = new AnimationUtil();
    private final AnimationUtil e = new AnimationUtil();
    private final AnimationUtil f = new AnimationUtil();
    private final AnimationUtil g = new AnimationUtil();
    private final String h = ((ModuleRegister) getClass().getAnnotation(ModuleRegister.class)).a();
    private final String i = ((ModuleRegister) getClass().getAnnotation(ModuleRegister.class)).b();
    private final Category j = ((ModuleRegister) getClass().getAnnotation(ModuleRegister.class)).c();
    private int n = -1;

    @Generated
    public void b(boolean bind) {
        this.l = bind;
    }

    @Generated
    public void c(boolean extended) {
        this.m = extended;
    }

    @Generated
    public void a(int key) {
        this.n = key;
        Delta.h().d().t().requestAutoSave();
    }

    @Generated
    public List<Element_2<?>> d() {
        return this.b;
    }

    @Generated
    public List<Setting<?>> e() {
        return this.c;
    }

    @Generated
    public AnimationUtil f() {
        return this.d;
    }

    @Generated
    public AnimationUtil g() {
        return this.e;
    }

    @Generated
    public AnimationUtil h() {
        return this.f;
    }

    @Generated
    public AnimationUtil i() {
        return this.g;
    }

    @Generated
    public String j() {
        return this.h;
    }

    @Generated
    public String k() {
        return this.i;
    }

    @Generated
    public Category l() {
        return this.j;
    }

    @Generated
    public boolean m() {
        return this.k;
    }

    @Generated
    public boolean n() {
        return this.l;
    }

    @Generated
    public boolean o() {
        return this.m;
    }

    @Generated
    public int p() {
        return this.n;
    }

    public final void a() {
        a(!this.k);
    }

    public final void a(boolean newState) {
        if (this.k == newState) {
            return;
        }
        this.k = newState;
        if (this.k) {
            b();
        } else {
            c();
        }
        Delta.h().d().t().at().d(this.k);
        Delta.h().d().t().requestAutoSave();
    }

    public final void a(Setting<?>... settings) {
        for (Setting<?> setting : settings) {
            this.c.add(setting);
            Element_2<?> element = setting.d();
            if (element != null) {
                this.b.add(element);
            }
        }
    }

    public void b() {
        EventManager.a(this);
        Delta.h().d().m().a(new Notification("Q", ColorUtil.a(InterfaceC0020Opcode.bW, 220, InterfaceC0020Opcode.bv, 255), j() + " активирован", 1500));
    }

    public void c() {
        EventManager.b(this);
        Delta.h().d().m().a(new Notification("Q", ColorUtil.a(230, InterfaceC0020Opcode.bW, InterfaceC0020Opcode.bW, 255), j() + " деактивирован", 1500));
    }
}



