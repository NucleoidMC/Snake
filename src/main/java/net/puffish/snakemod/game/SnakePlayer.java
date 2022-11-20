package net.puffish.snakemod.game;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.puffish.snakemod.callbacks.EliminateCallback;
import net.puffish.snakemod.game.entity.SnakePartEntity;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Stack;

public class SnakePlayer {
	private static final int SEPARATION = 2;
	private static final double SPEED = 25.0 / 36.0;

	private final ServerWorld world;
	private final ServerPlayerEntity player;
	private final DyeColor color;
	private final LinkedList<Vec3d> path;
	private final Stack<SnakePartEntity> entities;

	private int length;
	private int kills;
	private boolean dead;

	private SnakePlayer(ServerWorld world, ServerPlayerEntity player, DyeColor color) {
		this.world = world;
		this.player = player;
		this.color = color;
		this.path = new LinkedList<>();
		this.entities = new Stack<>();
		this.length = 5;
		this.kills = 0;
		this.dead = false;
	}

	public static SnakePlayer setup(ServerWorld world, ServerPlayerEntity player, DyeColor color, Vec3d pos) {
		var snakePlayer = new SnakePlayer(world, player, color);

		var entity = snakePlayer.createAndSpawnPart(pos, player.getYaw());
		player.startRiding(entity, true);
		snakePlayer.entities.push(entity);

		return snakePlayer;
	}

	public Vec3d getHeadPos() {
		return entities.firstElement().getCenter();
	}

	public void grow() {
		this.length++;
	}

	public void tick(boolean move) {
		if (dead) {
			return;
		}

		var newEntities = new ArrayList<SnakePartEntity>();
		while (entities.size() < length && path.size() >= entities.size() * SEPARATION) {
			newEntities.add(entities.push(createPart(path.get(entities.size() * SEPARATION - 1))));
		}
		while (entities.size() > length) {
			entities.pop().remove(Entity.RemovalReason.DISCARDED);
		}

		int index = 0;
		for (var entity : entities) {
			if (index == 0) {
				float yaw = player.getYaw();

				entity.setYaw(yaw);
				entity.setHeadYaw(yaw);

				if (move) {
					entity.setVelocity(
							-MathHelper.sin(yaw * MathHelper.RADIANS_PER_DEGREE) * SPEED,
							entity.getVelocity().y,
							MathHelper.cos(yaw * MathHelper.RADIANS_PER_DEGREE) * SPEED
					);

					path.addFirst(entity.getPos());
				}
			} else {
				Vec3d target = path.get(index * SEPARATION - 1);

				entity.setVelocity(
						target.x - entity.getPos().x,
						entity.getVelocity().y,
						target.z - entity.getPos().z
				);

				float yaw = MathHelper.wrapDegrees(
						(float) MathHelper.atan2(
								entity.getVelocity().z,
								entity.getVelocity().x
						) * MathHelper.DEGREES_PER_RADIAN
				) - 90.0f;

				entity.setYaw(yaw);
				entity.setHeadYaw(yaw);
			}

			index++;
		}

		while (path.size() > entities.size() * SEPARATION) {
			path.removeLast();
		}

		newEntities.forEach(this::spawnPart);

		for(var entity : entities){
			entity.updateSimpleMovement();
		}
	}

	public void checkCollisions(Collection<SnakePlayer> otherSnakes, EliminateCallback eliminateCallback) {
		if (dead) {
			return;
		}

		for (var otherSnake : otherSnakes) {
			if (otherSnake.dead) {
				continue;
			}
			if (checkCollision(otherSnake, eliminateCallback)) {
				kill();
				break;
			}
		}
	}

	private boolean checkCollision(SnakePlayer otherSnake, EliminateCallback eliminateCallback) {
		var minSquaredDistance = 4.0 * SnakePartEntity.RADIUS * SnakePartEntity.RADIUS;

		int index = 0;
		for (var entity : otherSnake.entities) {
			if (this != otherSnake || index != 0) {
				if (getHeadPos().squaredDistanceTo(entity.getCenter()) < minSquaredDistance) {
					eliminateCallback.accept(otherSnake.player, this.player);
					this.player.changeGameMode(GameMode.SPECTATOR);
					otherSnake.kills++;
					return true;
				}
			}

			index++;
		}

		return false;
	}

	private void kill() {
		for (var entity : entities) {
			entity.kill();
		}
		this.dead = true;
	}

	public void remove(){
		for (var entity : entities) {
			entity.remove(Entity.RemovalReason.DISCARDED);
		}
	}

	public void spawnFirework() {
		var pos = getHeadPos();
		var item = ItemStackBuilder.firework(color.getFireworkColor(), 1, FireworkRocketItem.Type.SMALL_BALL).build();
		var entity = new FireworkRocketEntity(world, pos.x, pos.y, pos.z, item);
		world.spawnEntity(entity);
	}

	private SnakePartEntity createPart(Vec3d pos) {
		var entity = SnakePartEntity.create(world);
		entity.setColor(color);
		entity.setPosition(pos);
		return entity;
	}

	private SnakePartEntity spawnPart(SnakePartEntity entity) {
		world.spawnEntity(entity);
		return entity;
	}

	private SnakePartEntity createAndSpawnPart(Vec3d pos, float yaw) {
		var entity = createPart(pos);
		entity.setYaw(yaw);
		entity.setHeadYaw(yaw);
		return spawnPart(entity);
	}

	public boolean isDead() {
		return dead;
	}

	public boolean isAlive() {
		return !dead;
	}

	public int getLength() {
		return length;
	}

	public int getKills() {
		return kills;
	}
}
