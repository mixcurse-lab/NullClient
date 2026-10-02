package platform.client.features.modules.render;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.InterfaceC0020Opcode;
import platform.api.module.Module;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.MathUtil;
import platform.client.utils.math.ProjectUtil;
import platform.client.utils.player.ServerUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.DrawEvent;
import platform.client.features.modules.misc.StreamerMode;
import platform.api.module.setting.BooleanSetting;

import platform.api.module.setting.MultiModeSetting;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;
import org.joml.Vector2f;

@ModuleRegister(a = "Entity ESP", b = "Отображает информацию о сущностях над их головой", c = Category.Render)
public class EntityESP extends Module {
    private final MultiModeSetting b = new MultiModeSetting("Отслеживаемые сущности", new BooleanSetting("Игроки", true), new BooleanSetting("Животные", false), new BooleanSetting("Мобы", false), new BooleanSetting("Предметы", false));
    private final BooleanSetting d = new BooleanSetting("Скрывать ники", true);
    private final List<a> c = new ArrayList();

    @Generated
    public List<a> q() {
        return this.c;
    }

    public EntityESP() {
        a(this.b, this.d);
    }

    private String d(Entity entity) {
        if (entity instanceof Player) {
            return "Игроки";
        }
        if (entity instanceof Monster) {
            return "Мобы";
        }
        if ((entity instanceof Animal) || (entity instanceof Shulker) || (entity instanceof Villager)) {
            return "Животные";
        }
        return ((entity instanceof ItemEntity) || (entity instanceof Arrow)) ? "Предметы" : null;
    }

    public boolean isVanillaNameHidden(Entity entity) {
        if (!this.m() || !this.d.c().booleanValue()) {
            return false;
        }
        String key = d(entity);
        return key != null && this.b.a(key).c().booleanValue();
    }

    @EventTarget
    public void a(DrawEvent event) {
        if (event.b()) {
            if (aM_.level == null || aM_.player == null) return;
            for (Entity entity : aM_.level.entitiesForRendering()) {
                if (entity != aM_.player) {
                    String key = d(entity);
                    if (key != null && this.b.a(key).c().booleanValue()) {
                        int color = ((entity instanceof Player) && Delta.h().d().e().d(entity.getName().getString())) ? ColorUtil.a(0, 100, 0, InterfaceC0020Opcode.bN) : ColorUtil.a(0, 0, 0, 80);
                        Vec3 interpolated = MathUtil.a(entity, event.g());
                        Vec3 entityPos = interpolated.add(0.0d, entity.getBbHeight() + 0.25f, 0.0d);
                        Vector2f screenPos = ProjectUtil.a(entityPos.x, entityPos.y, entityPos.z);
                        if (ProjectUtil.a(screenPos)) {
                            if ((entity instanceof ItemEntity) || (entity instanceof Arrow)) {
                                c(entity, event, screenPos, 7.5f, 2.0f, color);
                            } else {
                                a(entity, event, screenPos, 7.5f, 2.0f, color);
                                b(entity, event, ProjectUtil.a(interpolated.x, interpolated.y - 0.25d, interpolated.z), 7.5f, 2.0f, color);
                            }
                        }
                    }
                }
            }
        }
    }

    private void a(Entity entity, DrawEvent event, Vector2f screenPos, float fontSize, float padding, int color) {
        StreamerMode streamerMode = Delta.h().d().t().aE();
        Component name = entity.getName();
        if (streamerMode.m() && streamerMode.r().c().booleanValue()) {
            name = Component.literal(streamerMode.a(name.getString())).setStyle(name.getStyle());
        }
        MutableComponent display = name.copy().setStyle(name.getStyle().withColor(16777215));
        display.getSiblings().replaceAll(sibling -> sibling.copy().setStyle(sibling.getStyle().withColor(16777215)));
        MutableComponent text = (entity.getTeam() instanceof PlayerTeam team) ? team.getPlayerPrefix().copy().append(display) : display;
        if (entity instanceof LivingEntity living) {
            text.append(Component.literal(" " + ((int) ServerUtil.a.a(living))).setStyle(Style.EMPTY.withColor(16711680)));
        }
        float textWidth = Fonts.e.a(text.getString(), fontSize);
        float textHeight = Fonts.e.a(fontSize);
        float textX = screenPos.x() - (textWidth / 2.0f);
        float textY = screenPos.y();
        event.d().a(event.i(), textX - padding, textY, textWidth + (padding * 2.0f), textHeight, 0.0f, color);
        Fonts.e.a(event.i(), text, textX, textY, fontSize);
        a(entity, event, textWidth + (padding * 2.0f), textX - padding, textY, color, textHeight);
    }

    private void a(Entity entity, DrawEvent event, float nameTagWidth, float nameTagX, float nameTagY, int color, float textHeight) {
        if (entity instanceof Player player) {
            float spacing = textHeight * 0.3f;
            ItemStack[] stacks = {player.getMainHandItem(), player.getItemBySlot(EquipmentSlot.HEAD), player.getItemBySlot(EquipmentSlot.CHEST), player.getItemBySlot(EquipmentSlot.LEGS), player.getItemBySlot(EquipmentSlot.FEET), player.getOffhandItem()};
            int count = 0;
            for (ItemStack stack : stacks) {
                if (!stack.isEmpty()) {
                    count++;
                }
            }
            if (count > 0) {
                float x = nameTagX + ((nameTagWidth - ((count * textHeight) + ((count - 1) * spacing))) / 2.0f);
                float y = (nameTagY - textHeight) - spacing;
                for (ItemStack stack : stacks) {
                    if (!stack.isEmpty()) {
                        event.d().a(event.i(), x, y, textHeight, textHeight, 0.0f, color);
                        event.e().a(event.i(), stack, x, y, 0, 1.0f, textHeight / 16.0f, true);
                        x += textHeight + spacing;
                    }
                }
            }
        }
    }

