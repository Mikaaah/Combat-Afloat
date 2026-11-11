package com.townskingdoms.combatafloat;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yesman.epicfight.api.utils.LevelUtil;

import java.util.UUID;

/** Retains a local impact anchor even if the ship moves before the packet arrives. */
public record ShipSlamPayload(UUID shipId, Vec3 center, double radius, boolean noSound, boolean noParticle)
        implements CustomPacketPayload {
    public static final Type<ShipSlamPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CombatAfloat.MOD_ID, "ship_slam"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShipSlamPayload> CODEC = StreamCodec.of(
            (buffer, packet) -> {
                buffer.writeUUID(packet.shipId);
                buffer.writeDouble(packet.center.x); buffer.writeDouble(packet.center.y); buffer.writeDouble(packet.center.z);
                buffer.writeDouble(packet.radius); buffer.writeBoolean(packet.noSound); buffer.writeBoolean(packet.noParticle);
            }, buffer -> new ShipSlamPayload(buffer.readUUID(), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                    buffer.readDouble(), buffer.readBoolean(), buffer.readBoolean()));

    @Override public Type<ShipSlamPayload> type() { return TYPE; }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE, CODEC, ShipSlamPayload::handle);
    }

    private static void handle(ShipSlamPayload packet, IPayloadContext context) {
        if (!ShipSpace.finite(packet.center) || !Double.isFinite(packet.radius) || packet.radius > 64) return;
        SubLevelContainer container = SubLevelContainer.getContainer(context.player().level());
        SubLevel ship = container == null ? null : container.getSubLevel(packet.shipId);
        if (ship == null || ship.isRemoved()) return;
        // The explicit plot anchor prevents a packet from becoming a normal-world slam.
        if (dev.ryanhcode.sable.Sable.HELPER.getContaining(ship.getLevel(), packet.center) != ship) return;
        LevelUtil.circleSlamFracture(null, ship.getLevel(), packet.center, packet.radius,
                packet.noSound, packet.noParticle, false);
    }
}
