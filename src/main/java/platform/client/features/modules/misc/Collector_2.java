package platform.client.features.modules.misc;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.text.ChatUtil;
import platform.client.utils.math.MathUtil;
import platform.client.utils.player.ServerUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.other.ContainerEvent;
import platform.api.event.events.player.InputEvent;
import platform.api.event.events.player.KeyEvent;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;
import platform.client.ui.screen.StationScreen;

import platform.client.utils.render.AnimationUtil;
import platform.api.module.setting.ButtonSetting;
import platform.client.utils.timer.CounterUtil;
import platform.api.system.configs.DescriptionProcessor;
import platform.api.system.configs.EnchantmentProcessor;
import platform.api.system.configs.PotionProcessor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import lombok.Generated;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.core.component.DataComponents;

@ModuleRegister(a = "Collector", b = "Автоматически собирает нужный инвентарь на FunTime", c = Category.Misc)
public class Collector_2 extends Module {
    private final ButtonSetting b = new ButtonSetting("Запустить работу", () -> {
        if (!m()) {
            a();
        }
        r();
    });
    private final ButtonSetting c = new ButtonSetting("Открыть редактор", () -> {
        aM_.gui.setScreen(new StationScreen(Component.literal(""), 0));
    });
    private final List<b> d = Delta.h().d().p().e();
    private final List<a> e = new ArrayList();
    private final CounterUtil f = new CounterUtil();
    private final CounterUtil g = new CounterUtil();
    private b h;
    private a i;
    private int j;

    @Generated
    public List<b> q() {
        return this.d;
    }

    public Collector_2() {
        a(this.b, this.c);
    }

    @Override
    public void b() {
        super.b();
        ChatUtil.a((Object) "Модуль ищет самые дешевые лоты среди тех которые есть, имейте это ввиду, и будьте осторожны!");
    }

    private void r() {
        int start = this.h == null ? 0 : this.d.indexOf(this.h) + 1;
        this.i = null;
        this.e.clear();
        IntStream intStreamRange = IntStream.range(start, this.d.size());
        List<b> list = this.d;
        Objects.requireNonNull(list);
        this.h = (b) intStreamRange.mapToObj(list::get).filter((v0) -> {
            return v0.k();
        }).filter(slot -> {
            return a(slot) < a(slot, false);
        }).findFirst().orElse(null);
        if (this.h != null) {
            ChatUtil.a((Object) ("Переходим к сбору предмета: " + this.h.j()));
        } else {
            ChatUtil.a((Object) "Все предметы собраны, работа завершена");
        }
    }

