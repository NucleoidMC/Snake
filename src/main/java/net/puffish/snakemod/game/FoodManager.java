package net.puffish.snakemod.game;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.puffish.snakemod.game.entity.SnakeFoodEntity;
import net.puffish.snakemod.game.entity.SnakePartEntity;
import net.puffish.snakemod.game.map.SnakeMap;
import xyz.nucleoid.map_templates.BlockBounds;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FoodManager {
	private final ServerWorld world;
	private final List<Vec3d> positions;
	private final int minCount;
	private final Random random;
	private final List<SnakeFoodEntity> entities;

	private FoodManager(ServerWorld world, List<Vec3d> positions, int minCount, Random random) {
		this.world = world;
		this.positions = positions;
		this.minCount = minCount;
		this.random = random;
		this.entities = new ArrayList<>();
	}

	public static FoodManager create(ServerWorld world, SnakeMap map, Random random, float density) {
		var regions = map.getFoodSpawns();

		var positions = regions.stream().flatMap(region -> {
			var bounds = region.getBounds();
			return StreamSupport.stream(new BlockBounds(
					bounds.min().down(),
					bounds.max()
			).spliterator(), false);
		}).distinct().flatMap(pos -> {
			var state = world.getBlockState(pos);
			var shape = state.getCollisionShape(world, pos);
			if(shape.getBoundingBoxes().size() == 1){
				var box = shape.getBoundingBoxes().get(0);
				if(box.minX == 0.0 && box.minZ == 0.0 && box.maxX == 1.0 && box.maxZ == 1.0){
					if(world.getBlockState(pos.up()).isAir()){
						return Stream.of(Vec3d.ofCenter(pos, box.maxY));
					}
				}
			}
			return Stream.empty();
		}).collect(Collectors.toCollection(ArrayList::new));

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

			if (entity.getCenter().squaredDistanceTo(pos) < minSquaredDistance) {
				positions.add(entity.getPos());
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
		entity.setYaw(random.nextFloat() * 360f);
		world.spawnEntity(entity);
		return entity;
	}
}
