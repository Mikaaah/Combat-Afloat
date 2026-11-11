package com.townskingdoms.combatafloat.test;

import com.townskingdoms.combatafloat.ShipSpace;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import yesman.epicfight.api.utils.LevelUtil;

import java.util.ArrayList;
import java.util.List;

/** Runs only in the isolated development probe; excluded from the release JAR. */
@Mod("combatafloat_tests")
public final class IntegrationProbe {
    public IntegrationProbe() {
        if (Boolean.getBoolean("combatafloat.integration")) NeoForge.EVENT_BUS.addListener(IntegrationProbe::run);
    }

    private static void require(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
        System.out.println("COMBAT_AFLOAT_PASS: " + message);
    }

    private static Zombie target(ServerLevel level, Vec3 feet) {
        Zombie zombie = EntityType.ZOMBIE.create(level);
        if (zombie == null) throw new AssertionError("Zombie creation failed");
        zombie.setNoAi(true); zombie.setNoGravity(true);
        zombie.moveTo(feet.x, feet.y, feet.z, 0, 0);
        if (!level.addFreshEntity(zombie)) throw new AssertionError("Zombie spawn failed");
        return zombie;
    }

    private static void run(ServerStartedEvent event) {
        ServerLevel level = event.getServer().overworld();
        BlockPos anchor = new BlockPos(0, 150, 0);
        List<BlockPos> blocks = new ArrayList<>();
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            BlockPos pos = anchor.offset(x, 0, z);
            level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState()); blocks.add(pos);
        }
        BlockPos torch = anchor.offset(2, 1, 0);
        level.setBlockAndUpdate(torch, Blocks.TORCH.defaultBlockState()); blocks.add(torch);
        ServerSubLevel ship = SubLevelAssemblyHelper.assembleBlocks(level, anchor, blocks,
                new BoundingBox3i(anchor.offset(-3, 0, -3), anchor.offset(4, 3, 4)));
        ship.updateBoundingBox();
        ship.forceUpdateGlobalBounds();
        BlockPos plot = ship.getPlot().getCenterBlock();
        Vec3 local = Vec3.atBottomCenterOf(plot);
        BlockHitResult rawShipHit = new BlockHitResult(local.add(0, 1, 0), Direction.UP, plot, false);
        BlockHitResult worldShipHit = ShipSpace.worldRayHit(level, rawShipHit);
        require(worldShipHit.getLocation().distanceTo(ship.logicalPose().transformPosition(rawShipHit.getLocation())) < .00001,
                "ship raycast endpoint converted into world coordinates");
        require(worldShipHit.getBlockPos().equals(plot), "raycast retains plot block identity");
        BlockHitResult terrainHit = new BlockHitResult(new Vec3(8, 150, 8), Direction.UP, new BlockPos(8, 150, 8), false);
        require(ShipSpace.worldRayHit(level, terrainHit) == terrainHit, "normal-world raycast remains unchanged");
        Vec3 feet = ship.logicalPose().transformPosition(Vec3.atBottomCenterOf(plot.above()));
        Zombie caster = target(level, feet.add(0, 0, 10));
        Zombie zombie = target(level, feet);
        float health = zombie.getHealth();
        require(LevelUtil.circleSlamFracture(caster, level, local, 3, true, true, true), "local ship slam executes");
        require(zombie.getHealth() < health, "local ship slam damages world-space target");
        zombie.discard();
        require(level.getBlockState(plot.offset(2, 1, 0)).is(Blocks.TORCH), "ship torch survives shockwave");

        Zombie second = target(level, feet);
        require(ShipSpace.impact(level, ship.logicalPose().transformPosition(local), second) != null, "world-space deck detection finds ship");
        require(LevelUtil.circleSlamFracture(caster, level, ship.logicalPose().transformPosition(local), 3, true, true, true), "world-space ship slam executes");
        require(second.getHealth() < second.getMaxHealth(), "world-space ship slam damages target");
        second.discard();
        caster.discard();
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++)
            require(level.getBlockState(plot.offset(x, 0, z)).is(Blocks.STONE), "deck intact " + x + "," + z);

        ship.logicalPose().orientation().rotateXYZ(.25, .5, .15);
        ship.logicalPose().position().add(30, 8, -20);
        ship.updateBoundingBox(); ship.forceUpdateGlobalBounds();
        Vec3 rotatedFeet = ship.logicalPose().transformPosition(Vec3.atBottomCenterOf(plot.above()));
        Zombie rotatedCaster = target(level, rotatedFeet.add(0, 0, 10));
        Zombie rotatedTarget = target(level, rotatedFeet);
        require(LevelUtil.circleSlamFracture(rotatedCaster, level, ship.logicalPose().transformPosition(local), 3, true, true, true), "translated and pitched ship slam executes");
        require(rotatedTarget.getHealth() < rotatedTarget.getMaxHealth(), "pitched ship target receives slam damage");
        require(level.getBlockState(plot.offset(2, 1, 0)).is(Blocks.TORCH), "pitched ship attachment survives");
        rotatedTarget.discard(); rotatedCaster.discard();

        // Run this immediate-spawn check in the already active spawn chunk. A newly
        // loaded distant chunk may not expose new entities to queries until its first tick.
        BlockPos terrain = new BlockPos(8, 150, 8);
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++)
            level.setBlockAndUpdate(terrain.offset(x, 0, z), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(terrain.above(), Blocks.TORCH.defaultBlockState());
        Zombie terrainCaster = target(level, Vec3.atBottomCenterOf(terrain.above()).add(0, 0, 10));
        Zombie terrainTarget = target(level, Vec3.atBottomCenterOf(terrain.above()));
        require(LevelUtil.circleSlamFracture(terrainCaster, level, Vec3.atBottomCenterOf(terrain), 3, true, true, true), "normal terrain slam executes");
        require(terrainTarget.getHealth() < terrainTarget.getMaxHealth(), "normal terrain damage retained");
        require(level.getBlockState(terrain.above()).isAir(), "normal terrain attachment behavior retained");
        terrainTarget.discard(); terrainCaster.discard();

        require(!LevelUtil.circleSlamFracture(null, level, local, Double.NaN, true, true, true), "NaN radius rejected");
        require(!LevelUtil.circleSlamFracture(null, level, local, Double.POSITIVE_INFINITY, true, true, true), "infinite radius rejected");
        require(!LevelUtil.circleSlamFracture(null, level, local, 1e20, true, true, true), "unbounded radius rejected");
        require(!LevelUtil.circleSlamFracture(null, level, new Vec3(1e100, 150, 0), 3, true, true, true), "overflow coordinates rejected");
        System.out.println("COMBAT_AFLOAT_INTEGRATION_SUCCESS");
        event.getServer().halt(false);
    }
}
