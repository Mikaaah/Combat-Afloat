package com.townskingdoms.combatafloat.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.Sable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.client.particle.AbstractTrailParticle;
import yesman.epicfight.client.particle.AnimationTrailParticle;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(value = AnimationTrailParticle.class, remap = false)
public abstract class TrailParticleMixin extends AbstractTrailParticle<LivingEntityPatch<?>> {
    protected TrailParticleMixin(ClientLevel level, LivingEntityPatch<?> owner, TrailInfo trailInfo) {
        super(level, owner, trailInfo);
    }

    @WrapOperation(method = {"<init>", "createNextCurve"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getPosition(F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 combatafloat$trailPosition(LivingEntity entity, float partialTick, Operation<Vec3> original) {
        if (Sable.HELPER.getTrackingSubLevel(entity) == null) return original.call(entity, partialTick);
        return Sable.HELPER.getEyePositionInterpolated(entity, partialTick).subtract(0, entity.getEyeHeight(), 0);
    }

    @WrapOperation(method = "createNextCurve", at = @At(value = "INVOKE", target = "Lyesman/epicfight/api/utils/math/MathUtils;lerpVector(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 combatafloat$curvedShipPath(Vec3 start, Vec3 end, float partialTick, Operation<Vec3> original) {
        LivingEntity entity = owner.getOriginal();
        if (Sable.HELPER.getTrackingSubLevel(entity) == null) return original.call(start, end, partialTick);
        return Sable.HELPER.getEyePositionInterpolated(entity, partialTick).subtract(0, entity.getEyeHeight(), 0);
    }
}
