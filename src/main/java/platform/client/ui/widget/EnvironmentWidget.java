package platform.client.ui.widget;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.event.GlobalEvent;
import platform.api.module.Interface;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.player.InventoryUtil;
import platform.api.system.configs.ThemeInfo;
import platform.api.system.configs.ThemeProcessor;
import platform.api.event.events.render.DrawEvent;
import platform.api.module.setting.BooleanSetting;
import platform.client.ui.element.DragInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.UseCooldown;

public class EnvironmentWidget extends Widget implements Interface {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final BooleanSetting f;
    private final Map<UUID, b> g;
    private final List<UUID> h;

    public EnvironmentWidget() {
        super(new DragInfo("Окружение", 0.0f, 0.0f, 0.0f, 0.0f));
        this.f = new BooleanSetting("Показывать броню", true);
        this.g = new HashMap();
        this.h = new ArrayList();
        j().a(this);
        addSettings(this.f);
    }

    @Override
    public void a(DrawEvent event) {
        GuiGraphicsExtractor context = event.i();
        if (context == null) {
            super.a(event);
            return;
        }
        d().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
        float animation = a();
        float x = j().a();
        float y = j().b();
        Stream<UUID> stream = this.h.stream();
        Map<UUID, b> map = this.g;
        Objects.requireNonNull(map);
        List<b> shown = stream.map(v1 -> {
            return map.get(v1);
        }).filter(Objects::nonNull).toList();
        float width = 14.5f + Fonts.e.a("Окружение", this.e) + 5.0f;
        for (b data : shown) {
            width = Math.max(width, Math.max(20.0f + Fonts.e.a(data.d + ((int) data.f) + "HP", 6.5f) + 34.0f, (data.b.size() * 15.0f) - 2.0f));
        }
        j().c(width);
        if (animation > 0.0f) {
            drawHeader(context, "x", "Окружение", width, animation);
        }
        float contentY = y + this.d + 3.0f;
        Iterator<b> it = shown.iterator();
        while (it.hasNext()) {
            contentY += a(event, context, it.next(), x, contentY, width, animation);
        }
        j().d(Math.max(this.d, (contentY - y) - 2.0f));
        super.a(event);
    }

    private float a(DrawEvent event, GuiGraphicsExtractor context, b data, float x, float y, float width, float animation) {
        float textY = (y + ((12.0f - Fonts.e.a(6.5f)) / 2.0f)) - 0.5f;
        String health = ((int) data.f) + "HP";
        a(event, context, x, y, width, 12.0f, 3.0f, animation, false);
        event.d().a(context, data.e, null, x + 2.0f, y + 2.0f, 8.0f, 8.0f, 1.5f, animation);
        Fonts.e.a(context, health, ((x + width) - 3.0f) - Fonts.e.a(health, 6.5f), textY, 6.5f, ColorUtil.a(-1, animation));
        float right = ((x + width) - 4.0f) - Fonts.e.a(health, 6.5f);
        if (this.f.c().booleanValue()) {
            for (int i = 3; i >= 0; i--) {
                if (!data.c[i].isEmpty()) {
                    right -= 7.0f;
                    event.e().a(context, InventoryUtil.a(data.c[i]), right - 2.0f, y + 2.0f, 0, animation, 0.5f, false);
                }
            }
        }
        Fonts.e.c(context, data.d, x + 12.5f, textY, 6.5f, ColorUtil.a(-1, animation), (right - 16.5f) - x);
        if (data.b.isEmpty()) {
            return 14.0f;
        }
        long now = System.currentTimeMillis();
        List<a> history = new ArrayList<>(data.b.values());
        Collections.reverse(history);
        for (int i2 = 0; i2 < history.size(); i2++) {
            a entry = history.get(i2);
            float itemX = x + (i2 * 15.0f);
            float itemY = y + 12.0f + 2.0f;
            int left = entry.a(now);
            a(event, context, itemX, itemY, 13.0f, 13.0f, 2.0f, animation, entry.c != 0);
            event.e().a(context, entry.a, itemX + 2.1f, itemY + 2.1f, 0, animation, 0.55f, entry.c == 0);
            if (left > 0) {
                String text = left > 99 ? "99+" : String.valueOf(left);
                float textWidth = Fonts.e.a(text, 6.5f);
                float badge = textWidth + 6.0f;
                float badgeX = itemX + ((13.0f - badge) / 2.0f);
                float badgeY = itemY - 4.0f;
                float textX = badgeX + (((badge - textWidth) - 0.3f) / 2.0f);
                float textY2 = badgeY + ((8.0f - Fonts.e.a(6.5f)) / 2.0f) + 1.0f;
                int color = ColorUtil.a(ColorUtil.a(255, 60, 60, 255), animation);
                a(event, context, badgeX + 1.5f, badgeY + 1.0f, badge - 3.0f, 8.0f, 2.0f, animation, false);
                Fonts.e.a(context, text, textX, textY2, 6.5f, color);
                Fonts.e.a(context, text, textX + 0.3f, textY2, 6.5f, color);
            }
        }
        return 29.0f;
    }