    @Override
    public void c() {
        super.c();
        this.h = null;
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.h != null) {
            Inventory inv = aM_.player.getInventory();
            boolean hasEmpty = IntStream.range(0, inv.getContainerSize()).anyMatch(idx -> inv.getItem(idx).isEmpty());

            boolean noEmpty = IntStream.range(0, 36).noneMatch(idx -> inv.getItem(idx).isEmpty());
            if (noEmpty) {
                ChatUtil.a((Object) "Автоматическое отключение: нет свободных слотов в инвентаре, освободите место");
                s();
                a();
                return;
            }
            this.j++;
            if (!(aM_.gui.screen() instanceof ContainerScreen) && aM_.player.tickCount >= 200 && this.f.a(1000L, 300L)) {
                aM_.player.connection.sendCommand("ah search " + this.h.j());
                this.f.b();
                this.j = 0;
            }
        }
    }

    @EventTarget
    public void a(ContainerEvent event) {
        if (this.h != null && event.h() != ContainerEvent.Phase.PRE) {
            String title = event.b().getTitle().getString().replaceAll("§.", "").toLowerCase().trim();
            if (title.contains(this.h.j().toLowerCase())) {
                if (this.f.a(300L, 80L) && a(this.h) >= a(this.h, false)) {
                    ChatUtil.a((Object) ("Предмет " + this.h.j() + " приобретен, перехожу к следующему"));
                    r();
                    s();
                    return;
                }
                if (this.g.a(1000L, 150L)) {
                    if (this.f.a(this.h.l() ? 400L : 550L, 120L)) {
                        if (this.h.l()) {
                            Matcher matcher = Pattern.compile("(\\d+)/(\\d+)").matcher(title);
                            if (matcher.find()) {
                                int currentPage = Integer.parseInt(matcher.group(1));
                                int lastScanPage = Math.min(4, Integer.parseInt(matcher.group(2)));
                                if (this.i == null) {
                                    if (this.e.stream().noneMatch(offer -> {
                                        return offer.a() == currentPage;
                                    })) {
                                        event.e().stream().filter(slot -> {
                                            return a(slot.getItem());
                                        }).forEach(slot2 -> {
                                            this.e.add(new a(currentPage, slot2.index, ServerUtil.a.a(slot2.getItem())));
                                        });
                                    }
                                    if (currentPage < lastScanPage) {
                                        a(event, "следующая страница");
                                    } else {
                                        this.i = this.e.stream().min(Comparator.comparingInt((v0) -> {
                                            return v0.c();
                                        })).orElse(null);
                                        if (this.i == null) {
                                            s();
                                        }
                                    }
                                } else if (currentPage == this.i.a()) {
                                    Slot offer2 = event.e().stream().filter(slot3 -> {
                                        return a(slot3.getItem()) && ServerUtil.a.a(slot3.getItem()) == this.i.c();
                                    }).findFirst().orElse(null);
                                    if (offer2 == null) {
                                        offer2 = event.e().stream().filter(slot4 -> {
                                            return a(slot4.getItem());
                                        }).min(Comparator.comparingInt(slot5 -> {
                                            return ServerUtil.a.a(slot5.getItem());
                                        })).orElse(null);
                                    }
                                    if (offer2 != null) {
                                        aM_.gameMode.handleContainerInput(event.c().containerId, offer2.index, 0, ContainerInput.QUICK_MOVE, aM_.player);
                                        this.f.b();
                                    } else {
                                        ChatUtil.a((Object) "Оффер пропал, пересканирую");
                                        this.i = null;
                                        this.e.clear();
                                        s();
                                    }
                                } else {
                                    a(event, currentPage < this.i.a() ? "следующая страница" : "предыдущая страница");
                                }
                            }
                        } else {
                            List<Slot> buyable = event.e().stream().filter(slot6 -> {
                                return a(slot6.getItem());
                            }).toList();
                            int minPrice = buyable.stream().mapToInt(slot7 -> {
                                return ServerUtil.a.a(slot7.getItem());
                            }).min().orElse(0);
                            List<Slot> affordable = buyable.stream().filter(slot8 -> {
                                return ServerUtil.a.a(slot8.getItem()) <= Math.round((float) (minPrice * 2));
                            }).filter(slot9 -> {
                                return slot9.getItem().getCount() <= a(this.h, true) - a(this.h);
                            }).toList();
                            Slot cheapest = affordable.stream().filter(slot10 -> {
                                return slot10.getItem().getCount() >= a(this.h, false) - a(this.h);
                            }).min(Comparator.comparingInt(slot11 -> {
                                return ServerUtil.a.a(slot11.getItem());
                            })).orElse(null);
                            if (cheapest == null) {
                                cheapest = affordable.stream().min(Comparator.comparingInt(slot12 -> {
                                    return ServerUtil.a.a(slot12.getItem());
                                })).orElse(null);
                            }
                            if (cheapest != null) {
                                aM_.gameMode.handleContainerInput(event.c().containerId, cheapest.index, 0, ContainerInput.QUICK_MOVE, aM_.player);
                                this.f.b();
                            } else {
                                Matcher matcher2 = Pattern.compile("(\\d+)/(\\d+)").matcher(title);
                                if (matcher2.find()) {
                                    int currentPage2 = Integer.parseInt(matcher2.group(1));
                                    int totalPages = Integer.parseInt(matcher2.group(2));
                                    if (totalPages == 1) {
                                        ChatUtil.a((Object) ("Пропускаем предмет " + this.h.j() + ", ибо нету подходящего"));
                                        r();
                                        s();
                                        return;
                                    }
                                    a(event, currentPage2 == 1 ? "следующая страница" : "предыдущая страница");
                                }
                            }
                        }
                        this.f.b();
                        return;
                    }
                    return;
                }
                return;
            }
            if (title.contains("подтверждение покупки") || title.contains("подозрительная цена!") || title.contains("подозрительная цена: ")) {
                if (this.f.a(200L, 90L)) {
                    a(event, "[Кyпить]");
                    this.f.b();
                    return;
                }
                return;
            }
            s();
        }
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (this.h != null && event.c()) {
            String message = null;
            if (event.d() instanceof ClientboundSystemChatPacket sys) {
                message = sys.content().getString();
            } else if (event.d() instanceof ClientboundDisguisedChatPacket dis) {
                message = dis.message().getString();
            }
            if (message != null) {
                if (message.toLowerCase().contains("[✘] Ошибка! Этот товар уже Купили!")) {
                    this.i = null;
                    this.e.clear();
                    return;
                } else if (message.contains("[✘] Ошибка! У Вас не хватает Монет!")) {
                    ChatUtil.a((Object) "Автоматическое отключение из-за нехватки баланса на аккаунте");
                    a();
                    return;
                } else if (message.contains("Данная команда недоступна в режиме AFK")) {
                    Delta.h().d().v().g().a(10);
                }
            }
            if ((event.d() instanceof ClientboundOpenScreenPacket) && !(aM_.gui.screen() instanceof ContainerScreen)) {
                if (this.j >= 8) {
                    int anarchy = (int) (MathUtil.a(0.0f, 100.0f) <= 50.0f ? MathUtil.a(205.0f, 231.0f) : MathUtil.a(305.0f, 325.0f));
                    aM_.player.connection.sendCommand("an" + anarchy);
                    ChatUtil.a((Object) ("Обнаружили замедление аукциона, переходим на " + anarchy + " анархию"));
                }
                this.g.b();
            }
        }
    }

    @EventTarget
    public void a(InputEvent event) {
        if (this.h != null && (aM_.gui.screen() instanceof AbstractContainerScreen)) {
            event.a(0.0f);
            event.b(0.0f);
        }
    }

    @EventTarget
    public void a(KeyEvent event) {
        if (this.h != null && (aM_.gui.screen() instanceof AbstractContainerScreen)) {
            event.a(true);
        }
        if (event.b() == 256 && this.h != null) {
            this.h = null;
            ChatUtil.a((Object) "Работа модуля была принудительно завершена");
        }
    }

    private void a(ContainerEvent event, String name) {
        event.e().stream().filter(slot -> {
            return slot.getItem().getHoverName().getString().toLowerCase().contains(name.toLowerCase());
        }).findFirst().ifPresent(slot2 -> {
            aM_.gameMode.handleContainerInput(event.c().containerId, slot2.index, 0, ContainerInput.QUICK_MOVE, aM_.player);
        });
    }

    private void s() {
        if (aM_.gui.screen() instanceof ContainerScreen) {
            aM_.player.closeContainer();
        }
    }

    private boolean a(ItemStack stack) {
        if (stack.isEmpty() || !this.h.a(stack)) {
            return false;
        }
        Inventory inv = aM_.player.getInventory();
        ItemStack inventory = IntStream.range(0, inv.getContainerSize()).mapToObj(inv::getItem).filter(s -> {
            return !s.isEmpty() && this.h.a(s);
        }).findFirst().orElse(ItemStack.EMPTY);
        if ((!inventory.isEmpty() && (((this.h.i() instanceof PotionItem) && !Objects.equals(stack.get(DataComponents.POTION_CONTENTS), inventory.get(DataComponents.POTION_CONTENTS))) || !stack.getHoverName().getString().trim().equalsIgnoreCase(inventory.getHoverName().getString().trim()))) || stack.getTooltipLines(Item.TooltipContext.EMPTY, aM_.player, TooltipFlag.NORMAL).stream().anyMatch(line -> {
            return line.getString().contains("➥ Нажмите, чтобы забрать");
        }) || ServerUtil.a.a(stack) <= 0) {
            return false;
        }
        if (this.h.i() == Items.TOTEM_OF_UNDYING && stack.hasFoil()) {
            return false;
        }
        if (this.h.i() == Items.ELYTRA && stack.get(DataComponents.DAMAGE) != null) {
            return ((float) (432 - ((Integer) stack.get(DataComponents.DAMAGE)).intValue())) / 432.0f >= 0.5f;
        }
        if (!Interface.isHumanoidArmor(new ItemStack(this.h.i())) || stack.get(DataComponents.DAMAGE) == null || stack.getMaxDamage() <= 0) {
            return true;
        }
        return !(this.h.f() == null || this.h.f().b().stream().noneMatch(condition -> {
            return condition.b() && !condition.d() && condition.i().equals(Enchantments.MENDING);
        })) || ((float) (stack.getMaxDamage() - ((Integer) stack.get(DataComponents.DAMAGE)).intValue())) / ((float) stack.getMaxDamage()) >= 0.7f;
    }

    private int a(b info, boolean upper) {
        return Math.max(1, upper ? info.m() + Math.round(info.m() * 0.2f) : info.m());
    }

    private int a(b slot) {
        Inventory inv = aM_.player.getInventory();
        return IntStream.range(0, inv.getContainerSize()).mapToObj(inv::getItem).filter(stack -> {
            return !stack.isEmpty() && slot.a(stack);
        }).mapToInt((v0) -> {
            return v0.getCount();
        }).sum();
    }

    public static class b {
        private DescriptionProcessor a;
        private EnchantmentProcessor b;
        private PotionProcessor c;
        private final AnimationUtil d = new AnimationUtil();
        private final Item e;
        private final String f;
        private boolean g;
        private boolean h;
        private int i;
        private int j;

        @Generated
        public void b(int count) {
            this.i = count;
        }

        @Generated
        public DescriptionProcessor e() {
            return this.a;
        }

        @Generated
        public EnchantmentProcessor f() {
            return this.b;
        }

        @Generated
        public PotionProcessor g() {
            return this.c;
        }

        @Generated
        public AnimationUtil h() {
            return this.d;
        }

        @Generated
        public Item i() {
            return this.e;
        }

        @Generated
        public String j() {
            return this.f;
        }

        @Generated
        public boolean k() {
            return this.g;
        }

        @Generated
        public boolean l() {
            return this.h;
        }

        @Generated
        public int m() {
            return this.i;
        }

        @Generated
        public int n() {
            return this.j;
        }

        private b(Item item, int count, String name) {
            this.e = item;
            this.f = name;
            this.i = count;
        }

        public static b a(Item item, int count, String name) {
            return new b(item, count, name);
        }

        public boolean a() {
            return this.e.getDefaultMaxStackSize() > 1 || this.e == Items.TOTEM_OF_UNDYING || (this.e instanceof SplashPotionItem) || (this.e instanceof PotionItem);
        }

        public int b() {
            if (this.e instanceof PotionItem) {
                return 16;
            }
            if (this.e == Items.TOTEM_OF_UNDYING || (this.e instanceof SplashPotionItem)) {
                return 6;
            }
            return this.e.getDefaultMaxStackSize();
        }

        public b a(EnchantmentProcessor processor) {
            this.b = processor;
            return this;
        }

        public b a(DescriptionProcessor processor) {
            this.a = processor;
            return this;
        }

        public b a(PotionProcessor processor) {
            this.c = processor;
            return this;
        }

        public b a(boolean active) {
            this.g = active;
            return this;
        }

        public b b(boolean scan) {
            this.h = scan;
            return this;
        }

        public b a(int color) {
            this.j = color;
            return this;
        }

        public boolean a(ItemStack stack) {
            return stack.is(this.e) && (this.a == null || this.a.a(stack)) && ((this.b == null || this.b.a(stack)) && (this.c == null || this.c.a(stack)));
        }

        public ItemStack c() {
            ItemStack stack = new ItemStack(this.e, this.i);
            if (this.j != 0) {
                List<MobEffectInstance> effects = this.c == null ? List.of() : this.c.a().stream().map(c -> {
                    return new MobEffectInstance(c.a(), c.c(), Math.max(0, c.b() - 1));
                }).toList();
                stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(Integer.valueOf(this.j)), effects, Optional.empty()));
            }
            return stack;
        }

        public b d() {
            return a(this.e, this.i, this.f).a(this.a).a(this.b).a(this.c).a(this.g).b(this.h).a(this.j);
        }
    }

    static final class a {
        private final int a;
        private final int b;
        private final int c;

        a(int page, int slotId, int price) {
            this.a = page;
            this.b = slotId;
            this.c = price;
        }
 public int a() {
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
