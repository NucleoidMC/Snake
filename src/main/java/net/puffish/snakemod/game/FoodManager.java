package net.puffish.snakemod.game;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.puffish.snakemod.game.entity.SnakeFoodEntity;
import net.puffish.snakemod.game.entity.SnakePartEntity;
import net.puffish.snakemod.game.map.SnakeMap;

import java.util.*;
import java.util.stream.StreamSupport;

public class FoodManager {
	private final ServerWorld world;
	private final List<BlockPos> positions;
	private final int minCount;
	private final Random random;
	private final List<SnakeFoodEntity> entities;

	private FoodManager(ServerWorld world, List<BlockPos> positions, int minCount, Random random) {
		this.world = world;
		this.positions = positions;
		this.minCount = minCount;
		this.random = random;
		this.entities = new ArrayList<>();
	}

	public static FoodManager create(ServerWorld world, SnakeMap map, Random random, float density) {
		var regions = map.getFoodSpawns();

		var positions = new ArrayList<>(regions.stream()
				.flatMap(
						region -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(region.getBounds().iterator(), 0), false)
								.map(BlockPos::toImmutable)
				)
				.toList());

		return new FoodManager(
				world,
				positions,
				(int) (((float) positions.size()) * density),
				random
		);
	}

	public void tick(Collection<SnakePlayer> snakes) {
		spawnMore();
		checkCollisions(snakes);
	}

	private void spawnMore() {
		while (entities.size() < minCount) {
			entities.add(spawnFood(Vec3d.ofBottomCenter(
					positions.remove(random.nextInt(positions.size()))
			)));
		}
	}

	private void checkCollisions(Collection<SnakePlayer> snakes) {
		entities.removeIf(foodEntity -> checkCollisions(foodEntity, snakes));
	}

	private boolean checkCollisions(SnakeFoodEntity entity, Collection<SnakePlayer> snakes) {
		var minSquaredDistance = (SnakePartEntity.RADIUS + SnakeFoodEntity.RADIUS) * (SnakePartEntity.RADIUS + SnakeFoodEntity.RADIUS);

		for (var snake : snakes) {
			var pos = snake.getHeadPos();

			if (entity.getCenter().squaredDistanceTo(pos) < minSquaredDistance) {
				positions.add(entity.getBlockPos());
				entity.remove(Entity.RemovalReason.DISCARDED);
				snake.grow();

				world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 1.0f, random.nextFloat() * 0.1f + 0.9f);

				return true;
			}
		}

		return false;
	}

	private SnakeFoodEntity spawnFood(Vec3d pos) {
		var entity = SnakeFoodEntity.create(world);
		entity.setPosition(pos);
		world.spawnEntity(entity);
		return entity;
	}
}