    private void a(DrawEvent event, GuiGraphicsExtractor context, float x, float y, float width, float height, float radius, float animation, boolean empty) {
        ThemeProcessor theme = Delta.h().d().o();
        int background = ColorUtil.a(theme.a(ThemeInfo.BACKGROUND_HUD).a(), theme.a(ThemeInfo.PRIMARY).a(), theme.a(ThemeInfo.PRIMARY).b() / 6.0f);
        event.d().b(context, x, y, width, height, radius, ColorUtil.a(empty ? ColorUtil.a(background, ColorUtil.a(255, 60, 60, 255), 0.35f) : background, theme.a(ThemeInfo.BACKGROUND_HUD).b() * animation), animation);
    }

    @Override
    public void a(GlobalEvent event) {
        if (aM_.level != null && aM_.player != null) {
            long now = System.currentTimeMillis();
            List<? extends Player> nearby = aM_.level.players().stream().filter(player -> {
                return (player == aM_.player || !player.isAlive()) ? false : true;
            }).sorted(Comparator.<Player>comparingInt(EnvironmentWidget::a).thenComparing(player -> {
                return player.getName().getString();
            }, (v0, v1) -> {
                return v0.compareToIgnoreCase(v1);
            })).limit(2L).toList();
            this.h.clear();
            for (Player player : nearby) {
                this.h.add(player.getUUID());
                this.g.computeIfAbsent(player.getUUID(), b::new).a(player, now);
            }
            this.g.values().removeIf(data -> {
                return !this.h.contains(data.a) && now - data.g > 30000;
            });
        }
        d().a(aM_.level != null && (!this.h.isEmpty() || (aM_.gui.screen() instanceof ChatScreen)));
        super.a(event);
    }

    private static int a(Player player) {
        int score = 0;
        for (int i = 0; i < 4; i++) {
            String name = BuiltInRegistries.ITEM.getKey(player.getItemBySlot(ARMOR_SLOTS[i]).getItem()).getPath();
            int value;
            if (name.startsWith("netherite_")) {
                value = 4;
            } else if (name.startsWith("diamond_")) {
                value = 3;
            } else if (name.startsWith("iron_")) {
                value = 2;
            } else {
                value = name.startsWith("leather_") ? 1 : 0;
            }
            score += value;
        }
        return score;
    }

    private static boolean a(ItemStack stack, Item item) {
        return stack.is(it -> it.value() == item);
    }

    static int a(ItemStack stack) {
        if (a(stack, Items.DRIED_KELP) || a(stack, Items.NETHERITE_SCRAP) || a(stack, Items.SNOWBALL) || a(stack, Items.SUGAR) || a(stack, Items.PHANTOM_MEMBRANE) || a(stack, Items.WIND_CHARGE) || a(stack, Items.ENDER_EYE)) {
            return 3;
        }
        if (stack.has(DataComponents.FOOD)) {
            return 2;
        }
        return (a(stack, Items.EXPERIENCE_BOTTLE) || a(stack, Items.FIRE_CHARGE)) ? 1 : 0;
    }

