package com.townskingdoms.combatafloat;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import yesman.epicfight.api.utils.LevelUtil;
import org.joml.Vector3d;

/** All stored entity positions remain in the coordinate space owned by Sable. */
public final class ShipSpace {
    private ShipSpace() {}

    public static boolean finite(Vec3 point) {
        return Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z);
    }

    public static Vec3 worldPosition(Entity entity) {
        return Sable.HELPER.projectOutOfSubLevel(entity.level(), entity.position());
    }

    /** Sable raycasts retain plot-local hit locations; EF movement requires a world-space endpoint. */
    public static BlockHitResult worldRayHit(Level level, BlockHitResult hit) {
        if (hit.getType() != HitResult.Type.BLOCK) return hit;
        SubLevel ship = Sable.HELPER.getContaining(level, hit.getBlockPos());
        if (ship == null || Sable.HELPER.getContaining(level, hit.getLocation()) != ship) return hit;
        return new BlockHitResult(ship.logicalPose().transformPosition(hit.getLocation()),
                hit.getDirection(), hit.getBlockPos(), hit.isInside());
    }

    public static Vec3 inShip(SubLevel ship, Entity entity) {
        return ship.logicalPose().transformPositionInverse(worldPosition(entity));
    }

    public static SubLevel tracking(Entity entity) {
        SubLevel containing = Sable.HELPER.getContaining(entity);
        return containing != null ? containing : Sable.HELPER.getTrackingOrVehicleSubLevel(entity);
    }

    public record Impact(SubLevel ship, Vec3 center) {
        public Vec3 world(Vec3 local) { return ship.logicalPose().transformPosition(local); }
    }

    /** Detect an actual supporting ship block, rather than any nearby ship bounding box. */
    public static Impact impact(Level level, Vec3 center, Entity caster) {
        SubLevel containing = Sable.HELPER.getContaining(level, center);
        if (containing != null) return new Impact(containing, center);

        // A normal terrain slam must retain Epic Fight's normal terrain behavior.
        BlockPos origin = BlockPos.containing(center);
        if (level.hasChunkAt(origin) && LevelUtil.canTransferShockWave(level, origin, level.getBlockState(origin))) {
            return null;
        }

        SubLevel preferred = caster == null ? null : tracking(caster);
        Impact result = supporting(level, preferred, center);
        if (result != null) return result;
        BoundingBox3d bounds = new BoundingBox3d(new AABB(center, center).inflate(1.5));
        for (SubLevel candidate : Sable.HELPER.getAllIntersecting(level, bounds)) {
            if (candidate == preferred) continue;
            result = supporting(level, candidate, center);
            if (result != null) return result;
        }
        // A loaded ship's bounds can be available before its physics query ticket.
        SubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container != null) for (SubLevel candidate : container.queryIntersecting(bounds)) {
            result = supporting(level, candidate, center);
            if (result != null) return result;
        }
        return null;
    }

    private static Impact supporting(Level level, SubLevel ship, Vec3 world) {
        if (ship == null || ship.isRemoved()) return null;
        Vec3 local = ship.logicalPose().transformPositionInverse(world);
        if (!finite(local)) return null;
        BlockPos origin = BlockPos.containing(local);
        // EF's weapon-edge sample can lie just above/below a pitched deck.
        for (int offset : new int[]{0, -1, 1}) {
            BlockPos probe = origin.offset(0, offset, 0);
            if (Sable.HELPER.getContaining(level, probe) == ship && level.hasChunkAt(probe)
                    && LevelUtil.canTransferShockWave(level, probe, level.getBlockState(probe))) {
                return new Impact(ship, new Vec3(local.x, probe.getY(), local.z));
            }
        }
        return null;
    }

    /** Entity displacement with the motion of its supporting ship removed. */
    public static Vec3 relativeDisplacement(Entity entity, SubLevel ship, Vec3 previous) {
        Vector3d delta = ShipKinematics.relativeDisplacement(ship.lastPose(), ship.logicalPose(),
                new Vector3d(previous.x, previous.y, previous.z),
                new Vector3d(entity.getX(), entity.getY(), entity.getZ()));
        return new Vec3(delta.x, delta.y, delta.z);
    }
}
