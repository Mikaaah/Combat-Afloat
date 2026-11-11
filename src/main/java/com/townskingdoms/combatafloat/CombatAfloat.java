package com.townskingdoms.combatafloat;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;

@Mod(CombatAfloat.MOD_ID)
public final class CombatAfloat {
    public static final String MOD_ID = "combatafloat";
    public CombatAfloat(IEventBus modBus) {
        modBus.addListener(ShipSlamPayload::register);
    }
}
