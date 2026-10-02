package platform.client.services;

import platform.api.system.configs.BaseProcessor;
import platform.api.system.interfaces.NativeMethodLookup;
import platform.client.Delta;
import platform.client.utils.lib.log4j.LoggerFactory;

import platform.api.module.Interface;

import platform.api.utils.auction.BatchProcessor;
import platform.api.utils.auction.AutoBuyProcessor;
import platform.api.utils.auction.CollectorProcessor;
import platform.api.command.CommandProcessor;
import platform.client.utils.bridge.discord.DiscordProcessor;
import platform.client.ui.element.DragProcessor;
import platform.client.utils.render.Draw2DProcessor;
import platform.client.utils.render.Draw3DProcessor;
import platform.client.utils.lib.log4j.Logger_2;
import platform.api.utils.macros.MacrosProcessor;
import platform.api.system.configs.ModuleProcessor;
import platform.api.utils.notification.NotificationProcessor;
import platform.api.handlers.HandlerProcessor;
import platform.api.handlers.RotationProcessor;
import platform.api.utils.account.FriendProcessor;
import platform.api.utils.staff.StaffProcessor;
import platform.api.system.configs.ThemeProcessor;
import platform.api.auth.AccountProcessor;
import platform.client.processors.FlaskProcessor;
import platform.api.annotation.Compile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Generated;

public class Processor_2 implements Interface {

    @Generated
    private static final Logger_2 b;
    private final List<BaseProcessor> c;
    private final MacrosProcessor d;
    private final FriendProcessor e;
    private final StaffProcessor f;
    private final DiscordProcessor g;
    private final Draw2DProcessor i;
    private final Draw3DProcessor j;
    private final RotationProcessor k;
    private final BatchProcessor l;
    private final NotificationProcessor m;
    private final ThemeProcessor o;
    private final DragProcessor s;
    private final ModuleProcessor t;
    private final HandlerProcessor v;
    private final AccountProcessor h;
    private final CollectorProcessor p;
    private final AutoBuyProcessor q;
    private final CommandProcessor u;
    private final FlaskProcessor w;

    @Compile
    public void a() {
        Collections.addAll(this.c, this.d, this.p, this.q, this.e, this.f, this.o, this.g, this.t, this.k, this.m, this.i, this.s, this.j, this.l, this.v, this.h, this.u, this.w);
        this.c.forEach(new Consumer() {
            @Override
            public void accept(Object obj) {
                ((BaseProcessor) obj).setup();
            }
        });
        System.out.println("setup - ".concat(String.valueOf(this.c.stream().map(new Function() {
            @Override
            public Object apply(Object obj) {
                return ((BaseProcessor) obj).getClass().getSimpleName();
            }
        }).toList())));
    }

    static {
        NativeMethodLookup.lookup(Processor_2.class, 18);
        b = LoggerFactory.a((Class<?>) Processor_2.class);
    }

    public Processor_2() {
        Delta.h().a(this);
        this.c = new ArrayList();
        this.d = new MacrosProcessor();
        this.e = new FriendProcessor();
        this.f = new StaffProcessor();
        this.g = new DiscordProcessor();
        this.i = new Draw2DProcessor();
        this.j = new Draw3DProcessor();
        this.k = new RotationProcessor();
        this.l = new BatchProcessor();
        this.m = new NotificationProcessor();
        this.o = new ThemeProcessor();
        this.s = new DragProcessor();
        this.t = new ModuleProcessor();
        this.v = new HandlerProcessor();
        this.h = new AccountProcessor();
        this.p = new CollectorProcessor();
        this.q = new AutoBuyProcessor();
        this.u = new CommandProcessor();
        this.w = new FlaskProcessor();
    }

    @Generated
    public List<BaseProcessor> c() {
        return this.c;
    }

    @Generated
    public MacrosProcessor d() {
        return this.d;
    }

    @Generated
    public FriendProcessor e() {
        return this.e;
    }

    @Generated
    public StaffProcessor f() {
        return this.f;
    }

    @Generated
    public DiscordProcessor g() {
        return this.g;
    }

    @Generated
    public Draw2DProcessor i() {
        return this.i;
    }

    @Generated
    public Draw3DProcessor j() {
        return this.j;
    }

    @Generated
    public RotationProcessor k() {
        return this.k;
    }

    @Generated
    public BatchProcessor l() {
        return this.l;
    }

    @Generated
    public NotificationProcessor m() {
        return this.m;
    }

    @Generated
    public ThemeProcessor o() {
        return this.o;
    }

    @Generated
    public DragProcessor s() {
        return this.s;
    }

    @Generated
    public ModuleProcessor t() {
        return this.t;
    }

    @Generated
    public HandlerProcessor v() {
        return this.v;
    }

    @Generated
    public AccountProcessor h() {
        return this.h;
    }

    @Generated
    public CollectorProcessor p() {
        return this.p;
    }

    @Generated
    public AutoBuyProcessor q() {
        return this.q;
    }

    @Generated
    public CommandProcessor u() {
        return this.u;
    }

    public FlaskProcessor flaskProcessor() {
        return this.w;
    }

    public void b() {
        this.c.forEach(processor -> {
            try {
                processor.unSetup();
            } catch (Throwable th) {
            }
        });
        System.out.println("unSetup - " + String.valueOf(this.c.stream().map(processor2 -> {
            return processor2.getClass().getSimpleName();
        }).toList()));
    }
}



