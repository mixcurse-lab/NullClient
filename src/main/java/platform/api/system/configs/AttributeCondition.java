package platform.api.system.configs;


import lombok.Generated;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Holder;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.core.component.DataComponents;

public class AttributeCondition {
    private final Holder<Attribute> a;
    private final AttributeModifier.Operation b;
    private final double c;

    @Generated
    public Holder<Attribute> a() {
        return this.a;
    }

    @Generated
    public AttributeModifier.Operation b() {
        return this.b;
    }

    @Generated
    public double c() {
        return this.c;
    }

    public AttributeCondition(Holder<Attribute> attribute, double expectedAmount, AttributeModifier.Operation expectedOperation) {
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute cannot be null");
        }
        this.a = attribute;
        this.c = expectedAmount;
        this.b = expectedOperation;
    }

    public boolean a(Holder<Attribute> currentAttr, AttributeModifier modifier) {
        return currentAttr.equals(this.a) && Math.abs(modifier.amount() - this.c) < 9.9999942618434E-4d && modifier.operation() == this.b;
    }

    public boolean a(ItemStack stack) {
        ItemAttributeModifiers modifiersComponent = (ItemAttributeModifiers) stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (ItemAttributeModifiers.Entry entry : modifiersComponent.modifiers()) {
            if (a(entry.attribute(), entry.modifier())) {
                return true;
            }
        }
        return false;
    }
}



