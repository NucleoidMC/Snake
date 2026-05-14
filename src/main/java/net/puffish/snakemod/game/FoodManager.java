package net.puffish.snakemod.game;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.puffish.snakemod.game.entity.SnakeFoodEntity;
import net.puffish.snakemod.game.entity.SnakePartEntity;
import net.puffish.snakemod.game.map.SnakeMap;
import xyz.nucleoid.map_templates.BlockBounds;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FoodManager {
	private final ServerLevel level;
	private final List<Vec3> positions;
	private final int minCount;
	private final Random random;
	private final List<SnakeFoodEntity> entities;

	private FoodManager(ServerLevel level, List<Vec3> positions, int minCount, Random random) {
		this.level = level;
		this.positions = positions;
		this.minCount = minCount;
		this.random = random;
		this.entities = new ArrayList<>();
	}

	public static FoodManager create(ServerLevel level, SnakeMap map, Random random, float density) {
		var regions = map.getFoodSpawns();

		var positions = regions.stream().flatMap(region -> {
			var bounds = region.getBounds();
			return StreamSupport.stream(new BlockBounds(
					bounds.min().below(),
					bounds.max()
			).spliterator(), false);
		}).map(BlockPos::immutable).distinct().flatMap(pos -> {
			var state = level.getBlockState(pos);
			var shape = state.getCollisionShape(level, pos);
			if(shape.toAabbs().size() == 1){
				var box = shape.toAabbs().get(0);
				if(box.minX == 0.0 && box.minZ == 0.0 && box.maxX == 1.0 && box.maxZ == 1.0){
					if(level.getBlockState(pos.above()).isAir()){
						return Stream.of(Vec3.upFromBottomCenterOf(pos, box.maxY));
					}
				}
			}
			return Stream.empty();
		}).collect(Collectors.toCollection(ArrayList::new));

		return new FoodManager(
				level,
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
			entities.add(spawnFood(
					positions.remove(random.nextInt(positions.size()))
			));
		}
	}

	private void checkCollisions(Collection<SnakePlayer> snakes) {
		entities.removeIf(foodEntity -> checkCollisions(foodEntity, snakes));
	}

	private boolean checkCollisions(SnakeFoodEntity entity, Collection<SnakePlayer> snakes) {
		var minSquaredDistance = (SnakePartEntity.RADIUS + SnakeFoodEntity.RADIUS) * (SnakePartEntity.RADIUS + SnakeFoodEntity.RADIUS);

		for (var snake : snakes) {
			var pos = snake.getHeadPos();

			if (entity.getCenter().distanceToSqr(pos) < minSquaredDistance) {
				positions.add(entity.position());
				entity.remove(Entity.RemovalReason.DISCARDED);
				snake.grow();

				level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1.0f, random.nextFloat() * 0.1f + 0.9f);

				return true;
			}
		}

		return false;
	}

	private SnakeFoodEntity spawnFood(Vec3 pos) {
		var entity = SnakeFoodEntity.create(level);
		entity.setPos(pos);
		var yaw = random.nextFloat() * 360f;
		entity.setYRot(yaw);
		entity.setYHeadRot(yaw);
		entity.setYBodyRot(yaw);
		level.addFreshEntity(entity);
		return entity;
	}
}
