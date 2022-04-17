package com.github.maxopoly.finale.combat.knockback;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.bukkit.util.Vector;

public interface KnockbackStrategy {

	void handleKnockback(Player attacker, Entity victim, int knockbackLevel);

}
