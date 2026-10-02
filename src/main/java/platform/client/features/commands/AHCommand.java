package platform.client.features.commands;

import platform.api.utils.auction.AutoBuyEntry;
import platform.client.utils.text.StringUtils;
import platform.api.utils.auction.AutoBuyProcessor;
import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.client.utils.text.ChatUtil;
import platform.client.utils.player.ServerUtil;

import platform.api.command.BaseCommand;
import platform.api.command.Command;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;

import platform.client.utils.timer.CounterUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.locale.Language;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;

@Command(a = "ah")
public class AHCommand extends BaseCommand implements Interface {
    private final List<a> c = new ArrayList();
    private Language d;
    private String e;
    private b f;

    @Override
    public void a(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.executes(context -> {
            ItemStack stack = aM_.player.getMainHandItem();
            if (!stack.isEmpty()) {
                aM_.player.connection.sendCommand("ah search " + a(stack));
                return 1;
            }
            return 1;
        }).then(a("sell").executes(context2 -> {
            a(0.0f);
            return 1;
        }).then(f("процент").executes(context3 -> {
            a(c(context3, "процент"));
            return 1;
        })));
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (this.f != null && this.e == null && event.c()) {
            ClientboundContainerSetContentPacket class_2649VarD = (ClientboundContainerSetContentPacket) event.d();
            if (class_2649VarD instanceof ClientboundContainerSetContentPacket) {
                ClientboundContainerSetContentPacket packet = class_2649VarD;
                if (packet.containerId() != 0) {
                    List<ItemStack> contents = packet.items();
                    List<Integer> prices = contents.subList(0, Math.max(0, contents.size() - 36)).stream().filter(stack -> {
                        return this.f.a().a(stack) && stack.getTooltipLines(Item.TooltipContext.EMPTY, aM_.player, TooltipFlag.NORMAL).stream().noneMatch(line -> {
                            return line.getString().contains("Нажмите, чтобы забрать");
                        });
                    }).mapToInt(ServerUtil.a::a).filter(price -> {
                        return price > 0;
                    }).sorted().boxed().toList();
                    if (!prices.isEmpty()) {
                        int reference = prices.get(Math.min(2, prices.size() - 1)).intValue();
                        int cheapest = prices.stream().filter(price2 -> {
                            return ((double) price2.intValue()) >= ((double) reference) * 0.75d;
                        }).findFirst().orElse(Integer.valueOf(reference)).intValue();
                        this.c.removeIf(entry -> {
                            return entry.a() == this.f.a();
                        });
                        this.c.add(new a(this.f.a(), cheapest, new CounterUtil()));
                        a(this.f, cheapest);
                    }
                    this.f = null;
                }
            }
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.e != null) {
            if (aM_.gui.screen() != null) {
                aM_.player.closeContainer();
            } else {
                aM_.player.connection.sendCommand(this.e);
                this.e = null;
            }
        }
    }

    private void a(float percent) {
        ItemStack stack = aM_.player.getMainHandItem();
        AutoBuyEntry item = Delta.h().d().q().e().stream().filter(info -> {
            return info.a(stack);
        }).findFirst().orElse(null);
        if (item == null) {
            ChatUtil.a((Object) "Авто-продажа недоступна для обычных предметов — только для донатных");
            return;
        }
        b request = new b(item, stack.getCount(), percent);
        a cached = this.c.stream().filter(entry -> {
            return entry.a() == item && !entry.c().a(15000L);
        }).findFirst().orElse(null);
        if (cached != null) {
            a(request, cached.b());
        } else {
            aM_.player.connection.sendCommand("ah search " + a(stack));
            this.f = request;
        }
    }

    private void a(b sell, int cheapest) {
        long price = Math.max(1L, Math.round(((double) cheapest) * (1.0d - (((double) sell.c()) / 100.0d)) * ((double) Math.max(1, sell.b()))));
        String strB = sell.a().b();
        ChatUtil.a((Object) ("Выставляю &c" + strB + " &7за &c" + price + " &7(-" + strB + "% от " + ((int) sell.c()) + ")"));
        this.e = "ah sell " + price;
    }

    private String a(ItemStack stack) {
        this.d = this.d == null ? Language.getInstance() : this.d;
        String name = (String) Delta.h().d().q().e().stream().filter(item -> {
            return item.a(stack);
        }).findFirst().map((v0) -> {
            return v0.b();
        }).orElse(this.d.getOrDefault(stack.getItem().getDescriptionId(), stack.getItem().getDescriptionId()));
        return name.replaceAll("\\[\\d+x\\d+]", "").replace("⚡", "").replace("xxx", "").replace("[", "").replace("]", "").replace("★", "").trim().replaceAll("\\s+", StringUtils.a);
    }

    static final class b {
        private final AutoBuyEntry a;
        private final int b;
        private final float c;

        b(AutoBuyEntry item, int count, float percent) {
            this.a = item;
            this.b = count;
            this.c = percent;
        }
public AutoBuyEntry a() {
            return this.a;
        }

        public int b() {
            return this.b;
        }

        public float c() {
            return this.c;
        }
    }

    static final class a {
        private final AutoBuyEntry a;
        private final int b;
        private final CounterUtil c;

        a(AutoBuyEntry item, int price, CounterUtil timer) {
            this.a = item;
            this.b = price;
            this.c = timer;
        }
public AutoBuyEntry a() {
            return this.a;
        }

        public int b() {
            return this.b;
        }

        public CounterUtil c() {
            return this.c;
        }
    }
}



