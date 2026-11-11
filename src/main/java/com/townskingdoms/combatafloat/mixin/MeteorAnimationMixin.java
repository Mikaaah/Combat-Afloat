package com.townskingdoms.combatafloat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.townskingdoms.combatafloat.ShipSpace;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yesman.epicfight.gameasset.Animations;

@Mixin(value = Animations.class, remap = false)
public abstract class MeteorAnimationMixin {
    // This coordinate setter otherwise builds a movement curve millions of blocks long.
    @WrapOperation(method = "lambda$build$242", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;pick(DFZ)Lnet/minecraft/world/phys/HitResult;"))
    private static HitResult combatafloat$worldMovementTarget(LivingEntity entity, double distance,
            float partialTick, boolean fluids, Operation<HitResult> original) {
        HitResult hit = original.call(entity, distance, partialTick, fluids);
        return hit instanceof BlockHitResult block ? ShipSpace.worldRayHit(entity.level(), block) : hit;
    }
}
