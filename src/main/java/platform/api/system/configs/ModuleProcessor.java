package platform.api.system.configs;

import platform.api.module.Module;
import platform.client.Delta;
import platform.api.event.interfaces.EventTarget;
import platform.api.event.events.player.KeyEvent;
import platform.client.features.modules.combat.AimAssistant;
import platform.client.features.modules.combat.AntiBot;
import platform.client.features.modules.combat.Aura;
import platform.client.features.modules.combat.AutoArmor;
import platform.client.features.modules.combat.AutoEXP;
import platform.client.features.modules.combat.AutoSwap;
import platform.client.features.modules.combat.AutoExplosion;
import platform.client.features.modules.combat.AutoTotem;
import platform.client.features.modules.combat.HitBoxes;
import platform.client.features.modules.combat.LungeBoost;
import platform.client.features.modules.combat.MaceExploit;
import platform.client.features.modules.combat.MaceHelper;
import platform.client.features.modules.combat.NoFriendDamage;
import platform.client.features.modules.combat.NoServerDesync;
import platform.client.features.modules.combat.NoServerPack;
import platform.client.features.modules.combat.NoSlotChange;
import platform.client.features.modules.combat.ProjectileHelper;
import platform.client.features.modules.combat.ShiftTAP;
import platform.client.features.modules.combat.TapeMouse;
import platform.client.features.modules.combat.TriggerBot;
import platform.client.features.modules.combat.Velocity;
import platform.client.features.modules.movement.AirStuck;
import platform.client.features.modules.movement.AutoDodge;
import platform.client.features.modules.movement.ElytraTarget;
import platform.client.features.modules.movement.FastBreak;
import platform.client.features.modules.movement.Fly;
import platform.client.features.modules.movement.FreeCamera;
import platform.client.features.modules.movement.NoCrouch;
import platform.client.features.modules.movement.NoDelay;
import platform.client.features.modules.movement.NoPush;
import platform.client.features.modules.movement.NoSlowDown;
import platform.client.features.modules.movement.SafeWalk;
import platform.client.features.modules.movement.Scaffold;
import platform.client.features.modules.movement.ScreenWalk;
import platform.client.features.modules.movement.Sprint;
import platform.client.features.modules.movement.Speed;
import platform.client.features.modules.movement.WallClimb;
import platform.client.features.modules.movement.WaterJump;
import platform.client.features.modules.movement.WaterSpeed;
import platform.client.features.modules.movement.WindBoost;
import platform.client.features.modules.misc.AncientFarmer;
import platform.client.features.modules.misc.BaritoneModule;
import platform.client.features.modules.misc.AutoWarden;
import platform.client.features.modules.misc.AutoCart;
import platform.client.features.modules.misc.AntiAFK;
import platform.client.features.modules.misc.AppleFarmer;
import platform.client.features.modules.misc.AutoBuy;
import platform.client.features.modules.misc.ChatHelper;
import platform.client.features.modules.misc.ClanUpgrader;
import platform.client.features.modules.misc.Collector_2;
import platform.client.features.modules.misc.Communication;
import platform.client.features.modules.misc.FunDeliver;
import platform.client.features.modules.misc.MineAssistant;
import platform.client.features.modules.misc.NoCommands;
import platform.client.features.modules.misc.NoInteract;
import platform.client.features.modules.misc.Nuker;
import platform.client.features.modules.misc.PortalBypass;
import platform.client.features.modules.misc.PotionThrower;
import platform.client.features.modules.misc.RPSpoofs;
import platform.client.features.modules.misc.ServerAssistant;
import platform.client.features.modules.misc.ServerJoiner;
import platform.client.features.modules.misc.SkullFix;
import platform.client.features.modules.misc.Sounds;
import platform.client.features.modules.misc.Wasted;
import platform.client.features.modules.misc.XRay;
import platform.client.features.modules.player.AucReissue;
import platform.client.features.modules.player.AutoAccept;
import platform.client.features.modules.player.AutoAuth;
import platform.client.features.modules.player.AutoEat;
import platform.client.features.modules.player.AutoFish;
import platform.client.features.modules.player.AutoLeave;
import platform.client.features.modules.player.AutoRespawn;
import platform.client.features.modules.player.AutoTool;
import platform.client.features.modules.player.CaptchaSolver;
import platform.client.features.modules.player.ChestStealer;
import platform.client.features.modules.player.ClickAction;
import platform.client.features.modules.player.DeathCoords;
import platform.client.features.modules.player.ElytraHelper;
import platform.client.features.modules.player.FakeLags;
import platform.client.features.modules.player.FastEXP;
import platform.client.features.modules.player.FastLoad;
import platform.client.features.modules.player.ItemScroller;
import platform.client.features.modules.player.LockSlot;
import platform.client.features.modules.player.OpenWalls;
import platform.client.features.modules.player.SoundReducer;
import platform.client.features.modules.player.Structures;
import platform.client.features.modules.player.ThirdPerson;
import platform.client.features.modules.player.UseTracker;
import platform.client.features.modules.player.WindHop;
import platform.client.features.modules.misc.StreamerMode;
import platform.client.features.modules.render.Animations;
import platform.client.features.modules.render.AspectRatio;
import platform.client.features.modules.render.BlockESP;
import platform.client.features.modules.render.EntityESP;
import platform.client.features.modules.render.BoardSpoofer;
import platform.client.features.modules.render.Crosshair;
import platform.client.features.modules.render.FullBright;
import platform.client.features.modules.render.HandsShader;
import platform.client.features.modules.render.Interface_2;
import platform.client.features.modules.render.ItemPhysic;
import platform.client.features.modules.render.JumpCircles;
import platform.client.features.modules.render.Arrows;
import platform.client.features.modules.render.Pointers;
import platform.client.features.modules.render.Predictions;
import platform.client.features.modules.render.Removals;
import platform.client.features.modules.render.SeeInvisibles;
import platform.client.features.modules.render.ShaderESP;
import platform.client.features.modules.render.ShaderSky;
import platform.client.features.modules.render.ShulkerPreview;
import platform.client.features.modules.render.SoundESP;
import platform.client.features.modules.render.SwingAnimation;
import platform.client.features.modules.render.ViewModel;
import platform.client.features.modules.render.WardenESP;
import platform.client.features.modules.render.TargetESP;
import platform.client.utils.lib.json.JSONArray;
import platform.client.utils.lib.json.JSONObject;
import platform.api.module.setting.BindSetting;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.ColorSetting;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.MultiModeSetting;
import platform.api.module.setting.Setting;
import platform.api.module.setting.SliderSetting;
import platform.api.module.setting.StringSetting;
import platform.client.ui.element.DragInfo;
import lombok.Generated;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModuleProcessor extends BaseProcessor {
    private final List<Module> modules = new ArrayList<>();
    private final File configDir;
    private String lastConfigName = "default";
    private volatile boolean loadingConfig = false;
    private volatile boolean autoSaveEnabled = false;
    private volatile long autoSaveRequestTime = 0L;
    private Thread autoSaveWorker = null;
    private Interface_2 bd;
    private WindHop aW;
    private WindBoost windBoost;
    private AutoSwap autoSwap;
    private Speed speed;
    private Scaffold aS;
    private SafeWalk ah;
    private Aura B;
    private AutoTotem V;
    private ProjectileHelper D;
    private ElytraHelper F;
    private ElytraTarget G;
    private MaceHelper H;
    private MaceExploit maceExploit;
    private LungeBoost lungeBoost;
    private TriggerBot X;
    private HitBoxes t;
    private TapeMouse u;
    private NoServerPack v;
    private ShiftTAP A;
    private AutoExplosion C;
    private AutoArmor W;
    private AimAssistant Y;
    private AntiBot Z;
    private NoFriendDamage ac;
    private NoServerDesync ae;
    private NoSlotChange af;
    private AutoEXP aP;
    private Velocity bc;
    private WardenESP i;
    private AutoEat aV;
    private ScreenWalk f;
    private FreeCamera h;
    private AutoDodge l;
    private NoCrouch n;
    private Sprint o;
    private NoSlowDown q;
    private NoPush O;
    private WaterJump aq;
    private WaterSpeed waterSpeed;
    private AirStuck av;
    private NoDelay ax;
    private Fly aQ;
    private WallClimb aR;
    private FastBreak aX;
    private SoundESP m;
    private BoardSpoofer x;
    private SwingAnimation R;
    private SeeInvisibles T;
    private BlockESP ab;
    private ShaderESP ad;
    private ItemPhysic ag;
    private Removals ai;
    private ViewModel ao;
    private Crosshair au;
    private ShulkerPreview aw;
    private AspectRatio aB;
    private Predictions aC;
    private StreamerMode aE;
    private Pointers aL;
    private Arrows arrows;
    private FullBright aO;
    private HandsShader aT;
    private ShaderSky shaderSky;
    private Animations Q;
    private JumpCircles jumpCircles;
    private EntityESP entityESP;
    private TargetESP targetESP;
    private AucReissue aucReissue;
    private AutoAccept autoAccept;
    private AutoAuth autoAuth;
    private AutoFish autoFish;
    private AutoLeave autoLeave;
    private AutoRespawn autoRespawn;
    private AutoTool autoTool;
    private CaptchaSolver captchaSolver;
    private ChestStealer chestStealer;
    private ClickAction clickAction;
    private DeathCoords deathCoords;
    private FakeLags fakeLags;
    private FastEXP fastExp;
    private FastLoad fastLoad;
    private ItemScroller itemScroller;
    private LockSlot lockSlot;
    private OpenWalls openWalls;
    private SoundReducer soundReducer;
    private Structures structures;
    private ThirdPerson thirdPerson;
    private UseTracker useTracker;
    private AncientFarmer ancientFarmer;
    private AutoWarden autoWarden;
    private BaritoneModule baritoneModule;
    private AntiAFK antiAFK;
    private AppleFarmer appleFarmer;
    private AutoBuy autoBuy;
    private ChatHelper chatHelper;
    private ClanUpgrader clanUpgrader;
    private Collector_2 collector2;
    private Communication communication;
    private FunDeliver funDeliver;
    private MineAssistant mineAssistant;
    private NoCommands noCommands;
    private NoInteract noInteract;
    private Nuker nuker;
    private PortalBypass portalBypass;
    private PotionThrower potionThrower;
    private RPSpoofs rpSpoofs;
    private ServerAssistant serverAssistant;
    private ServerJoiner serverJoiner;
    private SkullFix skullFix;
    private Sounds sounds;
    private AutoCart autoCart;
    private Wasted wasted;
    private XRay xRay;

    @Override
    public void setup() {
        this.bd = new Interface_2();
        a(this.bd);
        this.aW = new WindHop();
        this.aS = new Scaffold();
        this.ah = new SafeWalk();
        a(this.aW, this.aS, this.ah);
        this.B = new Aura();
        this.V = new AutoTotem();
        this.D = new ProjectileHelper();
        this.F = new ElytraHelper();
        this.G = new ElytraTarget();
        this.H = new MaceHelper();
        this.maceExploit = new MaceExploit();
        this.X = new TriggerBot();
        this.lungeBoost = new LungeBoost();
        this.t = new HitBoxes();
        this.u = new TapeMouse();
        this.v = new NoServerPack();
        this.A = new ShiftTAP();
        this.C = new AutoExplosion();
        this.W = new AutoArmor();
        this.Y = new AimAssistant();
        this.Z = new AntiBot();
        this.ac = new NoFriendDamage();
        this.ae = new NoServerDesync();
        this.af = new NoSlotChange();
        this.aP = new AutoEXP();
        this.bc = new Velocity();
        a(this.B, this.V, this.D, this.F, this.G, this.H, this.maceExploit, this.X, this.lungeBoost, this.t, this.u, this.v, this.A, this.C, this.W, this.Y, this.Z, this.ac, this.ae, this.af, this.aP, this.bc);
        this.i = new WardenESP();
        this.aV = new AutoEat();
        this.aucReissue = new AucReissue();
        this.autoAccept = new AutoAccept();
        this.autoAuth = new AutoAuth();
        this.autoFish = new AutoFish();
        this.autoLeave = new AutoLeave();
        this.autoRespawn = new AutoRespawn();
        this.autoTool = new AutoTool();
        this.captchaSolver = new CaptchaSolver();
        this.chestStealer = new ChestStealer();
        this.clickAction = new ClickAction();
        this.deathCoords = new DeathCoords();
        this.fakeLags = new FakeLags();
        this.fastExp = new FastEXP();
        this.fastLoad = new FastLoad();
        this.itemScroller = new ItemScroller();
        this.lockSlot = new LockSlot();
        this.openWalls = new OpenWalls();
        this.soundReducer = new SoundReducer();
        this.structures = new Structures();
        this.thirdPerson = new ThirdPerson();
        this.useTracker = new UseTracker();
        a(this.i, this.aV, this.aucReissue, this.autoAccept, this.autoAuth, this.autoFish, this.autoLeave, this.autoRespawn, this.autoTool, this.captchaSolver, this.chestStealer, this.clickAction, this.deathCoords, this.fakeLags, this.fastExp, this.fastLoad, this.itemScroller, this.lockSlot, this.openWalls, this.soundReducer, this.structures, this.thirdPerson, this.useTracker);
        this.ancientFarmer = new AncientFarmer();
        this.autoWarden = new AutoWarden();
        this.antiAFK = new AntiAFK();
        this.appleFarmer = new AppleFarmer();
        this.autoBuy = new AutoBuy();
        this.chatHelper = new ChatHelper();
        this.clanUpgrader = new ClanUpgrader();
        this.collector2 = new Collector_2();
        this.communication = new Communication();
        this.funDeliver = new FunDeliver();
        this.mineAssistant = new MineAssistant();
        this.noCommands = new NoCommands();
        this.noInteract = new NoInteract();
        this.nuker = new Nuker();
        this.portalBypass = new PortalBypass();
        this.potionThrower = new PotionThrower();
        this.rpSpoofs = new RPSpoofs();
        this.serverAssistant = new ServerAssistant();
        this.serverJoiner = new ServerJoiner();
        this.skullFix = new SkullFix();
        this.autoCart = new AutoCart();
        this.sounds = new Sounds();
        this.wasted = new Wasted();
        this.xRay = new XRay();
        this.baritoneModule = new BaritoneModule();
        a(this.ancientFarmer, this.autoWarden, this.antiAFK, this.appleFarmer, this.autoBuy, this.baritoneModule, this.chatHelper, this.clanUpgrader, this.collector2, this.communication, this.funDeliver, this.mineAssistant, this.noCommands, this.noInteract, this.nuker, this.portalBypass, this.potionThrower, this.rpSpoofs, this.serverAssistant, this.serverJoiner, this.skullFix, this.autoCart, this.sounds, this.wasted, this.xRay);
        this.f = new ScreenWalk();
        this.h = new FreeCamera();
        this.l = new AutoDodge();
        this.n = new NoCrouch();
        this.o = new Sprint();
        this.q = new NoSlowDown();
        this.O = new NoPush();
        this.aq = new WaterJump();
        this.waterSpeed = new WaterSpeed();
        this.av = new AirStuck();
        this.ax = new NoDelay();
        this.aQ = new Fly();
        this.aR = new WallClimb();
        this.aX = new FastBreak();
        this.windBoost = new WindBoost();
        this.autoSwap = new AutoSwap();
        this.speed = new Speed();
        a(this.f, this.h, this.l, this.n, this.o, this.q, this.O, this.aq, this.waterSpeed, this.av, this.ax, this.aQ, this.aR, this.aX, this.windBoost, this.autoSwap, this.speed);
        this.m = new SoundESP();
        this.x = new BoardSpoofer();
        this.R = new SwingAnimation();
        this.T = new SeeInvisibles();
        this.ab = new BlockESP();
        this.ad = new ShaderESP();
        this.ag = new ItemPhysic();
        this.ai = new Removals();
        this.ao = new ViewModel();
        this.au = new Crosshair();
        this.aw = new ShulkerPreview();
        this.aB = new AspectRatio();
        this.aC = new Predictions();
        this.aE = new StreamerMode();
        this.aL = new Pointers();
        this.arrows = new Arrows();
        this.aO = new FullBright();
        this.aT = new HandsShader();
        this.shaderSky = new ShaderSky();
        this.Q = new Animations();
        this.jumpCircles = new JumpCircles();
        this.entityESP = new EntityESP();
        this.targetESP = new TargetESP();
        a(this.m, this.x, this.R, this.T, this.ab, this.ad, this.ag, this.ai, this.ao, this.au, this.aw, this.aB, this.aC, this.aE, this.aL, this.arrows, this.aO, this.aT, this.shaderSky, this.Q, this.jumpCircles, this.entityESP, this.targetESP);
        spreadDefaultDragPositions();
        this.lastConfigName = readLastConfigName();
        if (this.lastConfigName != null && !this.lastConfigName.isBlank()) {
            c(this.lastConfigName);
        }
        Setting.changeListener = setting -> requestAutoSave();
        this.autoSaveEnabled = true;
        if (!this.bd.m()) {
            this.bd.a(true);
        }
    }

    public synchronized void requestAutoSave() {
        if (!this.autoSaveEnabled || this.loadingConfig || this.lastConfigName == null || this.lastConfigName.isBlank()) {
            return;
        }
        this.autoSaveRequestTime = System.currentTimeMillis();
        if (this.autoSaveWorker == null || !this.autoSaveWorker.isAlive()) {
            Thread worker = new Thread(this::autoSaveLoop, "Delta-ConfigAutoSave");
            worker.setDaemon(true);
            this.autoSaveWorker = worker;
            worker.start();
        }
    }

    private void autoSaveLoop() {
        while (true) {
            long idle = System.currentTimeMillis() - this.autoSaveRequestTime;
            if (idle >= 400L) {
                break;
            }
            try {
                Thread.sleep(400L - idle);
            } catch (InterruptedException e) {
                return;
            }
        }
        String configName = this.lastConfigName;
        if (!this.loadingConfig && configName != null && !configName.isBlank()) {
            try {
                b(configName);
            } catch (Exception ignored) {
            }
        }
    }

    private void spreadDefaultDragPositions() {
        try {
            float width = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth();
            float height = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight();
            if (width <= 0.0f || height <= 0.0f) {
                return;
            }
            List<DragInfo> unplaced = new ArrayList<>();
            for (DragInfo dragInfo : Delta.h().d().s().e()) {
                if (dragInfo.c() == 0.0f && dragInfo.d() == 0.0f) {
                    unplaced.add(dragInfo);
                }
            }
            layoutCentered(unplaced, width, height);
        } catch (Exception ignored) {
        }
    }

    public static void layoutCentered(List<DragInfo> infos, float width, float height) {
        if (infos == null || infos.isEmpty() || width <= 0.0f || height <= 0.0f) {
            return;
        }
        for (DragInfo info : infos) {
            seedDefaultSize(info);
            float x;
            float y;
            switch (info.j()) {
                case "Окружение" -> {
                    x = 5.0f;
                    y = 5.0f;
                }
                case "Задержки" -> {
                    x = width - info.f() - 5.0f;
                    y = 5.0f;
                }
                case "Клавиши" -> {
                    x = width - info.f() - 5.0f;
                    y = 26.0f;
                }
                case "Зелья" -> {
                    x = width * 0.35f;
                    y = 28.0f;
                }
                case "Стафф" -> {
                    x = width * 0.49f;
                    y = 62.0f;
                }
                case "Предметы" -> {
                    x = width - info.f() - 5.0f;
                    y = 48.0f;
                }
                case "Броня" -> {
                    x = (width - info.f()) * 0.5f;
                    y = height - info.g() - 42.0f;
                }
                case "Таргет-худ" -> {
                    x = (width - info.f()) * 0.5f;
                    y = height * 0.79f;
                }
                case "Уведомления" -> {
                    x = (width - info.f()) * 0.5f;
                    y = height * 0.68f;
                }
                case "Инфо-панель" -> {
                    x = (width - info.f()) * 0.5f;
                    y = 5.0f;
                }
                default -> {
                    x = 5.0f;
                    y = 5.0f;
                }
            }
            info.a(Math.max(0.0f, Math.min(x, width - info.f())));
            info.b(Math.max(0.0f, Math.min(y, height - info.g())));
        }
    }

    private static void seedDefaultSize(DragInfo info) {
        if (info.f() > 0.5f && info.g() > 0.5f) {
            return;
        }
        switch (info.j()) {
            case "Инфо-панель" -> {
                info.c(190.0f);
                info.d(28.0f);
            }
            case "Таргет-худ" -> {
                info.c(100.0f);
                info.d(24.0f);
            }
            case "Уведомления" -> {
                info.c(120.0f);
                info.d(14.0f);
            }
            case "Окружение" -> {
                info.c(92.0f);
                info.d(42.0f);
            }
            case "Броня" -> {
                info.c(64.0f);
                info.d(28.0f);
            }
            case "Задержки", "Клавиши" -> {
                info.c(56.0f);
                info.d(26.0f);
            }
            case "Зелья" -> {
                info.c(55.0f);
                info.d(20.0f);
            }
            case "Предметы" -> {
                info.c(60.0f);
                info.d(20.0f);
            }
            case "Стафф" -> {
                info.c(49.0f);
                info.d(20.0f);
            }
            default -> {
                info.c(90.0f);
                info.d(20.0f);
            }
        }
    }

    @Override
    public void unSetup() {
        if (this.lastConfigName != null && !this.lastConfigName.isBlank()) {
            try {
                b(this.lastConfigName);
            } catch (Exception ignored) {
            }
        }
    }

    @EventTarget
    public void a(KeyEvent event) {
        int action = event.d();
        int key = event.b();
        for (Module module : e()) {
            if (module.p() != -1 && module.p() == key && action == 1) {
                module.a();
            }
            if (module.m()) {
                for (Setting<?> setting : module.e()) {
                    if (setting instanceof BindSetting) {
                        BindSetting bind = (BindSetting) setting;
                        if (bind.c().intValue() == key) {
                            if (action == 1) {
                                bind.k().execute();
                            } else if (action == 0 && bind.m() == 0 && bind.l() != null) {
                                bind.l().execute();
                            }
                        }
                    }
                }
            }
        }
    }

    public ModuleProcessor() {
        this.configDir = new File(net.minecraft.client.Minecraft.getInstance().gameDirectory, "delta");
        if (!this.configDir.exists()) {
            this.configDir.mkdirs();
        }
    }

    @Generated
    public Module[] e() {
        return modules.toArray(new Module[0]);
    }

    public void a(Module module) {
        if (module != null) {
            modules.add(module);
        }
    }

    public void a(Module... mods) {
        for (Module m : mods) {
            if (m != null) modules.add(m);
        }
    }

    public Module h() { return this.h; }
    public Module aq() { return this.aq; }
    public Sounds at() { return this.sounds; }
    public AspectRatio aB() { return this.aB; }
    public Animations Q() { return this.Q; }
    public List<Module> d() { return modules; }

    public Aura B() {
        return this.B;
    }

    public AutoTotem V() {
        return this.V;
    }

    public ProjectileHelper D() {
        return this.D;
    }

    public ElytraHelper F() {
        return this.F;
    }

    public ElytraTarget G() {
        return this.G;
    }

    public MaceHelper H() {
        return this.H;
    }

    public MaceExploit maceExploit() {
        return this.maceExploit;
    }

    public TriggerBot X() {
        return this.X;
    }

    public LungeBoost lungeBoost() {
        return this.lungeBoost;
    }

    public HitBoxes t() {
        return this.t;
    }

    public TapeMouse u() {
        return this.u;
    }

    public NoServerPack v() {
        return this.v;
    }

    public ShiftTAP A() {
        return this.A;
    }

    public AutoExplosion C() {
        return this.C;
    }

    public AutoArmor W() {
        return this.W;
    }

    public AimAssistant Y() {
        return this.Y;
    }

    public AntiBot Z() {
        return this.Z;
    }

    public NoFriendDamage ac() {
        return this.ac;
    }

    public NoServerDesync ae() {
        return this.ae;
    }

    public NoSlotChange af() {
        return this.af;
    }

    public AutoEXP aP() {
        return this.aP;
    }

    public Velocity bc() {
        return this.bc;
    }

    public WardenESP i() {
        return this.i;
    }

    public AutoWarden autoWarden() {
        return this.autoWarden;
    }

    public Interface_2 cI() {
        return this.bd;
    }

    public AutoEat aV() {
        return this.aV;
    }

    public WindHop aW() {
        return this.aW;
    }

    public WindBoost windBoost() {
        return this.windBoost;
    }

    public AutoSwap autoSwap() {
        return this.autoSwap;
    }

    public Speed speed() {
        return this.speed;
    }

    public Scaffold aS() {
        return this.aS;
    }

    public SafeWalk ah() {
        return this.ah;
    }

    public ScreenWalk f() {
        return this.f;
    }

    public AutoDodge l() {
        return this.l;
    }

    public NoCrouch n() {
        return this.n;
    }

    public Sprint o() {
        return this.o;
    }

    public NoSlowDown q() {
        return this.q;
    }

    public NoPush O() {
        return this.O;
    }

    public AirStuck av() {
        return this.av;
    }

    public NoDelay ax() {
        return this.ax;
    }

    public Fly aQ() {
        return this.aQ;
    }

    public WallClimb aR() {
        return this.aR;
    }

    public FastBreak aX() {
        return this.aX;
    }

    public SoundESP m() {
        return this.m;
    }

    public BoardSpoofer x() {
        return this.x;
    }

    public SwingAnimation R() {
        return this.R;
    }

    public SeeInvisibles T() {
        return this.T;
    }

    public BlockESP ab() {
        return this.ab;
    }

    public ShaderESP ad() {
        return this.ad;
    }

    public ItemPhysic ag() {
        return this.ag;
    }

    public Removals ai() {
        return this.ai;
    }

    public ViewModel ao() {
        return this.ao;
    }

    public Crosshair au() {
        return this.au;
    }

    public ShulkerPreview aw() {
        return this.aw;
    }

    public Predictions aC() {
        return this.aC;
    }

    public StreamerMode aE() {
        return this.aE;
    }

    public Pointers aL() {
        return this.aL;
    }

    public Arrows arrows() {
        return this.arrows;
    }

    public FullBright aO() {
        return this.aO;
    }

    public HandsShader aT() {
        return this.aT;
    }

    public ShaderSky shaderSky() { return this.shaderSky; }

    public EntityESP entityESP() { return this.entityESP; }

    public AucReissue aucReissue() { return this.aucReissue; }
    public AutoAccept autoAccept() { return this.autoAccept; }
    public AutoAuth autoAuth() { return this.autoAuth; }
    public AutoFish autoFish() { return this.autoFish; }
    public AutoLeave autoLeave() { return this.autoLeave; }
    public AutoRespawn autoRespawn() { return this.autoRespawn; }
    public AutoTool autoTool() { return this.autoTool; }
    public CaptchaSolver captchaSolver() { return this.captchaSolver; }
    public ChestStealer chestStealer() { return this.chestStealer; }
    public ClickAction clickAction() { return this.clickAction; }
    public DeathCoords deathCoords() { return this.deathCoords; }
    public FakeLags fakeLags() { return this.fakeLags; }
    public FastEXP fastExp() { return this.fastExp; }
    public FastLoad fastLoad() { return this.fastLoad; }
    public ItemScroller itemScroller() { return this.itemScroller; }
    public LockSlot lockSlot() { return this.lockSlot; }
    public OpenWalls openWalls() { return this.openWalls; }
    public SoundReducer soundReducer() { return this.soundReducer; }
    public Structures structures() { return this.structures; }
    public ThirdPerson thirdPerson() { return this.thirdPerson; }
    public UseTracker useTracker() { return this.useTracker; }
    public AncientFarmer ancientFarmer() { return this.ancientFarmer; }
    public AntiAFK antiAFK() { return this.antiAFK; }
    public AppleFarmer appleFarmer() { return this.appleFarmer; }
    public AutoBuy autoBuy() { return this.autoBuy; }
    public ChatHelper chatHelper() { return this.chatHelper; }
    public ClanUpgrader clanUpgrader() { return this.clanUpgrader; }
    public Collector_2 collector2() { return this.collector2; }
    public Communication communication() { return this.communication; }
    public FunDeliver funDeliver() { return this.funDeliver; }
    public MineAssistant mineAssistant() { return this.mineAssistant; }
    public NoCommands noCommands() { return this.noCommands; }
    public NoInteract noInteract() { return this.noInteract; }
    public Nuker nuker() { return this.nuker; }
    public PortalBypass portalBypass() { return this.portalBypass; }
    public PotionThrower potionThrower() { return this.potionThrower; }
    public ServerAssistant serverAssistant() { return this.serverAssistant; }
    public ServerJoiner serverJoiner() { return this.serverJoiner; }
    public SkullFix skullFix() { return this.skullFix; }
    public Sounds sounds() { return this.sounds; }
    public AutoCart autoCart() { return this.autoCart; }

    public Wasted wasted() { return this.wasted; }
    public XRay xRay() { return this.xRay; }
    public WaterSpeed waterSpeed() { return this.waterSpeed; }

    public AucReissue S() { return this.aucReissue; }
    public AutoAccept K() { return this.autoAccept; }
    public AutoAuth kAuth() { return this.autoAuth; }
    public AutoFish al() { return this.autoFish; }
    public AutoLeave bb() { return this.autoLeave; }
    public AutoRespawn P() { return this.autoRespawn; }
    public AutoTool N() { return this.autoTool; }
    public CaptchaSolver aH() { return this.captchaSolver; }
    public ChestStealer aD() { return this.chestStealer; }
    public ClickAction ap() { return this.clickAction; }
    public DeathCoords J() { return this.deathCoords; }
    public FakeLags gF() { return this.fakeLags; }
    public FastEXP aI() { return this.fastExp; }
    public FastLoad aN() { return this.fastLoad; }
    public ItemScroller w() { return this.itemScroller; }
    public LockSlot p() { return this.lockSlot; }
    public OpenWalls aOW() { return this.openWalls; }
    public SoundReducer r() { return this.soundReducer; }
    public Structures str() { return this.structures; }
    public ThirdPerson M() { return this.thirdPerson; }
    public UseTracker z() { return this.useTracker; }

    public File b() {
        return this.configDir;
    }

    public void b(String configName) {
        try {
            JSONArray root = new JSONArray();
            for (Module module : this.modules) {
                JSONObject modObj = new JSONObject();
                modObj.c("name", module.j());
                modObj.b("enabled", module.m());
                modObj.b("pinned", module.n());
                modObj.b("extended", module.o());
                modObj.c("key", module.p());
                JSONArray settingsArr = new JSONArray();
                for (Setting<?> setting : module.e()) {
                    JSONObject setObj = new JSONObject();
                    setObj.c("name", setting.i());
                    if (setting instanceof MultiModeSetting) {
                        JSONArray modesArr = new JSONArray();
                        for (BooleanSetting child : ((MultiModeSetting) setting).c()) {
                            JSONObject childObj = new JSONObject();
                            childObj.c("name", child.i());
                            childObj.b("value", child.c());
                            modesArr.a(childObj);
                        }
                        setObj.c("value", modesArr);
                    } else {
                        setObj.c("value", setting.c());
                    }
                    settingsArr.a(setObj);
                }
                modObj.c("settings", settingsArr);
                root.a(modObj);
            }
            Files.writeString(new File(configDir, configName + ".json").toPath(), root.E(2));
            this.lastConfigName = configName;
            writeLastConfigName(configName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean c(String configName) {
        File file = new File(configDir, configName + ".json");
        if (!file.exists()) {
            return false;
        }
        this.loadingConfig = true;
        try {
            JSONArray root = new JSONArray(Files.readString(file.toPath()));
            for (int i = 0; i < root.a(); i++) {
                JSONObject modObj = root.j(i);
                Module module = a(modObj.l("name"));
                if (module == null) {
                    continue;
                }
                if (modObj.m("enabled")) {
                    module.a(modObj.b("enabled"));
                }
                if (modObj.m("pinned")) {
                    module.b(modObj.b("pinned"));
                }
                if (modObj.m("extended")) {
                    module.c(modObj.b("extended"));
                }
                if (modObj.m("key")) {
                    module.a(modObj.h("key"));
                }
                JSONArray settingsArr = modObj.i("settings");
                if (settingsArr == null) {
                    continue;
                }
                for (int j = 0; j < settingsArr.a(); j++) {
                    JSONObject setObj = settingsArr.j(j);
                    String setName = setObj.l("name");
                    if (setName == null) {
                        continue;
                    }
                    for (Setting<?> setting : module.e()) {
                        if (setting.i().equalsIgnoreCase(setName)) {
                            a(setting, setObj);
                        }
                    }
                }
            }
            this.lastConfigName = configName;
            writeLastConfigName(configName);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            this.loadingConfig = false;
        }
    }

    private String readLastConfigName() {
        try {
            File file = new File(configDir, "lastConfig.txt");
            if (file.exists()) {
                String name = Files.readString(file.toPath()).trim();
                if (!name.isBlank()) {
                    return name;
                }
            }
        } catch (IOException ignored) {
        }
        return "default";
    }

    private void writeLastConfigName(String name) {
        try {
            Files.writeString(new File(configDir, "lastConfig.txt").toPath(), name);
        } catch (IOException ignored) {
        }
    }

    private Module a(String name) {
        if (name == null) {
            return null;
        }
        for (Module module : this.modules) {
            if (module.j().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void a(Setting<?> setting, JSONObject setObj) {
        if (setting instanceof MultiModeSetting) {
            JSONArray modesArr = setObj.i("value");
            if (modesArr == null) {
                return;
            }
            for (int i = 0; i < modesArr.a(); i++) {
                JSONObject childObj = modesArr.j(i);
                BooleanSetting child = ((MultiModeSetting) setting).a(childObj.l("name"));
                if (child != null) {
                    child.a(childObj.b("value"));
                }
            }
        } else if (setting instanceof BooleanSetting) {
            ((Setting<Boolean>) setting).a(setObj.b("value"));
        } else if (setting instanceof SliderSetting slider) {
            float value = setObj.f("value");
            ((Setting<Float>) setting).a(Math.max(slider.a, Math.min(slider.b, value)));
        } else if (setting instanceof ModeSetting mode) {
            String value = setObj.l("value");
            if (value != null && mode.k().stream().anyMatch(m -> m.equalsIgnoreCase(value))) {
                ((Setting<String>) setting).a(value);
            }
        } else if (setting instanceof BindSetting || setting instanceof ColorSetting) {
            ((Setting<Integer>) setting).a(setObj.h("value"));
        } else if (setting instanceof StringSetting) {
            ((Setting<String>) setting).a(setObj.l("value"));
        }
    }

    public boolean d(String configName) {
        File configFile = new File(configDir, configName + ".json");
        if (configFile.exists()) {
            return configFile.delete();
        }
        return false;
    }
}