    static int b(ItemStack stack) {
        if (a(stack, Items.ENCHANTED_GOLDEN_APPLE)) {
            return 150;
        }
        if (a(stack, Items.SUGAR) || a(stack, Items.ENDER_EYE) || a(stack, Items.FIRE_CHARGE)) {
            return 60;
        }
        if (a(stack, Items.GOLDEN_APPLE)) {
            return 30;
        }
        if (a(stack, Items.DRIED_KELP)) {
            return 25;
        }
        return a(stack, Items.NETHERITE_SCRAP) ? 15 : 0;
    }

    static final class a {
        final ItemStack a;
        long b;
        long c;
        int d;

        a(ItemStack stack, int hand) {
            this.a = stack.copy();
            this.a.setDamageValue(0);
            this.a.set(DataComponents.USE_COOLDOWN, new UseCooldown(0.0f, Optional.of(Identifier.fromNamespaceAndPath("delta", "widget"))));
            this.d = hand;
        }

        int a(long now) {
            int total = EnvironmentWidget.b(this.a);
            if (total == 0 || this.b == 0) {
                return 0;
            }
            return Math.max((int) Math.ceil(((((long) total) * 1000) - (now - this.b)) / 1000.0d), 0);
        }
    }

    static final class b {
        final UUID a;
        final LinkedHashMap<Item, a> b = new LinkedHashMap<>();
        final ItemStack[] c = new ItemStack[6];
        String d = "";
        Identifier e;
        float f;
        long g;

        private b(UUID id) {
            this.a = id;
            Arrays.fill(this.c, ItemStack.EMPTY);
        }

        void a(Player player, long now) {
            this.d = player.getName().getString();
            if (player instanceof LocalPlayer local) {
                this.e = local.getSkin().body().texturePath();
            } else {
                this.e = DefaultPlayerSkin.getDefaultTexture();
            }
            this.f = player.getHealth() + player.getAbsorptionAmount();
            this.g = now;
            for (int i = 0; i < 4; i++) {
                this.c[i] = player.getItemBySlot(ARMOR_SLOTS[i]);
            }
            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();
            for (a entry : this.b.values()) {
                if (entry.c == 0 && entry.a.getCount() == 1 && this.c[entry.d].is(it -> it.value() == entry.a.getItem())) {
                    if ((entry.d == 4 ? main : off).isEmpty()) {
                        entry.c = now;
                    }
                }
            }
            this.c[4] = main;
            this.c[5] = off;
            for (int hand = 4; hand < 6; hand++) {
                if (EnvironmentWidget.a(this.c[hand]) > 0) {
                    a(player, this.c[hand], hand, now);
                }
            }
            this.b.values().removeIf(entry2 -> {
                return entry2.c != 0 && now - entry2.c > 10000;
            });
        }

        private void a(Player player, ItemStack stack, int hand, long now) {
            a entry = this.b.remove(stack.getItem());
            if (entry != null) {
                if (stack.getCount() < entry.a.getCount() && EnvironmentWidget.b(stack) > 0 && Interface.aM_.level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(2.0d), item -> {
                    return item.getItem().is(it -> it.value() == stack.getItem());
                }).isEmpty()) {
                    entry.b = now;
                }
                entry.a.setCount(stack.getCount());
                entry.d = hand;
                entry.c = 0L;
                this.b.put(stack.getItem(), entry);
                return;
            }
            if (this.b.size() >= 9) {
                Optional<Item> map = this.b.entrySet().stream().filter(candidate -> {
                    return EnvironmentWidget.a(((a) candidate.getValue()).a) < EnvironmentWidget.a(stack);
                }).findFirst().map(Map.Entry::getKey);
                LinkedHashMap<Item, a> linkedHashMap = this.b;
                Objects.requireNonNull(linkedHashMap);
                map.ifPresent(linkedHashMap::remove);
                if (this.b.size() >= 9) {
                    return;
                }
            }
            this.b.put(stack.getItem(), new a(stack, hand));
        }
    }
}


