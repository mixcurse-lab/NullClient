package platform.client.utils.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class SkullProfileNbtFixer {
    private static final Logger LOGGER = LoggerFactory.getLogger("skullfix");

    private SkullProfileNbtFixer() {}

    public static void normalize(CompoundTag blockEntityTag) {
        if (blockEntityTag == null) return;
        CompoundTag profile = getCompound(blockEntityTag, "profile", "Profile");
        if (profile == null) return;
        boolean idChanged = normalizeId(profile);
        boolean propertiesChanged = normalizeProperties(profile);
        if (idChanged || propertiesChanged) {
            LOGGER.debug("skullfix: normalized legacy skull profile NBT (id={}, properties={})", idChanged, propertiesChanged);
        }
    }

    private static boolean normalizeId(CompoundTag profile) {
        Tag idTag = profile.get("id");
        String sourceKey = "id";
        if (idTag == null) {
            idTag = profile.get("Id");
            sourceKey = "Id";
        }
        if (!(idTag instanceof StringTag stringId)) return false;
        UUID uuid;
        try {
            uuid = UUID.fromString(stringId.value());
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (!"id".equals(sourceKey)) profile.remove(sourceKey);
        profile.put("id", intArrayFromUuid(uuid));
        return true;
    }

    private static IntArrayTag intArrayFromUuid(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        return new IntArrayTag(new int[]{
                (int) (msb >> 32),
                (int) msb,
                (int) (lsb >> 32),
                (int) lsb
        });
    }

    private static boolean normalizeProperties(CompoundTag profile) {
        String sourceKey = profile.contains("properties") ? "properties"
                : profile.contains("Properties") ? "Properties" : null;
        if (sourceKey == null) return false;
        Tag propertiesTag = profile.get(sourceKey);
        if (isAlreadyModernList(propertiesTag)) return false;
        if (!(propertiesTag instanceof CompoundTag legacyProperties)) return false;
        ListTag rebuilt = new ListTag();
        for (String propertyName : legacyProperties.keySet()) {
            Tag entries = legacyProperties.get(propertyName);
            if (entries instanceof ListTag list) {
                for (Tag entry : list) addRewritten(rebuilt, propertyName, entry);
            } else if (entries instanceof CompoundTag single) {
                addRewritten(rebuilt, propertyName, single);
            } else if (entries instanceof StringTag rawValue) {
                CompoundTag out = new CompoundTag();
                out.putString("name", propertyName);
                out.putString("value", rawValue.value());
                rebuilt.add(out);
            }
        }
        if (rebuilt.isEmpty()) return false;
        profile.remove(sourceKey);
        profile.put("properties", rebuilt);
        return true;
    }

    private static void addRewritten(ListTag target, String propertyName, Tag entry) {
        if (!(entry instanceof CompoundTag compound)) return;
        String value = firstString(compound, "value", "Value");
        if (value == null) return;
        String signature = firstString(compound, "signature", "Signature");
        CompoundTag out = new CompoundTag();
        out.putString("name", propertyName);
        out.putString("value", value);
        if (signature != null) out.putString("signature", signature);
        target.add(out);
    }

    private static String firstString(CompoundTag compound, String... keys) {
        for (String key : keys) {
            Tag tag = compound.get(key);
            if (tag instanceof StringTag stringTag) return stringTag.value();
        }
        return null;
    }

    private static boolean isAlreadyModernList(Tag tag) {
        if (!(tag instanceof ListTag list) || list.isEmpty()) return false;
        Tag first = list.get(0);
        return first instanceof CompoundTag compound && compound.contains("name") && compound.contains("value");
    }

    private static CompoundTag getCompound(CompoundTag parent, String... keys) {
        for (String key : keys) {
            Tag tag = parent.get(key);
            if (tag instanceof CompoundTag compound) return compound;
        }
        return null;
    }
}
