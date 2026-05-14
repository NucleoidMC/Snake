package net.puffish.snakemod.game;

import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import net.puffish.snakemod.callbacks.EliminateCallback;
import net.puffish.snakemod.game.entity.SnakePartEntity;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.api.util.ItemStackBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Stack;

public class SnakePlayer {
	private static final int SEPARATION = 3;
	private static final float SPEED = 5.0f / 9.0f;
	private static final float TURNING = 15.0f;

	private final ServerLevel level;
	private final ServerPlayer player;
	private final DyeColor color;
	private final LinkedList<Vec3> path;
	private final Stack<SnakePartEntity> entities;

	private int length;
	private int kills;
	private boolean dead;

	private SnakePlayer(ServerLevel level, ServerPlayer player, DyeColor color) {
		this.level = level;
		this.player = player;
		this.color = color;
		this.path = new LinkedList<>();
		this.entities = new Stack<>();
		this.length = 5;
		this.kills = 0;
		this.dead = false;
	}

	public static SnakePlayer setup(ServerLevel level, ServerPlayer player, DyeColor color, Vec3 pos) {
		var snakePlayer = new SnakePlayer(level, player, color);

		var entity = snakePlayer.createAndSpawnPart(pos, player.getYRot());
		player.startRiding(entity, true, true);
		snakePlayer.entities.push(entity);

		return snakePlayer;
	}

	public Vec3 getHeadPos() {
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
				float deltaYaw = Mth.degreesDifference(entity.getYRot(), player.getYRot());
				float yaw = entity.getYRot() + Mth.clamp(deltaYaw, -TURNING, TURNING);

				entity.setYRot(yaw);
				entity.setYHeadRot(yaw);
				entity.setYBodyRot(yaw);

				if (move) {
					entity.setDeltaMovement(
							-Mth.sin(yaw * Mth.DEG_TO_RAD) * SPEED,
							entity.getDeltaMovement().y,
							Mth.cos(yaw * Mth.DEG_TO_RAD) * SPEED
					);

					path.addFirst(entity.position());
				}
			} else {
				Vec3 target = path.get(index * SEPARATION - 1);

				entity.setDeltaMovement(
						target.x - entity.position().x,
						entity.getDeltaMovement().y,
						target.z - entity.position().z
				);

				float yaw = Mth.wrapDegrees(
						(float) Mth.atan2(
								entity.getDeltaMovement().z,
								entity.getDeltaMovement().x
						) * Mth.RAD_TO_DEG
				) - 90.0f;

				entity.setYRot(yaw);
				entity.setYHeadRot(yaw);
				entity.setYBodyRot(yaw);
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

	public void checkBounds(BlockBounds bounds, EliminateCallback eliminateCallback) {
		if (dead) {
			return;
		}

		if (!bounds.contains(BlockPos.containing(getHeadPos()))) {
			eliminateCallback.accept(this.player, this.player);
			kill();
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
			if (checkCollision(otherSnake)) {
				if (this != otherSnake) {
					otherSnake.kills++;
				}
				eliminateCallback.accept(otherSnake.player, this.player);
				kill();
				break;
			}
		}
	}

	private boolean checkCollision(SnakePlayer otherSnake) {
		var minSquaredDistance = 4.0 * SnakePartEntity.RADIUS * SnakePartEntity.RADIUS;

		boolean first = true;
		for (var entity : otherSnake.entities) {
			if (this == otherSnake && first) {
				first = false;
			} else if (getHeadPos().distanceToSqr(entity.getCenter()) < minSquaredDistance) {
				return true;
			}
		}

		return false;
	}

	private void kill() {
		for (var entity : entities) {
			entity.kill(this.level);
		}
		this.player.setGameMode(GameType.SPECTATOR);
		this.dead = true;
	}

	public void remove(){
		for (var entity : entities) {
			entity.remove(Entity.RemovalReason.DISCARDED);
		}
	}

	public void spawnFirework() {
		var pos = getHeadPos();
		var item = ItemStackBuilder.firework(color.getFireworkColor(), 1, FireworkExplosion.Shape.SMALL_BALL).build();
		var entity = new FireworkRocketEntity(level, pos.x, pos.y, pos.z, item);
		level.addFreshEntity(entity);
	}

	private SnakePartEntity createPart(Vec3 pos) {
		var entity = SnakePartEntity.create(level);
		entity.setColor(color);
		entity.setPos(pos);
		return entity;
	}

	private SnakePartEntity spawnPart(SnakePartEntity entity) {
		level.addFreshEntity(entity);
		return entity;
	}

	private SnakePartEntity createAndSpawnPart(Vec3 pos, float yaw) {
		var entity = createPart(pos);
		entity.setYRot(yaw);
		entity.setYHeadRot(yaw);
		entity.setYBodyRot(yaw);
		return spawnPart(entity);
	}

	public ServerPlayer getPlayer() {
		return player;
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
