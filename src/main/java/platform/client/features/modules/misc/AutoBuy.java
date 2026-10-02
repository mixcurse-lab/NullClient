package platform.client.features.modules.misc;

import platform.api.utils.auction.AutoBuyEntry;
import platform.api.utils.auction.AutoBuyProcessor;
import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.text.ChatUtil;
import platform.client.utils.math.MathUtil;
import platform.client.utils.player.MoveUtil;
import platform.client.utils.player.ServerUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.other.ContainerEvent;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;
import platform.client.ui.screen.StationScreen;

import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.ButtonSetting;
import platform.client.utils.timer.CounterUtil;
import platform.client.utils.text.DateUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Generated;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import platform.inject.accessors.HandledScreenAccessor;

@ModuleRegister(a = "Auto Buy", b = "Автоматически скупает выбранные предметы по заданной цене", c = Category.Misc)
public class AutoBuy extends Module implements Interface {
    private int f;
    private int g;
    private ItemStack i;
    private boolean j;
    private boolean k;
    private final BooleanSetting b = new BooleanSetting("Авто-перевыставление вещей", false);
    private final ButtonSetting c = new ButtonSetting("Открыть редактор", () -> {
        aM_.gui.setScreen(new StationScreen(Component.literal(""), 1));
    });
    private final List<ItemStack> d = new ArrayList();
    private final CounterUtil e = new CounterUtil();
    private int h = -1;

    @Generated
    public boolean q() {
        return this.j;
    }

    @Generated
    public boolean r() {
        return this.k;
    }

    @Generated
    public void d(boolean status) {
        this.j = status;
    }

    @Generated
    public void e(boolean ah) {
        this.k = ah;
    }

    public AutoBuy() {
        a(this.c, this.b);
    }

