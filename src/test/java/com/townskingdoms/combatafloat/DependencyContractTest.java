package com.townskingdoms.combatafloat;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Checks the published dependency bytecode without initializing Minecraft/client classes. */
class DependencyContractTest {
    private static ClassNode read(String name) throws Exception {
        try (InputStream in = DependencyContractTest.class.getClassLoader().getResourceAsStream(name + ".class")) {
            assertNotNull(in, "Class missing: " + name);
            ClassNode node = new ClassNode(); new ClassReader(in).accept(node, 0); return node;
        }
    }

    private static Map<String, Object> values(AnnotationNode annotation) {
        Map<String, Object> result = new HashMap<>();
        if (annotation.values != null) for (int i = 0; i < annotation.values.size(); i += 2)
            result.put((String) annotation.values.get(i), annotation.values.get(i + 1));
        return result;
    }

    private static List<AnnotationNode> annotations(List<AnnotationNode> visible, List<AnnotationNode> invisible) {
        List<AnnotationNode> result = new ArrayList<>();
        if (visible != null) result.addAll(visible); if (invisible != null) result.addAll(invisible); return result;
    }

    @Test void everyInjectionMatchesPublishedEpicFight() throws Exception {
        int checked = 0;
        for (String name : List.of("LevelUtilMixin", "MeteorSlamSkillMixin", "MeteorAnimationMixin", "MeteorMovementMixin", "client/AnimationPositionMixin", "client/PlayerMotionMixin", "client/TrailParticleMixin")) {
            ClassNode mixin = read("com/townskingdoms/combatafloat/mixin/" + name);
            AnnotationNode definition = annotations(mixin.visibleAnnotations, mixin.invisibleAnnotations).stream()
                    .filter(a -> a.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")).findFirst().orElseThrow();
            Map<String, Object> metadata = values(definition);
            String targetName = metadata.containsKey("value")
                    ? ((Type) ((List<?>) metadata.get("value")).getFirst()).getInternalName()
                    : ((String) ((List<?>) metadata.get("targets")).getFirst()).replace('.', '/');
            ClassNode target = read(targetName);
            assertEquals((target.access & org.objectweb.asm.Opcodes.ACC_INTERFACE) != 0,
                    (mixin.access & org.objectweb.asm.Opcodes.ACC_INTERFACE) != 0,
                    "Mixin and target must agree on interface/class type: " + targetName);
            for (MethodNode handler : mixin.methods) {
                for (AnnotationNode annotation : annotations(handler.visibleAnnotations, handler.invisibleAnnotations)) {
                    Map<String, Object> data = values(annotation);
                    if (!data.containsKey("method")) continue;
                    List<?> selectors = data.get("method") instanceof List<?> l ? l : List.of(data.get("method"));
                    List<MethodNode> methods = target.methods.stream().filter(m -> selectors.stream()
                            .anyMatch(s -> s.equals(m.name) || s.equals(m.name + m.desc))).toList();
                    assertFalse(methods.isEmpty(), targetName + ": " + selectors);
                    if (data.containsKey("at")) {
                        Object raw = data.get("at");
                        List<?> points = raw instanceof List<?> l ? l : List.of(raw);
                        for (Object p : points) {
                            Map<String, Object> point = values((AnnotationNode) p);
                            if (!point.containsKey("target")) continue;
                            String member = (String) point.get("target");
                            int end = member.indexOf(';');
                            String owner = member.substring(1, end), rest = member.substring(end + 1);
                            boolean found = false;
                            for (MethodNode method : methods) for (AbstractInsnNode insn : method.instructions) {
                                if (insn instanceof MethodInsnNode call && call.owner.equals(owner) && (call.name + call.desc).equals(rest)) found = true;
                                if (insn instanceof FieldInsnNode field && field.owner.equals(owner) && (field.name + ":" + field.desc).equals(rest)) found = true;
                            }
                            assertTrue(found, targetName + " " + selectors + " -> " + member);
                        }
                    }
                    checked++;
                }
            }
            // A shadow's descriptor must match its actual erasure in the target hierarchy.
            for (FieldNode shadow : mixin.fields) {
                if (annotations(shadow.visibleAnnotations, shadow.invisibleAnnotations).stream().noneMatch(a -> a.desc.endsWith("/Shadow;"))) continue;
                ClassNode current = target; boolean found = false;
                while (current != null) {
                    if (current.fields.stream().anyMatch(f -> f.name.equals(shadow.name) && f.desc.equals(shadow.desc))) { found = true; break; }
                    current = current.superName == null ? null : read(current.superName);
                }
                assertTrue(found, "Missing shadow " + shadow.name + shadow.desc);
            }
        }
        assertEquals(18, checked, "Expected all compatibility hooks to be checked");
    }
}
