package net.puffish.snakemod.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.puffish.snakemod.callbacks.EliminateCallback;
import net.puffish.snakemod.game.map.SnakeMap;
import xyz.nucleoid.map_templates.BlockBounds;

import java.util.*;

public class SnakeManager {
	private final Object2ObjectMap<ServerPlayer, SnakePlayer> snakes;
	private final BlockBounds bounds;

	private SnakeManager(Object2ObjectMap<ServerPlayer, SnakePlayer> snakes, BlockBounds bounds) {
		this.snakes = snakes;
		this.bounds = bounds;
	}

	public static SnakeManager create(ServerLevel level, Collection<ServerPlayer> players, SnakeMap map, Random random) {
		var snakes = new Object2ObjectOpenHashMap<ServerPlayer, SnakePlayer>();

		var spawns = new ArrayList<>(map.getSpawns());
		Collections.shuffle(spawns, random);

		var colors = new ArrayList<>(List.of(DyeColor.values()));
		Collections.shuffle(colors, random);

		int index = 0;
		for (var player : players) {
			snakes.put(player, SnakePlayer.setup(
					level,
					player,
					colors.get(index % colors.size()),
					spawns.get(index)
			));
			index++;
		}

		return new SnakeManager(snakes, map.getBounds());
	}

	public void tickStarting() {
		snakes.values().forEach(p -> p.tick(false));
	}

	public void tickPlaying(EliminateCallback eliminateCallback) {
		snakes.values().forEach(p -> p.checkCollisions(snakes.values(), eliminateCallback));
		snakes.values().forEach(p -> p.checkBounds(bounds, eliminateCallback));
		snakes.values().forEach(p -> p.tick(true));
	}

	public void removePlayer(ServerPlayer player) {
		Optional.ofNullable(snakes.remove(player)).ifPresent(SnakePlayer::remove);
	}

	public int getCount() {
		return snakes.size();
	}

	public int getAliveCount() {
		return (int) snakes.values()
				.stream()
				.filter(SnakePlayer::isAlive)
				.count();
	}

	public int getDeadCount() {
		return (int) snakes.values()
				.stream()
				.filter(SnakePlayer::isDead)
				.count();
	}

	public Optional<SnakePlayer> getSnake(ServerPlayer player) {
		return Optional.ofNullable(snakes.get(player));
	}

	public Collection<SnakePlayer> getAliveSnakes() {
		return snakes.values().stream().filter(SnakePlayer::isAlive).toList();
	}
}
