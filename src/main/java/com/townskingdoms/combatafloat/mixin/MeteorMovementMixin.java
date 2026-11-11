package com.townskingdoms.combatafloat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.townskingdoms.combatafloat.ShipSpace;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.gameasset.Animations;

@Mixin(value = ActionAnimation.class, remap = false)
public abstract class MeteorMovementMixin {
    @WrapOperation(method = "move", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
    private void combatafloat$sweptShipLanding(LivingEntity entity, MoverType type, Vec3 movement, Operation<Void> original) {
        if ((Object) this != Animations.METEOR_SLAM.get()) {
            original.call(entity, type, movement); return;
        }
        if (!ShipSpace.finite(movement) || movement.lengthSqr() > 64 * 64) return;
        boolean shipInPath = Sable.HELPER.getTrackingSubLevel(entity) != null;
        if (!shipInPath) {
            var bounds = new BoundingBox3d(entity.getBoundingBox().expandTowards(movement).inflate(.5));
            shipInPath = Sable.HELPER.getAllIntersecting(entity.level(), bounds).iterator().hasNext();
        }
        if (!shipInPath) { original.call(entity, type, movement); return; }
        // Sable's discrete hull collision can miss a deck when an animation moves through
        // its entire thickness in one call. Preserve collision/fall callbacks at each step.
        int steps = Math.max(1, (int) Math.ceil(movement.length() / .25));
        Vec3 step = movement.scale(1.0 / steps);
        for (int i = 0; i < steps; i++) original.call(entity, type, step);
    }
}
