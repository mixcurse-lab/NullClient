package platform.client.features.modules.misc;

import baritone.api.BaritoneAPI;
import platform.api.module.Category;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.module.setting.BooleanSetting;

@ModuleRegister(a = "Baritone", b = "Автопоиск пути. Команды в чате: #goto, #mine, #follow, #stop", c = Category.Misc)
public class BaritoneModule extends Module {
    private final BooleanSetting allowBreak = new BooleanSetting("Ломать блоки", true);
    private final BooleanSetting allowPlace = new BooleanSetting("Ставить блоки", true);
    private final BooleanSetting allowSprint = new BooleanSetting("Спринт", true);
    private final BooleanSetting antiCheatCompatibility = new BooleanSetting("Античит-совместимость", false);

    public BaritoneModule() {
        this.allowBreak.a(ignored -> applySettingsWhenEnabled());
        this.allowPlace.a(ignored -> applySettingsWhenEnabled());
        this.allowSprint.a(ignored -> applySettingsWhenEnabled());
        this.antiCheatCompatibility.a(ignored -> applySettingsWhenEnabled());
        a(this.allowBreak, this.allowPlace, this.allowSprint, this.antiCheatCompatibility);
    }

    @Override
    public void b() {
        super.b();
        applySettings();
    }

    @Override
    public void c() {
        super.c();
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
    }

    private void applySettingsWhenEnabled() {
        if (m()) {
            applySettings();
        }
    }

    private void applySettings() {
        var settings = BaritoneAPI.getSettings();
        settings.allowBreak.value = this.allowBreak.c();
        settings.allowPlace.value = this.allowPlace.c();
        settings.allowSprint.value = this.allowSprint.c();
        settings.antiCheatCompatibility.value = this.antiCheatCompatibility.c();
    }
}
