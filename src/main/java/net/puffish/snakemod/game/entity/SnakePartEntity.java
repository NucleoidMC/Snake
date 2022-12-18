package net.puffish.snakemod.game.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SnakePartEntity extends SheepEntity {
	public static final double RADIUS = 0.55;

	protected SnakePartEntity(World world) {
		super(EntityType.SHEEP, world);
	}

	public static SnakePartEntity create(World world){
		var entity = new SnakePartEntity(world);
		entity.init();
		return entity;
	}

	protected void init(){
		this.goalSelector.getGoals().clear();
		this.setInvulnerable(true);
		this.setPersistent();
	}

	public void updateSimpleMovement(){
		this.move(MovementType.SELF, this.getVelocity());

		Vec3d vel = this.getVelocity();

		double y = vel.y;
		if (this.horizontalCollision && this.isClimbing()) {
			y = 0.2;
		}
		y -= 0.16;

		this.setVelocity(vel.x, y, vel.z);

		this.velocityDirty = true;
		this.velocityModified = true;
	}

	@Override
	public void tick() {
		this.baseTick();
	}

	public Vec3d getCenter() {
		return getPos().add(0, getHeight(), 0);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return null;
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
