package com.townskingdoms.combatafloat.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.townskingdoms.combatafloat.ShipSpace;
import com.townskingdoms.combatafloat.ShipSlamPayload;
import dev.ryanhcode.sable.Sable;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import net.neoforged.neoforge.network.PacketDistributor;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.network.server.SPCreateTerrainFracture;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

@Mixin(value = LevelUtil.class, remap = false)
public abstract class LevelUtilMixin {
    @Unique private static final ThreadLocal<ShipSpace.Impact> combatafloat$impact = new ThreadLocal<>();

    @WrapMethod(method = "circleSlamFracture(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;DZZZ)Z")
    private static boolean combatafloat$shipSlam(LivingEntity caster, Level level, Vec3 center,
            double radius, boolean noSound, boolean noParticle, boolean hurtEntities, Operation<Boolean> original) {
        // NaN/infinite radii can turn EF's integer boundary loops into a soft hang.
        if (!ShipSpace.finite(center) || Math.abs(center.x) > 30_000_000
                || Math.abs(center.y) > 30_000_000 || Math.abs(center.z) > 30_000_000
                || !Double.isFinite(radius) || radius > 64) return false;
        ShipSpace.Impact previous = combatafloat$impact.get();
        ShipSpace.Impact impact = ShipSpace.impact(level, center, caster);
        if (impact == null) {
            combatafloat$impact.remove();
        } else {
            combatafloat$impact.set(impact);
        }
        try {
            // Retain the original terrain traversal, damage formula, skill hooks and stun.
            return original.call(caster, level, impact == null ? center : impact.center(),
                    radius, noSound, noParticle, hurtEntities);
        } finally {
            if (previous == null) combatafloat$impact.remove();
            else combatafloat$impact.set(previous);
        }
    }

    @ModifyExpressionValue(method = "spreadShockwave", at = @At(value = "FIELD", target = "Lyesman/epicfight/config/ClientConfig;groundSlams:Z"))
    private static boolean combatafloat$noShipFracture(boolean enabled, Level level, Vec3 center,
            Vec3 direction, double length, int edgeX, int edgeZ, List<Entity> hits) {
        // Skip the entire fracture branch, including its restore-state cache.
        return enabled && combatafloat$impact.get() == null && Sable.HELPER.getContaining(level, center) == null;
    }

    @WrapOperation(method = "spreadShockwave", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private static boolean combatafloat$protectAttachments(Level level, BlockPos pos, boolean drops, Operation<Boolean> original) {
        return combatafloat$impact.get() == null && Sable.HELPER.getContaining(level, pos) == null
                && original.call(level, pos, drops);
    }

    @WrapOperation(method = "spreadShockwave", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getY()D"))
    private static double combatafloat$targetY(Entity entity, Operation<Double> original) {
        ShipSpace.Impact impact = combatafloat$impact.get();
        return impact == null ? original.call(entity) : ShipSpace.inShip(impact.ship(), entity).y;
    }

    @WrapOperation(method = "spreadShockwave", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBlockX()I"))
    private static int combatafloat$targetX(Entity entity, Operation<Integer> original) {
        ShipSpace.Impact impact = combatafloat$impact.get();
        return impact == null ? original.call(entity) : Mth.floor(ShipSpace.inShip(impact.ship(), entity).x);
    }

    @WrapOperation(method = "spreadShockwave", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBlockZ()I"))
    private static int combatafloat$targetZ(Entity entity, Operation<Integer> original) {
        ShipSpace.Impact impact = combatafloat$impact.get();
        return impact == null ? original.call(entity) : Mth.floor(ShipSpace.inShip(impact.ship(), entity).z);
    }

    @WrapOperation(method = "circleSlamFracture(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;DZZZ)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;position()Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 combatafloat$damageDistance(Entity entity, Operation<Vec3> original) {
        ShipSpace.Impact impact = combatafloat$impact.get();
        return impact == null ? original.call(entity) : ShipSpace.inShip(impact.ship(), entity);
    }

    @WrapOperation(method = "circleSlamFracture(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;DZZZ)Z",
            at = @At(value = "INVOKE", target = "Lyesman/epicfight/world/damagesource/EpicFightDamageSource;setInitialPosition(Lnet/minecraft/world/phys/Vec3;)Lyesman/epicfight/world/damagesource/EpicFightDamageSource;"))
    private static EpicFightDamageSource combatafloat$damageOrigin(EpicFightDamageSource source, Vec3 center, Operation<EpicFightDamageSource> original) {
        ShipSpace.Impact impact = combatafloat$impact.get();
        return original.call(source, impact == null ? center : impact.world(center));
    }

    @WrapOperation(method = "circleSlamFracture(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;DZZZ)Z",
            at = @At(value = "INVOKE", target = "Lyesman/epicfight/network/EpicFightNetworkManager;sendToAllPlayerTrackingThisChunkWithSelf(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/ChunkPos;[Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V"))
    private static void combatafloat$worldTracking(CustomPacketPayload packet, ServerLevel level, ChunkPos chunk,
            CustomPacketPayload[] others, Operation<Void> original) {
        ShipSpace.Impact impact = combatafloat$impact.get();
        if (impact != null && packet instanceof SPCreateTerrainFracture fracture) {
            Vec3 world = impact.world(fracture.location());
            PacketDistributor.sendToPlayersNear(level, null, world.x, world.y, world.z, 128,
                    new ShipSlamPayload(impact.ship().getUniqueId(), fracture.location(), fracture.radius(), fracture.noSound(), fracture.noParticle()));
            return;
        }
        original.call(packet, level, chunk, others);
    }
}
