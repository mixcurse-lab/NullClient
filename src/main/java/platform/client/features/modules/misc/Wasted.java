package platform.client.features.modules.misc;

import static platform.api.module.Interface.aM_;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.SliderSetting;

import java.io.BufferedInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;

@ModuleRegister(a = "Wasted", b = "Проигрывает звук при вашей смерти", c = Category.Misc)
public class Wasted extends Module {
    private final ModeSetting sound = new ModeSetting("Звук", "Тип 1", "Тип 1", "Тип 2", "Тип 3");
    private final SliderSetting volume = new SliderSetting("Громкость", 0.5f, 0.0f, 1.0f, 0.05f);
    private final ExecutorService soundThread = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Wasted-Sound-Thread");
        thread.setDaemon(true);
        return thread;
    });
    private boolean wasDead;

    public Wasted() {
        a(this.sound, this.volume);
    }

    @EventTarget
    public void a(TickEvent event) {
        if (aM_.player == null) {
            this.wasDead = false;
            return;
        }
        boolean dead = aM_.player.isDeadOrDying();
        if (dead && !this.wasDead) {
            play(soundFile());
        }
        this.wasDead = dead;
    }

    private String soundFile() {
        switch (this.sound.c()) {
            case "Тип 2":
                return "you_dead_1.wav";
            case "Тип 3":
                return "you_dead_2.wav";
            case "Тип 1":
            default:
                return "you_dead.wav";
        }
    }

    private void play(String filename) {
        if (filename == null || filename.isEmpty() || aM_.getResourceManager() == null) {
            return;
        }
        this.soundThread.execute(() -> {
            try {
                AudioInputStream sourceStream = AudioSystem.getAudioInputStream(new BufferedInputStream(aM_.getResourceManager().open(net.minecraft.resources.Identifier.fromNamespaceAndPath("delta", "sounds/" + filename))));


                AudioFormat sourceFormat = sourceStream.getFormat();
                AudioFormat pcmFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        sourceFormat.getSampleRate(),
                        16,
                        sourceFormat.getChannels(),
                        sourceFormat.getChannels() * 2,
                        sourceFormat.getSampleRate(),
                        false
                );
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(pcmFormat, sourceStream);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), (float) (20.0d * Math.log10(Math.max(this.volume.c().floatValue(), 1.0E-4f))))));
                }
                clip.start();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
