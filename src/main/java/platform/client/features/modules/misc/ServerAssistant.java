package platform.client.features.modules.misc;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.text.ChatUtil;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.player.InventoryUtil;
import platform.client.utils.player.ServerUtil;

import platform.api.system.configs.DescriptionProcessor;
import platform.api.system.configs.EnchantmentProcessor;
import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.other.ContainerEvent;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;
import platform.api.event.events.render.TooltipEvent;

import platform.client.utils.render.AnimationUtil;
import platform.api.module.setting.BindSetting;
import platform.api.module.setting.BooleanSetting;
import platform.client.utils.timer.CounterUtil;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.MultiModeSetting;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Generated;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import platform.inject.accessors.ServerboundContainerClickPacketAccessor;
import platform.inject.accessors.HandledScreenAccessor;
import platform.inject.accessors.ItemCooldownsAccessor;
import platform.inject.accessors.ItemCooldownsEntryAccessor;

@ModuleRegister(a = "Server Assistant", b = "Помощник, упрощающий работу с сервером и игровыми механиками", c = Category.Misc)
public class ServerAssistant extends Module implements Interface {
    private List<b> w;
    private final ModeSetting b = new ModeSetting("Целевой сервер помощи", "FunTime", "FunTime", "SpookyTime", "HolyWorld");
    private final MultiModeSetting c = (MultiModeSetting) new MultiModeSetting("Элементы помощи", new BooleanSetting("Аукционный ассистент", true), new BooleanSetting("Сортировать по цене", true)).a(() -> {
        return Boolean.valueOf(this.b.l("FunTime") || this.b.l("SpookyTime"));
    });
    final MultiModeSetting d = (MultiModeSetting) new MultiModeSetting("Фильтр брони по", new BooleanSetting("Защите", false), new BooleanSetting("Аншип", true), new BooleanSetting("Починке", true), new BooleanSetting("Подводной ходьбе", true)).a(() -> {
        return Boolean.valueOf(this.c.a("Аукционный ассистент").c().booleanValue() && (this.b.l("FunTime") || this.b.l("SpookyTime")));
    });
    final MultiModeSetting e = (MultiModeSetting) new MultiModeSetting("Фильтр меча по", new BooleanSetting("Остроте", false), new BooleanSetting("Детекции", true), new BooleanSetting("Вампиризму", true), new BooleanSetting("Окислению", true), new BooleanSetting("Яду", true)).a(() -> {
        return Boolean.valueOf(this.c.a("Аукционный ассистент").c().booleanValue() && (this.b.l("FunTime") || this.b.l("SpookyTime")));
    });
    final MultiModeSetting f = (MultiModeSetting) new MultiModeSetting("Фильтр кирки по", new BooleanSetting("Эффективности", false), new BooleanSetting("Удаче", true), new BooleanSetting("Магнит", true), new BooleanSetting("Починке", true)).a(() -> {
        return Boolean.valueOf(this.c.a("Аукционный ассистент").c().booleanValue() && (this.b.l("FunTime") || this.b.l("SpookyTime")));
    });
    private final BindSetting g = a("Трапка", Items.NETHERITE_SCRAP, "FunTime", "SpookyTime");
    private final BindSetting h = a("Снежок заморозка", Items.SNOWBALL, "FunTime", "SpookyTime");
    private final BindSetting i = a("Пласт", Items.DRIED_KELP, "FunTime", "SpookyTime");
    private final BindSetting j = a("Дезориентация", Items.ENDER_EYE, "FunTime", "SpookyTime");
    private final BindSetting k = a("Явная пыль", Items.SUGAR, "FunTime", "SpookyTime");
    private final BindSetting l = a("Заряд ветра", Items.WIND_CHARGE, "FunTime", "SpookyTime");
    private final BindSetting m = a("Огненный заряд", Items.FIRE_CHARGE, "FunTime", "SpookyTime");
    private final BindSetting n = a("Божья аура", Items.PHANTOM_MEMBRANE, "FunTime", "SpookyTime");
    private final BindSetting o = a("Взрывная штучка", Items.FIRE_CHARGE, "HolyWorld");
    private final BindSetting p = a("Взрывная палочка", Items.BLAZE_ROD, "HolyWorld");
    private final BindSetting q = a("Взрывная трапка", Items.PRISMARINE_SHARD, "HolyWorld");
    private final BindSetting r = a("Стан", Items.NETHER_STAR, "HolyWorld");
    private final BindSetting s = a("Ком снега", Items.SNOWBALL, "HolyWorld");
    private final BooleanSetting t = (BooleanSetting) new BooleanSetting("Авто-божья аура", false).a(() -> {
        return Boolean.valueOf(this.b.l("FunTime") || this.b.l("SpookyTime"));
    });
    private final a u = new a();
    private final CounterUtil v = new CounterUtil();
    private Slot x = null;
    private int[] y = null;
    private int z = -1;