    @EventTarget
    public void a(TickEvent event) {
        if (aM_.player.tickCount >= 220 && this.k && !(aM_.gui.screen() instanceof ContainerScreen) && this.j && aM_.player.tickCount % 20 == 0) {
            aM_.player.connection.sendCommand("ah");
            this.k = false;
        }
        this.f++;
        this.g++;
        ContainerScreen class_476Var = (ContainerScreen) aM_.gui.screen();
        if (class_476Var instanceof ContainerScreen) {
            ContainerScreen screen = class_476Var;
            if (this.j) {
                AbstractContainerMenu handler = screen.getMenu();
                String title = screen.getTitle().getString().replaceAll("§.", "").toLowerCase().trim();
                boolean buy = aM_.player.tickCount % 2 == 0;
                boolean reissue = this.b.c().booleanValue() && this.e.a(DateUtils.b);
                if (title.contains("аукцион")) {
                    boolean found = false;
                    for (Slot slot : handler.slots.subList(0, Math.min(45, handler.slots.size()))) {
                        ItemStack stack = slot.getItem();
                        ItemContainerContents shulker = (ItemContainerContents) stack.get(DataComponents.CONTAINER);
                        AutoBuyEntry find = Delta.h().d().q().e().stream().filter(item -> {
                            if (item.l()) {
                                if (item.a(stack)) {
                                    if (ServerUtil.a.a(stack) > item.k() && ((long) ServerUtil.a.a(stack)) * ((long) Math.max(stack.getCount(), 1)) <= ServerUtil.a.e()) {
                                        return true;
                                    }
                                } else if (shulker != null) {
                                    Stream<ItemStack> streamMethod_57489 = shulker.nonEmptyItemCopyStream();
                                    Objects.requireNonNull(item);
                                    if (streamMethod_57489.anyMatch(innerStack -> item.a(innerStack))) {
                                        if (ServerUtil.a.a(stack) > item.k()) {
                                        }
                                    }
                                }
                            }
                            return false;
                        }).findFirst().orElse(null);
                        if (find != null && buy) {
                            found = true;
                            this.i = stack.copy();
                            a(handler, slot.index, ContainerInput.QUICK_MOVE);
                            break;
                        }
                    }
                    if (!found && !reissue && this.h == handler.containerId) {
                        a(handler, 49, MathUtil.a(0.0f, 100.0f) < 25.0f ? ContainerInput.QUICK_MOVE : ContainerInput.PICKUP);
                        this.h = -1;
                    }
                } else if ((title.contains("подтверждение покупки") || title.contains("подозрительная цена!") || title.contains("подозрительная цена: ")) && buy) {
                    a(handler, 1, ContainerInput.QUICK_MOVE);
                }
                if (this.b.c().booleanValue() && reissue) {
                    AbstractContainerScreen<?> class_465Var = (AbstractContainerScreen<?>) aM_.gui.screen();
                    if (class_465Var instanceof AbstractContainerScreen) {
                        AbstractContainerScreen<?> handledScreen = class_465Var;
                        if ((handledScreen instanceof ContainerScreen) && !MoveUtil.a()) {
                            if (title.matches(".*а.*у.*к.*ц.*и.*о.*н.*")) {
                                if (aM_.player.tickCount % 10 == 0) {
                                    a(handledScreen.getMenu(), 46, ContainerInput.PICKUP);
                                    this.g = 0;
                                }
                            } else if (title.matches(".*х.*р.*а.*н.*и.*л.*и.*щ.*е.*")) {
                                if (this.g % 20 == 10) {
                                    a(handledScreen.getMenu(), 52, ContainerInput.PICKUP);
                                } else if (this.g % 20 == 0 && this.g > 0) {
                                    a(handledScreen.getMenu(), 46, ContainerInput.PICKUP);
                                    this.e.b();
                                }
                            }
                            this.f = 0;
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (event.c()) {
            if (this.j) {
                if (event.d() instanceof ClientboundContainerSetContentPacket) {
                    ClientboundContainerSetContentPacket s2CPacket = (ClientboundContainerSetContentPacket) event.d();
                    if (s2CPacket.items().size() == 90 && this.f >= 7) {
                        int anarchy = (int) (MathUtil.a(0.0f, 100.0f) <= 50.0f ? MathUtil.a(205.0f, 231.0f) : MathUtil.a(305.0f, 325.0f));
                        aM_.player.connection.sendCommand("an" + anarchy);
                        ChatUtil.a((Object) ("Обнаружили замедление аукциона, переходим на " + anarchy + " анархию"));
                        this.f = 0;
                        this.k = true;
                    }
                }
                String msg = null;
                if (event.d() instanceof ClientboundSystemChatPacket) {
                    msg = ((ClientboundSystemChatPacket) event.d()).content().getString();
                } else if (event.d() instanceof ClientboundDisguisedChatPacket) {
                    msg = ((ClientboundDisguisedChatPacket) event.d()).message().getString();
                }
                if (msg != null && this.i != null && msg.contains("Вы успешно купили")) {
                    if (this.d.isEmpty() || !ItemStack.isSameItemSameComponents((ItemStack) this.d.getFirst(), this.i)) {

                        ChatUtil.a((Object) ("Успешно куплен предмет &c" + this.i.getHoverName().getString() + " &7за &c" + ServerUtil.a.a(this.i)));
                        this.d.addFirst(this.i);
                    }
                    this.i = null;
                }
            }
            if (event.d() instanceof ClientboundOpenScreenPacket) {
                ClientboundOpenScreenPacket openScreenS2CPacket = (ClientboundOpenScreenPacket) event.d();
                if (!(aM_.gui.screen() instanceof ContainerScreen)) {
                    this.f = 0;
                }
                this.h = openScreenS2CPacket.getContainerId();
            }
            if (event.d() instanceof ClientboundSoundPacket) {
                ClientboundSoundPacket s2CPacket3 = (ClientboundSoundPacket) event.d();
                if (((SoundEvent) s2CPacket3.getSound().value()).location().getPath().equals("block.note_block.basedrum")) {
                    this.h = aM_.player.containerMenu.containerId;
                    event.a(true);
                }
            }
        }
    }

    @EventTarget
    public void a(ContainerEvent event) {
        if (event.h() == ContainerEvent.Phase.POST) {
            String title = event.b().getTitle().getString().replaceAll("§.", "").toLowerCase().trim();
            if (title.contains("аукцион")) {
                GuiGraphicsExtractor context = event.d();
                if (context == null) return;
                HandledScreenAccessor accessor = (HandledScreenAccessor) event.b();
                int count = accessor.getBackgroundHeight() / 18;
                int x = accessor.getX() - 22;
                int y = accessor.getY() + 3;
                int bottom = y + (count * 18);
                int[][] edges = {new int[]{x - 2, y, x + 20, bottom, -3750202}, new int[]{x, y - 2, x + 18, bottom + 2, -3750202}, new int[]{x - 1, y - 1, x + 19, y, -3750202}, new int[]{x - 1, bottom, x + 19, bottom + 1, -3750202}, new int[]{x, y - 2, x + 18, y - 1, -1}, new int[]{x - 1, y - 1, x, y, -1}, new int[]{x - 2, y, x - 1, bottom, -1}, new int[]{x, bottom + 1, x + 18, bottom + 2, -11184811}, new int[]{x + 18, bottom, x + 19, bottom + 1, -11184811}, new int[]{x + 19, y, x + 20, bottom, -11184811}};
                for (int[] edge : edges) {
                    context.fill(edge[0], edge[1], edge[2], edge[3], edge[4]);
                }
                for (int i = 0; i < count; i++) {
                    int slotY = y + (i * 18);
                    context.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("container/slot"), x, slotY, 18, 18);
                    if (i < this.d.size()) {
                        ItemStack stack = this.d.get(i);
                        Delta.h().d().j().a(context, stack, x + 1, slotY + 1, 0, 1.0f, 1.0f, true);
                        if (MathUtil.a(event.f(), event.g(), x + 1, slotY + 1, 16.0f, 16.0f)) {
                            context.fill(x + 1, slotY + 1, x + 17, slotY + 17, -2130706433);
                            java.util.List<Component> lines = stack.getTooltipLines(Item.TooltipContext.EMPTY, aM_.player, TooltipFlag.NORMAL);
                            try {
                                context.setTooltipForNextFrame(aM_.font, lines, java.util.Optional.empty(), event.f(), event.g());
                            } catch (Throwable ignored) {
                            }
                        }
                    }
                }
            }
        }
    }

    private void a(AbstractContainerMenu handler, int slot, ContainerInput action) {
        aM_.gameMode.handleContainerInput(handler.containerId, slot, 0, action, aM_.player);
        this.f = 0;
    }
}
