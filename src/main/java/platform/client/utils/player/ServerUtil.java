package platform.client.utils.player;

import static platform.api.module.Interface.aM_;

import platform.api.module.Interface;
import platform.client.utils.text.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.client.gui.components.LerpingBossEvent;

import platform.inject.accessors.BossHealthOverlayAccessor;
import platform.inject.accessors.PlayerTabOverlayAccessor;

public class ServerUtil implements Interface {
    @Generated
    private ServerUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final class d {
        @Generated
        private d() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }

        public static boolean a() {
            return ServerUtil.a().toLowerCase().contains("spookytime") || ServerUtil.b().toLowerCase().contains("spookytime");
        }

        public static int b() {
            if (ServerUtil.a() == null || !ServerUtil.a().contains("Анархия-")) {
                return -1;
            }
            return Integer.parseInt(ServerUtil.a().split("Анархия-")[1].trim());
        }

        public static int a(ItemStack itemStack) {
            if (!itemStack.isEmpty()) {
                List<String> tooltipLines = itemStack.getTooltipLines(Item.TooltipContext.EMPTY, Interface.aM_.player, TooltipFlag.NORMAL).stream().skip(1L).map((v0) -> {
                    return v0.getString();
                }).toList();
                if (!itemStack.getItemName().getString().contains("Товар не актуален") && itemStack.getItem() != Items.DYE.gray()) {
                    for (String line : tooltipLines) {
                        if (line.contains("$ Цена: ")) {
                            String price = line.substring(line.indexOf("$ Цена: ") + "$ Цена: ".length()).trim().replace(",", "").replace("$", "").replaceAll("\\s+", "");
                            if (!price.isEmpty()) {
                                try {
                                    return Integer.parseInt(price) / (itemStack.getCount() > 0 ? itemStack.getCount() : 1);
                                } catch (NumberFormatException e) {
                                    return -1;
                                }
                            }
                        }
                    }
                    return -1;
                }
                return -1;
            }
            return -1;
        }
    }

    public static final class a {
        @Generated
        private a() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }

        public static boolean a() {
            return ServerUtil.a().toLowerCase().contains("funtime") || ServerUtil.b().toLowerCase().contains("funtime");
        }

        public static boolean b() {
            return a() && Interface.aM_.level != null && Interface.aM_.level.dimension().identifier().toString().equals("minecraft:duels");
        }

        public static boolean c() {
            return a() && Interface.aM_.player.connection != null && Interface.aM_.level.getBiome(Interface.aM_.player.blockPosition()).is(Biomes.SWAMP) && (aM_.getConnection() == null || (aM_.getConnection() instanceof net.minecraft.client.multiplayer.ClientPacketListener cpl && cpl.serverBrand() != null && cpl.serverBrand().contains("BotFilter (https://vk.cc/8hr1pU)")));
        }

        public static int d() {
            if (ServerUtil.a() == null || !ServerUtil.a().contains("Анархия-")) {
                return -1;
            }
            return Integer.parseInt(ServerUtil.a().split("Анархия-")[1].trim());
        }

        public static int a(ItemStack itemStack) {
            if (!itemStack.isEmpty()) {
                List<String> tooltipLines = itemStack.getTooltipLines(Item.TooltipContext.EMPTY, Interface.aM_.player, TooltipFlag.NORMAL).stream().skip(1L).map((v0) -> {
                    return v0.getString();
                }).toList();
                if (!itemStack.getItemName().getString().contains("Товар не актуален") && itemStack.getItem() != Items.DYE.gray()) {
                    for (String line : tooltipLines) {
                        if (line.contains("$ Ценa: ")) {
                            String price = line.substring(line.indexOf("$ Ценa: ") + "$ Ценa: ".length()).trim().replace(",", "").replace("$", "").replaceAll("\\s+", "");
                            if (!price.isEmpty()) {
                                try {
                                    return Integer.parseInt(price) / (itemStack.getCount() > 0 ? itemStack.getCount() : 1);
                                } catch (NumberFormatException e) {
                                    return -1;
                                }
                            }
                        }
                    }
                    return -1;
                }
                return -1;
            }
            return -1;
        }

        public static float a(LivingEntity entity) {
            if (Interface.aM_.level != null) {
                Scoreboard scoreboard = Interface.aM_.level.getScoreboard();
                for (Objective objective : scoreboard.getObjectives()) {
                    ReadOnlyScoreInfo score = scoreboard.getPlayerScoreInfo(entity, objective);
                    if (score != null) {
                        return score.value();
                    }
                }
            }
            return entity.getHealth() + entity.getAbsorptionAmount();
        }

        public static long e() {
            if (Interface.aM_.level != null) {
                for (PlayerTeam team : Interface.aM_.level.getScoreboard().getPlayerTeams()) {
                    String message = team.getPlayerPrefix().getString().toLowerCase(Locale.ROOT);
                    if (message.contains("монет")) {
                        return Long.parseLong(message.substring(message.lastIndexOf(StringUtils.a) + 1).replaceAll("[^0-9]", ""));
                    }
                }
                return -1L;
            }
            return -1L;
        }

        public static String b(ItemStack stack) {
            int id = ((Integer) Optional.ofNullable((CustomModelData) stack.get(DataComponents.CUSTOM_MODEL_DATA)).filter(data -> {
                return !data.floats().isEmpty();
            }).map(data2 -> {
                return Integer.valueOf(((Float) data2.floats().getFirst()).intValue());
            }).orElse(0)).intValue();
            switch (id) {
                case 1:
                    return "Талисман Мрака";
                case 2:
                    return "Талисман Вихря";
                case 3:
                    return "Талисман Демона";
                case 4:
                    return "Талисман Раздора";
                case 5:
                    return "Талисман Ярости";
                case 6:
                    return "Талисман Крушителя";
                case 7:
                    return "Талисман Карателя";
                case 8:
                    return "Талисман Тирана";
                default:
                    return "Тотем бессмертия";
            }
        }
    }

    public static final class c {
        @Generated
        private c() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }

        public static boolean a() {
            return ServerUtil.a().toLowerCase().contains("reallyworld") || ServerUtil.b().toLowerCase().contains("reallyworld");
        }
    }

    public static final class b {
        @Generated
        private b() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }

        public static boolean a() {
            return ServerUtil.a().toLowerCase().contains("holyworld") || ServerUtil.b().toLowerCase().contains("holyworld");
        }

        public static int b() {
            String last = ServerUtil.a().trim().replaceAll("(?s).*\\n", "");
            if (last.contains("Лайт") && last.contains("#")) {
                return Integer.parseInt(last.replaceAll(".*#(\\d+).*", "$1"));
            }
            return -1;
        }
    }

    public static String a() {
        return (aM_.player == null || aM_.player.connection == null || ((PlayerTabOverlayAccessor) aM_.gui.hud.getTabList()).getHeader() == null) ? "" : ((PlayerTabOverlayAccessor) aM_.gui.hud.getTabList()).getHeader().getString();
    }

    public static String b() {
        return (aM_.player == null || aM_.player.connection == null) ? "" : ((ServerData) Objects.requireNonNull(aM_.player.connection.getServerData())).ip;
    }

    public static double c() {
        return Math.hypot(aM_.player.getX() - aM_.player.xOld, aM_.player.getZ() - aM_.player.zOld) * 20.0d;
    }

    public static int d() {
        if (aM_.player == null || ((net.minecraft.client.multiplayer.ClientPacketListener) Objects.requireNonNull(aM_.getConnection())).getPlayerInfo(aM_.player.getUUID()) == null) {
            return 0;
        }
        return ((PlayerInfo) Objects.requireNonNull(aM_.getConnection().getPlayerInfo(aM_.player.getUUID()))).getLatency();
    }

    public static boolean e() {
        if (aM_.player != null && !((BossHealthOverlayAccessor) aM_.gui.hud.getBossOverlay()).getBossBars().isEmpty()) {
            for (LerpingBossEvent bossBar : ((BossHealthOverlayAccessor) aM_.gui.hud.getBossOverlay()).getBossBars().values()) {
                if (bossBar.getName().getString().toLowerCase(Locale.ROOT).contains("pvp") || bossBar.getName().getString().toLowerCase(Locale.ROOT).contains("пвп") || bossBar.getName().getString().toLowerCase(Locale.ROOT).contains("дуэль")) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public static int f() {
        if (aM_.player == null || ((BossHealthOverlayAccessor) aM_.gui.hud.getBossOverlay()).getBossBars().isEmpty()) {
            return -1;
        }
        for (LerpingBossEvent bossBar : ((BossHealthOverlayAccessor) aM_.gui.hud.getBossOverlay()).getBossBars().values()) {
            String name = bossBar.getName().getString().toLowerCase(Locale.ROOT);
            if (name.contains("pvp") || name.contains("пвп")) {
                Matcher matcher = Pattern.compile("(\\d+):(\\d+)").matcher(name);
                if (matcher.find()) {
                    return (Integer.parseInt(matcher.group(1)) * 60) + Integer.parseInt(matcher.group(2));
                }
                Matcher matcher2 = Pattern.compile("(\\d+)").matcher(name);
                if (matcher2.find()) {
                    return Integer.parseInt(matcher2.group(1));
                }
            }
        }
        return -1;
    }
}