    @Generated
    public List<b> q() {
        return this.w;
    }

    public ServerAssistant() {
        a(this.b, this.c, this.d, this.e, this.f, this.g, this.h, this.i, this.j, this.k, this.m, this.l, this.n, this.o, this.p, this.q, this.r, this.s, this.t);
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.t.c().booleanValue()) {
            if (ServerUtil.a.a() || ServerUtil.d.a()) {
                boolean hasBadEffect = aM_.player.getActiveEffects().stream().anyMatch(effect -> {
                    return effect.is(MobEffects.WEAKNESS) && effect.getAmplifier() >= 1 && ((double) effect.getDuration()) / 20.0d >= 15.0d;
                });
                if (InventoryUtil.b(Items.PHANTOM_MEMBRANE) != -1 && hasBadEffect && !aM_.player.getCooldowns().isOnCooldown(Items.PHANTOM_MEMBRANE.getDefaultInstance()) && this.v.a(5000L) && aM_.player.getAbsorptionAmount() <= 3.0f) {
                    Delta.h().d().v().b().a(Items.PHANTOM_MEMBRANE.getDefaultInstance());
                    this.v.b();
                }
            }
        }
    }

    @EventTarget
    public void a(TooltipEvent event) {
        ItemStack stack = event.b();
        int price = ServerUtil.a.a(stack);
        if (price > 0 && stack.getCount() > 1) {
            List<Component> lines = event.c();
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).getString().contains("$ Ценa: ")) {
                    lines.add(i + 1, Component.literal("$").withStyle(ChatFormatting.GREEN).append(Component.literal(" Цена за штуку: ").withStyle(ChatFormatting.WHITE)).append(Component.literal(String.format(Locale.US, "%,d", Integer.valueOf(price))).withStyle(ChatFormatting.GREEN)));
                    return;
                }
            }
        }
    }

    @EventTarget
    public void a(ContainerEvent event) {
        HandledScreenAccessor handledScreenAccessorB = (HandledScreenAccessor) event.b();
        if (handledScreenAccessorB instanceof ContainerScreen) {
            HandledScreenAccessor handledScreenAccessor = (HandledScreenAccessor) (ContainerScreen) handledScreenAccessorB;
            ToIntFunction<ItemStack> price = ServerUtil.a.a() ? ServerUtil.a::a : ServerUtil.d::a;
            if (event.h() == ContainerEvent.Phase.TITLE) {
                if (event.i().getString().contains("Хранилище")) {
                    long storage = event.c().slots.stream().mapToLong(slot -> {
                        return ((long) Math.max(price.applyAsInt(slot.getItem()), 0)) * ((long) Math.max(slot.getItem().getCount(), 1));
                    }).sum();
                    String suffix = " - " + ((String) Stream.of(Map.entry(1000000000L, "ккк"), Map.entry(1000000L, "кк"), Map.entry(1000L, "к")).filter((Map.Entry<Long, String> unit) -> {
                        return storage >= ((Long) unit.getKey()).longValue();
                    }).map((Map.Entry<Long, String> unit2) -> {
                        return String.format(Locale.US, "%.0f%s", Double.valueOf(storage / ((Long) unit2.getKey()).longValue()), unit2.getValue());
                    }).findFirst().orElseGet(() -> {
                        return String.valueOf(storage);
                    }));
                    MutableComponent title = Component.empty();
                    event.i().visit((style, part) -> {
                        title.append(Component.literal(part).setStyle(style));
                        if (part.contains("Хранилище")) {
                            title.append(Component.literal(suffix).setStyle(style));
                        }
                        return Optional.empty();
                    }, Style.EMPTY);
                    event.a((Component) title);
                    return;
                }
                return;
            }
            if (event.c().slots.size() >= 90) {
                List<Slot> containerSlots = new ArrayList<>(event.e().subList(0, event.e().size() - 36));
                boolean useFilter = containerSlots.stream().anyMatch(slot2 -> {
                    return price.applyAsInt(slot2.getItem()) >= 0 && this.u.a(slot2.getItem());
                });
                if (event.h() == ContainerEvent.Phase.POST && this.c.a("Аукционный ассистент").c().booleanValue()) {
                    if (this.x == null) {
                        Slot cheapest = null;
                        int minPrice = Integer.MAX_VALUE;
                        for (Slot slot3 : containerSlots) {
                            ItemStack stack = slot3.getItem();
                            int value = price.applyAsInt(stack);
                            if (!useFilter || this.u.a(stack)) {
                                if (value >= 0 && value < minPrice) {
                                    minPrice = value;
                                    cheapest = slot3;
                                }
                            }
                        }
                        this.x = cheapest;
                        return;
                    }
                    HandledScreenAccessor accessor = (HandledScreenAccessor) handledScreenAccessor;
                    float pulse = (float) ((Math.sin(((System.currentTimeMillis() % 100000) / 1000.0f) * 10.0f) + 1.0d) * 0.5d);
                    Delta.h().d().i().a(event.d(), accessor.getX() + ((Slot) this.x).x, accessor.getY() + ((Slot) this.x).y, 16.0f, 16.0f, ColorUtil.a(0, 255, 0, (int) (25.0f + (175.0f * pulse))));
                }
            }
        }
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (event.c()) {
            if (event.d() instanceof ClientboundSystemChatPacket) {
                ClientboundSystemChatPacket message = (ClientboundSystemChatPacket) event.d();
                if (ServerUtil.a.a() && message.content().getString().equals("На этой анархии этот предмет не работает")) {
                    this.z = ServerUtil.a.d();
                }
            }
            if (event.d() instanceof ClientboundContainerSetContentPacket) {
                ClientboundContainerSetContentPacket packet = (ClientboundContainerSetContentPacket) event.d();
                if (this.c.a("Сортировать по цене").c().booleanValue() && (ServerUtil.a.a() || ServerUtil.d.a())) {
                    List<ItemStack> contents = packet.items();
                    int chestSlots = contents.size() > 36 ? contents.size() - 36 : contents.size();
                    this.y = null;
                    if (chestSlots >= 44 && packet.containerId() != 0) {
                        ToIntFunction<ItemStack> price = ServerUtil.a.a() ? ServerUtil.a::a : ServerUtil.d::a;
                        boolean useFilter = contents.subList(0, chestSlots).stream().anyMatch(stack -> {
                            return price.applyAsInt(stack) >= 0 && this.u.a(stack);
                        });
                        int[] prices = new int[chestSlots];
                        for (int i = 0; i < chestSlots; i++) {
                            ItemStack stack2 = contents.get(i);
                            int value = (stack2.isEmpty() || (useFilter && !this.u.a(stack2))) ? -1 : price.applyAsInt(stack2);
                            prices[i] = value < 0 ? Integer.MAX_VALUE : value;
                        }
                        Integer[] order = (Integer[]) IntStream.range(0, chestSlots).boxed().sorted(Comparator.comparingInt(i2 -> {
                            return prices[i2.intValue()];
                        })).toArray(x$0 -> {
                            return new Integer[x$0];
                        });
                        List<ItemStack> original = new ArrayList<>(contents);
                        this.y = new int[chestSlots];
                        for (int display = 0; display < chestSlots; display++) {
                            this.y[display] = order[display].intValue();
                            contents.set(display, original.get(order[display].intValue()));
                        }
                    }
                }
            }
            if (event.d() instanceof ClientboundContainerSetSlotPacket) {
                ClientboundContainerSetSlotPacket slotPacket = (ClientboundContainerSetSlotPacket) event.d();
                if (this.y != null && slotPacket.getContainerId() != 0 && slotPacket.getContainerId() == aM_.player.containerMenu.containerId && slotPacket.getSlot() >= 0 && slotPacket.getSlot() < this.y.length) {
                    for (int display2 = 0; display2 < this.y.length; display2++) {
                        if (this.y[display2] == slotPacket.getSlot()) {
                            try {
                                java.lang.reflect.Field f = ClientboundContainerSetSlotPacket.class.getDeclaredField("slot");
                                f.setAccessible(true);
                                f.set(slotPacket, display2);
                            } catch (Exception ignored) {
                            }
                            break;
                        }
                    }
                }
                this.x = null;
            }
        }
        if (event.b()) {
            if (event.d() instanceof ServerboundContainerClickPacket) {
                ServerboundContainerClickPacketAccessor clickSlotC2SPacketAccessor = (ServerboundContainerClickPacketAccessor) event.d();
                if (this.y != null && clickSlotC2SPacketAccessor.getSyncId() != 0 && clickSlotC2SPacketAccessor.getSyncId() == aM_.player.containerMenu.containerId && clickSlotC2SPacketAccessor.getSlot() >= 0 && clickSlotC2SPacketAccessor.getSlot() < this.y.length) {
                    clickSlotC2SPacketAccessor.setSlot((short) this.y[clickSlotC2SPacketAccessor.getSlot()]);
                }
            }
        }
    }

    private BindSetting a(String name, Item item, String... servers) {
        BindSetting setting = (BindSetting) new BindSetting(name, -1).a(() -> {
            ItemCooldownsAccessor accessor = (ItemCooldownsAccessor) aM_.player.getCooldowns();
            if (aM_.player.getCooldowns().isOnCooldown(item.getDefaultInstance())) {
                Object entry = accessor.getCooldowns().get(aM_.player.getCooldowns().getCooldownGroup(item.getDefaultInstance()));
                Locale locale = Locale.US;
                Object[] objArr = new Object[1];
                objArr[0] = Float.valueOf(entry != null ? Math.max(((ItemCooldownsEntryAccessor) entry).getEndTime() - accessor.getTickCount(), 0) / 20.0f : 0.0f);
                ChatUtil.a((Object) ("&c" + name + "&7 - имеет задержку &c" + String.format(locale, "%.1fс", objArr)));
                return;
            }
            if (InventoryUtil.b(item) == -1) {
                ChatUtil.a((Object) ("&c" + name + "&7 - нет в инвентаре"));
            } else {
                Delta.h().d().v().b().a(item.getDefaultInstance());
            }
        }).a(() -> {
            Stream stream = Arrays.stream(servers);
            ModeSetting modeSetting = this.b;
            Objects.requireNonNull(modeSetting);
            return Boolean.valueOf(Arrays.stream(servers).anyMatch(server -> modeSetting.l(server)));
        });
        if (this.w == null) {
            this.w = new ArrayList();
        }
        this.w.add(new b(new AnimationUtil(), setting, item));
        return setting;
    }

    public class a {
        public a() {
        }

        public boolean a(ItemStack stack) {
            Item item = stack.getItem();
            boolean isArmor = stack.has(DataComponents.EQUIPPABLE) && ((Equippable) stack.get(DataComponents.EQUIPPABLE)).slot().isArmor();
            if (isArmor) {
                return b(stack);
            }
            if (item.builtInRegistryHolder().is(ItemTags.SWORDS)) {
                return c(stack);
            }
            if (item.builtInRegistryHolder().is(ItemTags.PICKAXES)) {
                return d(stack);
            }
            return true;
        }

        private boolean b(ItemStack stack) {
            boolean isArmor = stack.has(DataComponents.EQUIPPABLE) && ((Equippable) stack.get(DataComponents.EQUIPPABLE)).slot().isArmor();
            if (isArmor) {
                EnchantmentProcessor enchament = new EnchantmentProcessor();
                if (ServerAssistant.this.d.a("Защите").c().booleanValue()) {
                    enchament.a(Enchantments.UNBREAKING, 4);
                    enchament.a(Enchantments.PROTECTION, 5);
                }
                if (ServerAssistant.this.d.a("Аншип").c().booleanValue()) {
                    enchament.b(Enchantments.THORNS);
                }
                if (ServerAssistant.this.d.a("Починке").c().booleanValue()) {
                    enchament.a(Enchantments.MENDING, 1);
                }
                if (ServerAssistant.this.d.a("Подводной ходьбе").c().booleanValue() && stack.get(DataComponents.EQUIPPABLE) != null && ((Equippable) stack.get(DataComponents.EQUIPPABLE)).slot() == EquipmentSlot.FEET) {
                    enchament.a(Enchantments.DEPTH_STRIDER, 1);
                }
                return enchament.a(stack);
            }
            return true;
        }

        private boolean c(ItemStack stack) {
            if (stack.getItem().builtInRegistryHolder().is(ItemTags.SWORDS)) {
                EnchantmentProcessor enchantmentProcessor = new EnchantmentProcessor().b(Enchantments.KNOCKBACK, 2);
                if (ServerAssistant.this.e.a("Остроте").c().booleanValue()) {
                    enchantmentProcessor.a(Enchantments.SHARPNESS, 6);
                }
                enchantmentProcessor.b(Enchantments.KNOCKBACK, 1);
                if (enchantmentProcessor.a(stack)) {
                    DescriptionProcessor descriptionProcessor = new DescriptionProcessor();
                    descriptionProcessor.b("Нестабильность ");
                    descriptionProcessor.b("Нестабильный ");
                    if (ServerAssistant.this.e.a("Детекции").c().booleanValue()) {
                        descriptionProcessor.a("Детекция", 2);
                    }
                    if (ServerAssistant.this.e.a("Вампиризму").c().booleanValue()) {
                        descriptionProcessor.a("Вампиризм", 2);
                    }
                    if (ServerAssistant.this.e.a("Окислению").c().booleanValue()) {
                        descriptionProcessor.a("Окисление", 2);
                    }
                    if (ServerAssistant.this.e.a("Яду").c().booleanValue()) {
                        descriptionProcessor.a("Яд", 3);
                    }
                    return descriptionProcessor.a(stack);
                }
                return false;
            }
            return false;
        }

        private boolean d(ItemStack stack) {
            if (stack.getItem().builtInRegistryHolder().is(ItemTags.PICKAXES)) {
                if (ServerAssistant.this.f.a("Починке").c().booleanValue() && !new EnchantmentProcessor().a(Enchantments.MENDING, 1).a(stack)) {
                    return false;
                }
                DescriptionProcessor descriptionProcessor = new DescriptionProcessor();
                if (ServerAssistant.this.f.a("Удаче").c().booleanValue()) {
                    descriptionProcessor.a("Удача", 5);
                }
                if (ServerAssistant.this.f.a("Эффективности").c().booleanValue()) {
                    descriptionProcessor.a("Эффективность", 4);
                }
                if (ServerAssistant.this.f.a("Магнит").c().booleanValue()) {
                    descriptionProcessor.a("Магнит");
                }
                return descriptionProcessor.a(stack);
            }
            return false;
        }
    }

    public static final class b {
        private final AnimationUtil a;
        private final BindSetting b;
        private final Item c;

        public b(AnimationUtil animationUtil, BindSetting bind, Item item) {
            this.a = animationUtil;
            this.b = bind;
            this.c = item;
        }
 public AnimationUtil a() {
            return this.a;
        }

        public BindSetting b() {
            return this.b;
        }

        public Item c() {
            return this.c;
        }
    }
}
