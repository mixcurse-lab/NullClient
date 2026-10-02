package platform.client.utils.player;

import static platform.api.module.Interface.aM_;

import platform.api.event.EventManager;
import platform.api.event.interfaces.EventTarget;
import platform.api.event.events.client.TickEvent;
import platform.client.Delta;

import java.util.function.Predicate;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * Утилита использования предметов (зелья, еда и т.п.).
 * Находит предмет по условию, подменяет его в основную руку (выбором слота хотбара
 * или свапом из инвентаря), использует с проверкой результата и возвращает прежний слот.
 *
 * За раз выполняется только одна задача: повторные вызовы во время работы игнорируются,
 * поэтому дёрганья слотов и спам пакетов использования невозможны.
 */
public class UseItemUtil {
    private static final UseItemUtil b = new UseItemUtil();

    private static final int ITEM_TIMEOUT = 60;
    private static final int USE_TIMEOUT = 80;
    private static final int CONSUME_TIMEOUT = 140;
    private static final int RESTORE_TIMEOUT = 20;

    private LocalPlayer owner;
    private ItemStack expected = ItemStack.EMPTY;
    private int slot = -1;
    private int restoreSlot = -1;
    private boolean usedSwap;
    private boolean swapSent;
    private int phase;
    private int timer;
    private static boolean useKeyDown;

    private UseItemUtil() {
        EventManager.a(this);
    }

    /** Занята ли утилита использованием предмета. */
    public static boolean b() {
        return b.phase != 0;
    }

    /**
     * Использовать первый подходящий предмет (слоты хотбара в приоритете).
     * Возвращает false, если утилита занята, игрок уже что-то использует или предмет не найден.
     */
    public static boolean a(Predicate<ItemStack> match) {
        if (b.phase != 0 || aM_.player == null || aM_.level == null || aM_.gameMode == null) {
            return false;
        }
        if (aM_.player.isUsingItem() || Delta.h().d().v().k().a() || !Delta.h().d().v().a().a().isEmpty()) {
            return false;
        }
        int found = -1;
        boolean hotbar = true;
        for (int i = 0; i < 9 && found < 0; i++) {
            if (match.test(aM_.player.getInventory().getItem(i))) {
                found = i;
            }
        }
        if (found < 0) {
            for (int i = 9; i < 36 && found < 0; i++) {
                if (match.test(aM_.player.getInventory().getItem(i))) {
                    found = i;
                    hotbar = false;
                }
            }
        }
        if (found < 0) {
            return false;
        }
        b.owner = aM_.player;
        b.expected = aM_.player.getInventory().getItem(found).copy();
        b.slot = found;
        b.restoreSlot = aM_.player.getInventory().getSelectedSlot();
        b.usedSwap = !hotbar;
        b.swapSent = false;
        b.phase = 1;
        b.timer = 0;
        return true;
    }

    /** Сбросить состояние без возврата предмета (выключение модуля, смена мира/сервера). */
    public static void c() {
        releaseUseKey();
        b.owner = null;
        b.expected = ItemStack.EMPTY;
        b.slot = -1;
        b.phase = 0;
    }

    /** Имитация зажатой ПКМ: без этого ванилла сама отменяет потребление на следующем тике. */
    private static void pressUseKey() {
        aM_.options.keyUse.setDown(true);
        useKeyDown = true;
    }

    private static void releaseUseKey() {
        if (useKeyDown) {
            aM_.options.keyUse.setDown(false);
            useKeyDown = false;
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        if (b.phase == 0 || aM_.player == null || aM_.level == null || aM_.gameMode == null) {
            return;
        }
        if (aM_.player != b.owner) {
            c();
            return;
        }
        b.timer++;
        switch (b.phase) {
            case 1 -> g();
            case 2 -> h();
            case 3 -> i();
            case 4 -> j();
        }
    }

    /** Фаза 1: добиваемся, чтобы нужный предмет оказался в основной руке. */
    private void g() {
        if (f()) {
            b.phase = 2;
            b.timer = 0;
            return;
        }
        if (b.timer > ITEM_TIMEOUT) {
            l();
            return;
        }
        if (b.usedSwap) {
            if (!b.swapSent) {
                Delta.h().d().v().a().a(b.slot, b.restoreSlot, 2);
                b.swapSent = true;
            }
        } else {
            aM_.player.getInventory().setSelectedSlot(b.slot);
        }
    }

    /** Фаза 2: использование. Шлём пакет использования не чаще раза в 6 тиков и ждём подтверждения. */
    private void h() {
        pressUseKey();
        if (aM_.player.isUsingItem()) {
            b.phase = 3;
            b.timer = 0;
            return;
        }
        if (b.timer > USE_TIMEOUT) {
            l();
            return;
        }
        if (!f()) {
            l();
            return;
        }
        if (b.timer % 6 == 1) {
            aM_.gameMode.useItem(aM_.player, InteractionHand.MAIN_HAND);
        }
    }

    /** Фаза 3: предмет используется — ждём завершения потребления. */
    private void i() {
        pressUseKey();
        if (!aM_.player.isUsingItem()) {
            b.phase = 4;
            b.timer = 0;
            return;
        }
        if (b.timer > CONSUME_TIMEOUT) {
            l();
        }
    }

    /** Фаза 4: возвращаем прежний слот. */
    private void j() {
        if (b.usedSwap) {
            if (b.timer <= RESTORE_TIMEOUT && !Delta.h().d().v().a().a().isEmpty()) {
                return;
            }
            if (Delta.h().d().v().a().a().isEmpty()) {
                Delta.h().d().v().a().a(b.restoreSlot, b.slot, 2);
            }
        } else {
            aM_.player.getInventory().setSelectedSlot(b.restoreSlot);
        }
        k();
    }

    /** Предмет уже в основной руке? */
    private boolean f() {
        return ItemStack.isSameItemSameComponents(aM_.player.getMainHandItem(), b.expected);
    }

    private void k() {
        c();
    }

    /** Не удалось использовать — откатываем руку и сбрасываем состояние. */
    private void l() {
        releaseUseKey();
        if (aM_.player.isUsingItem()) {
            aM_.player.releaseUsingItem();
        }
        if (b.usedSwap && b.swapSent && Delta.h().d().v().a().a().isEmpty()) {
            Delta.h().d().v().a().a(b.restoreSlot, b.slot, 2);
        } else if (!b.usedSwap) {
            aM_.player.getInventory().setSelectedSlot(b.restoreSlot);
        }
        k();
    }
}
