package com.townskingdoms.combatafloat.mixin.client;

import com.townskingdoms.combatafloat.ShipSpace;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.mixinterface.entity.entities_stick_sublevels.EntityStickExtension;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.network.common.BiDirectionalSyncAnimationPositionPacket;

@Mixin(targets = "yesman.epicfight.network.EpicFightClientBoundPayloadHandler", remap = false)
public interface AnimationPositionMixin {
    @Inject(method = "handleSyncAnimationPosition", at = @At("HEAD"), cancellable = true)
    private static void combatafloat$shipInterpolation(BiDirectionalSyncAnimationPositionPacket data,
            IPayloadContext context, CallbackInfo ci) {
        Entity entity = context.player().level().getEntity(data.entityId());
        if (!(entity instanceof LivingEntity living)) return;
        if (!ShipSpace.finite(data.position()) || Math.abs(data.position().x) > 30_000_000
                || Math.abs(data.position().y) > 30_000_000 || Math.abs(data.position().z) > 30_000_000) {
            ci.cancel(); return;
        }
        SubLevel ship = Sable.HELPER.getTrackingSubLevel(entity);
        if (ship == null) return;

        // EF packets carry world coordinates. Sable's stored lerp target is plot-local.
        SubLevel packetShip = Sable.HELPER.getContaining(entity.level(), data.position());
        Vec3 world = packetShip == null ? data.position() : packetShip.logicalPose().transformPosition(data.position());
        Vec3 local = ship.logicalPose().transformPositionInverse(world);
        if (!ShipSpace.finite(local) || Sable.HELPER.getContaining(entity.level(), local) != ship) {
            ci.cancel(); return;
        }
        EntityStickExtension extension = (EntityStickExtension) living;
        if (extension.sable$getPlotPosition() == null
                || Sable.HELPER.getContaining(entity.level(), extension.sable$getPlotPosition()) != ship) {
            extension.sable$setPlotPosition(ship.logicalPose().transformPositionInverse(entity.position()));
        }
        extension.sable$plotLerpTo(local, Math.max(1, Math.min(20, data.lerpSteps())));
        ci.cancel();
    }
}