    private void b(Entity entity, DrawEvent event, Vector2f screenPos, float fontSize, float padding, int color) {
        List<MobEffectInstance> effects;
        if (entity instanceof LivingEntity living) {
            List<a> trackers = new ArrayList<>();
            for (int i = this.c.size() - 1; i >= 0; i--) {
                a entry = this.c.get(i);
                if (entry.b() == living.getId()) {
                    if (living.tickCount < entry.c()) {
                        this.c.remove(i);
                    } else {
                        trackers.add(entry);
                    }
                }
            }
            if (!trackers.isEmpty()) {
                effects = new ArrayList<>();
                for (a tracker : trackers) {
                    for (MobEffectInstance effectInstance : tracker.a()) {
                        int remaining = effectInstance.getDuration() - Math.max(0, living.tickCount - tracker.c());
                        if (remaining > 0) {
                            MobEffectInstance remainingEffect = effectInstance.getDuration() > 1000000 ? effectInstance : new MobEffectInstance(effectInstance.getEffect(), remaining, effectInstance.getAmplifier());
                            MobEffectInstance existing = null;
                            for (MobEffectInstance instance : effects) {
                                if (instance.getEffect().equals(effectInstance.getEffect())) {
                                    existing = instance;
                                    break;
                                }
                            }
                            if (existing == null) {
                                effects.add(remainingEffect);
                            } else if (remainingEffect.getAmplifier() > existing.getAmplifier() || (remainingEffect.getAmplifier() == existing.getAmplifier() && remainingEffect.getDuration() > existing.getDuration())) {
                                effects.remove(existing);
                                effects.add(remainingEffect);
                            }
                        }
                    }
                }
                if (effects.isEmpty()) {
                    effects = new ArrayList<>(living.getActiveEffects());
                }
            } else {
                effects = new ArrayList<>(living.getActiveEffects());
            }
            float lineHeight = Fonts.e.a(fontSize);
            float maxWidth = 0.0f;
            for (MobEffectInstance effect : effects) {
                int seconds = effect.getDuration() / 20;
                String line = Component.translatable(((MobEffect) effect.getEffect().value()).getDescriptionId()).getString() + " " + MathUtil.a(effect.getAmplifier()) + (effect.getDuration() > 1000000 ? " ∞" : " - " + (seconds / 60) + ":" + String.format("%02d", Integer.valueOf(seconds % 60)));
                maxWidth = Math.max(maxWidth, Fonts.e.a(line, fontSize));
            }
            float textX = screenPos.x() - (maxWidth / 2.0f);
            float textY = screenPos.y() + padding;
            event.d().a(event.i(), textX - padding, textY, maxWidth + (padding * 2.0f), effects.size() * lineHeight, 0.0f, color);
            float lineY = textY;
            for (MobEffectInstance effect2 : effects) {
                String duration = effect2.getDuration() > 1000000 ? " ∞" : " - " + ((effect2.getDuration() / 20) / 60) + ":" + String.format("%02d", Integer.valueOf((effect2.getDuration() / 20) % 60));
                String line = Component.translatable(((MobEffect) effect2.getEffect().value()).getDescriptionId()).getString() + " " + MathUtil.a(effect2.getAmplifier()) + duration;
                Fonts.e.a(event.i(), line, screenPos.x() - (Fonts.e.a(line, fontSize) / 2.0f), lineY, fontSize, ColorUtil.a(((MobEffect) effect2.getEffect().value()).getColor(), 1.0f));
                lineY += lineHeight;
            }
        }
    }

    private void c(Entity entity, DrawEvent event, Vector2f screenPos, float fontSize, float padding, int color) {
        MutableComponent text = entity instanceof ItemEntity itemEntity ? itemEntity.getItem().getItemName().copy() : entity.getName().copy();
        if (entity instanceof ItemEntity item) {
            if (item.getItem().getCount() > 1) {
                text.append(Component.literal(" x" + item.getItem().getCount()));
            }
        }
        float textWidth = Fonts.e.a(text.getString(), fontSize);
        float textHeight = Fonts.e.a(fontSize);
        float textX = screenPos.x() - (textWidth / 2.0f);
        float textY = screenPos.y();
        event.d().a(event.i(), textX - padding, textY, textWidth + (padding * 2.0f), textHeight, 0.0f, color);
        Fonts.e.a(event.i(), text, textX, textY, fontSize);
    }

    public static final class a {
        private final List<MobEffectInstance> a;
        private final int b;
        private final int c;

        public a(List<MobEffectInstance> effects, int id, int age) {
            this.a = effects;
            this.b = id;
            this.c = age;
        }

        public List<MobEffectInstance> a() {
            return this.a;
        }

        public int b() {
            return this.b;
        }

        public int c() {
            return this.c;
        }
    }
}
