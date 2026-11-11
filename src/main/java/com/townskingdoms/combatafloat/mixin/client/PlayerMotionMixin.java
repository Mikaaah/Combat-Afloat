package com.townskingdoms.combatafloat.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.townskingdoms.combatafloat.ShipSpace;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(value = AbstractClientPlayerPatch.class, remap = false)
public abstract class PlayerMotionMixin {
    @WrapOperation(method = "updateMotion", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getY()D"))
    private double combatafloat$relativeFall(AbstractClientPlayer entity, Operation<Double> original) {
        SubLevel ship = Sable.HELPER.getTrackingSubLevel(entity);
        if (ship == null) return original.call(entity);
        PlayerPatch<?> patch = (PlayerPatch<?>) (Object) this;
        Vec3 previous = new Vec3(patch.getXOld(), patch.getYOld(), patch.getZOld());
        // updateMotion subtracts patch.yo after this call. Preserve that expression.
        return patch.getYOld() + ShipSpace.relativeDisplacement(entity, ship, previous).y;
    }

    @Inject(method = "getAccuratePartialLocation", at = @At("HEAD"), cancellable = true)
    private void combatafloat$clothLocation(float partialTick, CallbackInfoReturnable<Vec3> cir) {
        // Negative partial ticks describe the prior tick and need EF's longer history.
        if (partialTick < 0.0F || partialTick > 1.0F) return;
        AbstractClientPlayer entity = ((AbstractClientPlayerPatch<?>) (Object) this).getOriginal();
        if (Sable.HELPER.getTrackingSubLevel(entity) != null) {
            cir.setReturnValue(Sable.HELPER.getEyePositionInterpolated(entity, partialTick).subtract(0, entity.getEyeHeight(), 0));
        }
    }

    @Inject(method = "getObjectVelocity", at = @At("HEAD"), cancellable = true)
    private void combatafloat$clothVelocity(CallbackInfoReturnable<Vec3> cir) {
        AbstractClientPlayer entity = ((AbstractClientPlayerPatch<?>) (Object) this).getOriginal();
        SubLevel ship = Sable.HELPER.getTrackingSubLevel(entity);
        if (ship != null) {
            cir.setReturnValue(ShipSpace.relativeDisplacement(entity, ship, new Vec3(entity.xOld, entity.yOld, entity.zOld)));
        }
    }
}
