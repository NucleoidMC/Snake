package net.puffish.snakemod.game.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SnakeFoodEntity extends SlimeEntity {
	public static final double RADIUS = 0.5;

	protected SnakeFoodEntity(World world) {
		super(EntityType.SLIME, world);
	}

	public static SnakeFoodEntity create(World world){
		var entity = new SnakeFoodEntity(world);
		entity.init();
		return entity;
	}

	protected void init(){
		this.goalSelector.getGoals().clear();
		this.setInvulnerable(true);
		this.setAiDisabled(true);
		this.setPersistent();
	}

	@Override
	public void tick() {

	}

	public Vec3d getCenter() {
		return getPos().add(0, getHeight(), 0);
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected boolean shouldDropLoot() {
		return false;
	}

	@Override
	public boolean shouldDropXp() {
		return false;
	}
}
