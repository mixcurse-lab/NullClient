package platform.client.features.modules.combat;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.player.InventoryUtil;

import platform.api.module.Category;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;

import platform.api.module.setting.BindSetting;
import java.util.List;
import lombok.Generated;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

@ModuleRegister(a = "Item Helper", b = "Перемещает нужный предмет и возвращает его обратно по нажатию клавиши", c = Category.Combat)
public class ItemHelper extends Module implements Interface {
    public ItemHelper() {
        List.of(new a(this, "Зачарованное яблоко", Items.ENCHANTED_GOLDEN_APPLE), new a(this, "Золотое яблоко", Items.GOLDEN_APPLE), new a(this, "Плод хоруса", Items.CHORUS_FRUIT), new a(this, "Арбалет", Items.CROSSBOW)).forEach(item -> {
            a(item.b());
        });
    }

    final class a {
        private final Item a;
        private final BindSetting b;
        private int c = -1;
        private int d = -1;

        @Generated
        public Item a() {
            return this.a;
        }

        @Generated
        public BindSetting b() {
            return this.b;
        }

        @Generated
        public int c() {
            return this.c;
        }

        @Generated
        public int d() {
            return this.d;
        }

        a(ItemHelper itemHelper, String name, Item item) {
            this.a = item;
            this.b = new BindSetting(name, -1, 1).a(this::e);
        }

        private void e() {
            if (this.d == -1) {
                f();
            } else {
                g();
            }
        }

        private void f() {
            if (this.d == -1) {
                int from = InventoryUtil.b(this.a);
                int target = Interface.aM_.player.getInventory().getSelectedSlot();
                if (from != -1 && from != target) {
                    this.d = from;
                    this.c = target;
                    Delta.h().d().v().a().a(from, target, 1);
                }
            }
        }

        private void g() {
            if (this.d != -1) {
                Delta.h().d().v().a().a(this.d, this.c, 1);
                this.d = -1;
                this.c = -1;
            }
        }
    }
}


