package com.townskingdoms.combatafloat.test;

import com.google.gson.*;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.registry.entries.*;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.*;

import java.nio.file.*;
import java.util.*;

/** Graphical test harness. This source set is never included in the release JAR. */
@EventBusSubscriber(modid = "combatafloat_tests", value = Dist.CLIENT)
public final class GraphicalCombatProbe {
    private static final Path CONTROL = Path.of("capture-command.json");
    private static final Path STATE = Path.of("capture-state.json");
    private static String lastCommand = "";
    private static ServerSubLevel ship;
    private static BlockPos plot;
    private static int ticks;
    private static boolean ready;
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();

    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (!Boolean.getBoolean("combatafloat.capture") || !(event.getEntity() instanceof ServerPlayer player)) return;
        var level = player.serverLevel();
        level.setDayTime(6000);
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, player.server);
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, player.server);
        level.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, player.server);
        var container = SubLevelContainer.getContainer(level);
        container.physicsSystem().setPaused(true);
        BlockPos anchor = new BlockPos(0, 80, 0);
        List<BlockPos> blocks = new ArrayList<>();
        for (int x = -7; x <= 7; x++) for (int z = -9; z <= 9; z++) {
            BlockPos at = anchor.offset(x, 0, z);
            boolean edge = Math.abs(x) == 7 || Math.abs(z) == 9;
            level.setBlockAndUpdate(at, (edge ? Blocks.STRIPPED_SPRUCE_LOG : Blocks.OAK_PLANKS).defaultBlockState());
            blocks.add(at);
            if (edge) {
                level.setBlockAndUpdate(at.above(), Blocks.SPRUCE_FENCE.defaultBlockState());
                blocks.add(at.above());
            }
        }
        for (int x : new int[]{-6, 6}) for (int z : new int[]{-8, 8}) {
            for (int y = 1; y <= 3; y++) {
                BlockPos at = anchor.offset(x, y, z);
                level.setBlockAndUpdate(at, Blocks.SPRUCE_FENCE.defaultBlockState()); blocks.add(at);
            }
            BlockPos lamp = anchor.offset(x, 4, z);
            level.setBlockAndUpdate(lamp, Blocks.LANTERN.defaultBlockState()); blocks.add(lamp);
        }
        ship = SubLevelAssemblyHelper.assembleBlocks(level, anchor, blocks,
                new BoundingBox3i(anchor.offset(-8, 0, -10), anchor.offset(8, 6, 10)));
        ship.setName("Combat Afloat Test Deck");
        ship.updateBoundingBox(); ship.forceUpdateGlobalBounds();
        plot = ship.getPlot().getCenterBlock();
        player.teleportTo(level, -.8, 81, -2.8, -20, 8);
        player.getInventory().setItem(0, new ItemStack(EpicFightItems.IRON_GREATSWORD.get()));
        player.getInventory().selected = 0;
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        var patch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        patch.toMode(PlayerPatch.PlayerMode.EPICFIGHT, true);
        patch.setStamina(patch.getMaxStamina());
        PlayerSkillCommandBridge.meteor(player);
        for (int i = 0; i < 3; i++) {
            Zombie target = EntityType.ZOMBIE.create(level);
            target.setNoAi(true); target.setPersistenceRequired();
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200); target.setHealth(200);
            target.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            target.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            target.setCustomName(Component.literal(i == 0 ? "Deck Sparring Guard" : "Training Guard"));
            target.setCustomNameVisible(true);
            target.moveTo(i == 0 ? .5 : (i == 1 ? -4 : 4), 81, i == 0 ? .2 : 4, 180, 0);
            level.addFreshEntity(target);
        }
        System.out.println("GRAPHICAL_PROBE_SHIP_READY " + ship.getUniqueId());
        ready = true;
    }

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("combatafloat.capture")) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        ticks++;
        if (ticks == 50) { mc.setScreen(null); mc.options.setCameraType(CameraType.THIRD_PERSON_BACK); }
        try {
            if (Files.exists(CONTROL)) {
                String text = Files.readString(CONTROL);
                if (!text.equals(lastCommand)) {
                    lastCommand = text;
                    JsonObject request = JsonParser.parseString(text).getAsJsonObject();
                    act(mc, request);
                }
            }
            if (ticks % 10 == 0) {
                JsonObject state = new JsonObject();
                state.addProperty("ticks", ticks); state.addProperty("sceneReady", ready);
                state.addProperty("screen", mc.screen == null ? "none" : mc.screen.getClass().getSimpleName());
                state.addProperty("position", mc.player.position().toString());
                state.addProperty("onGround", mc.player.onGround());
                var tracked = Sable.HELPER.getTrackingSubLevel(mc.player);
                state.addProperty("trackingShip", tracked == null ? "none" : tracked.getUniqueId().toString());
                var patch = EpicFightCapabilities.getLocalPlayerPatch(mc.player);
                state.addProperty("combatMode", patch != null && patch.isEpicFightMode());
                state.addProperty("health", mc.player.getHealth());
                if (patch != null) {
                    state.addProperty("stamina", patch.getStamina()); state.addProperty("maxStamina", patch.getMaxStamina());
                    patch.getSkillContainerFor(EpicFightSkills.METEOR_SLAM.get()).ifPresent(skill -> {
                        state.addProperty("meteorFallDistance", yesman.epicfight.skill.identity.MeteorSlamSkill.getFallDistance(skill));
                    });
                }
                if (patch != null) state.addProperty("animation", patch.getAnimator().getPlayerFor(null).getAnimation().toString());
                JsonArray targets = new JsonArray();
                for (Entity entity : mc.level.entitiesForRendering()) if (entity instanceof Zombie zombie) {
                    JsonObject z = new JsonObject(); z.addProperty("id", zombie.getId()); z.addProperty("health", zombie.getHealth());
                    z.addProperty("position", zombie.position().toString()); targets.add(z);
                }
                state.add("targets", targets); Files.writeString(STATE, JSON.toJson(state));
                Files.writeString(Path.of("capture-history.jsonl"), new Gson().toJson(state) + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        } catch (Throwable e) { System.out.println("GRAPHICAL_PROBE_ERROR " + e); e.printStackTrace(); }
    }

    private static void act(Minecraft mc, JsonObject request) {
        String action = request.get("action").getAsString();
        System.out.println("GRAPHICAL_PROBE_ACTION " + request);
        switch (action) {
            case "attack" -> {
                mc.setScreen(null);
                var patch = EpicFightCapabilities.getLocalPlayerPatch(mc.player);
                var result = patch.getSkill(SkillSlots.COMBO_ATTACKS).sendCastRequest(patch, ControlEngine.getInstance());
                System.out.println("GRAPHICAL_PROBE_ATTACK executable=" + result.isExecutable());
            }
            case "camera" -> mc.options.setCameraType(CameraType.valueOf(request.get("value").getAsString()));
            case "look" -> {
                mc.player.setYRot(request.get("yaw").getAsFloat()); mc.player.setXRot(request.get("pitch").getAsFloat());
                mc.player.yBodyRot = mc.player.getYRot(); mc.player.yHeadRot = mc.player.getYRot();
            }
            case "capture" -> Screenshot.grab(mc.gameDirectory, request.get("name").getAsString(), mc.getMainRenderTarget(),
                    message -> System.out.println("GRAPHICAL_PROBE_SCREENSHOT " + message.getString()));
            case "close_screen" -> mc.setScreen(null);
            case "command" -> mc.getSingleplayerServer().execute(() -> mc.getSingleplayerServer().getCommands().performPrefixedCommand(
                    mc.getSingleplayerServer().createCommandSourceStack().withSuppressedOutput(), request.get("value").getAsString()));
            case "exit" -> mc.stop();
        }
    }

    private static final class PlayerSkillCommandBridge {
        private static void meteor(ServerPlayer player) {
            player.server.getCommands().performPrefixedCommand(player.server.createCommandSourceStack().withSuppressedOutput(),
                    "epicfight skill add " + player.getGameProfile().getName() + " identity epicfight:meteor_slam");
        }
    }
}
