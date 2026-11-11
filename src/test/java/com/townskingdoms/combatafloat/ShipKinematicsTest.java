package com.townskingdoms.combatafloat;

import dev.ryanhcode.sable.companion.math.Pose3d;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ShipKinematicsTest {
    private static void same(Vector3d expected, Vector3d actual) {
        assertEquals(expected.x, actual.x, 1e-7);
        assertEquals(expected.y, actual.y, 1e-7);
        assertEquals(expected.z, actual.z, 1e-7);
    }

    @Test void standingOnTranslatingShipHasNoRelativeVelocity() {
        Pose3d old = new Pose3d(), now = new Pose3d();
        old.position().set(120, 80, -250); now.position().set(140, 60, -230);
        Vector3d local = new Vector3d(4, 2, -7);
        same(new Vector3d(), ShipKinematics.relativeDisplacement(old, now,
                old.transformPosition(local, new Vector3d()), now.transformPosition(local, new Vector3d())));
    }

    @Test void standingOnRotatingPitchedShipHasNoRelativeFall() {
        Pose3d old = new Pose3d(), now = new Pose3d();
        old.rotationPoint().set(20_480_512, 120, 20_480_512);
        now.rotationPoint().set(old.rotationPoint());
        old.position().set(1000, 120, 2000); now.position().set(1001, 119, 2003);
        old.orientation().rotationXYZ(.15, .4, -.1);
        now.orientation().rotationXYZ(.35, .8, .2);
        Vector3d local = new Vector3d(20_480_518, 122, 20_480_517);
        same(new Vector3d(), ShipKinematics.relativeDisplacement(old, now,
                old.transformPosition(local, new Vector3d()), now.transformPosition(local, new Vector3d())));
    }

    @Test void walkingDeltaSurvivesShipMotion() {
        Pose3d old = new Pose3d(), now = new Pose3d();
        now.position().set(12, -5, 16); now.orientation().rotationY(Math.PI / 2);
        Vector3d previous = new Vector3d(4, 2, 3), delta = new Vector3d(.2, -.05, .1);
        same(now.transformNormal(delta, new Vector3d()), ShipKinematics.relativeDisplacement(old, now,
                old.transformPosition(previous, new Vector3d()),
                now.transformPosition(new Vector3d(previous).add(delta), new Vector3d())));
    }
}
