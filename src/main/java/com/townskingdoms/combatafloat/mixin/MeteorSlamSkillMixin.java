package com.townskingdoms.combatafloat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.townskingdoms.combatafloat.ShipSpace;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yesman.epicfight.skill.identity.MeteorSlamSkill;

@Mixin(value = MeteorSlamSkill.class, remap = false)
public abstract class MeteorSlamSkillMixin {
    // Verified synthetic method in the supported 21.17.3.1 binary; the contract test pins it.
    @WrapOperation(method = "lambda$onInitiate$4", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;"))
    private BlockHitResult combatafloat$worldFallDistance(Level level, ClipContext ray, Operation<BlockHitResult> original) {
        return ShipSpace.worldRayHit(level, original.call(level, ray));
    }
}
