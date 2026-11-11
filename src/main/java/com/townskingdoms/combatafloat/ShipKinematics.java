package com.townskingdoms.combatafloat;

import dev.ryanhcode.sable.companion.math.Pose3dc;
import org.joml.Vector3d;

public final class ShipKinematics {
    private ShipKinematics() {}

    public static Vector3d relativeDisplacement(Pose3dc previousPose, Pose3dc currentPose,
            Vector3d previousWorld, Vector3d currentWorld) {
        Vector3d oldLocal = previousPose.transformPositionInverse(previousWorld, new Vector3d());
        Vector3d newLocal = currentPose.transformPositionInverse(currentWorld, new Vector3d());
        return currentPose.transformNormal(newLocal.sub(oldLocal));
    }
}
