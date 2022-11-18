package net.puffish.snakemod.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.puffish.snakemod.callbacks.EliminateCallback;
import net.puffish.snakemod.game.map.SnakeMap;
import xyz.nucleoid.plasmid.game.player.PlayerOps;

import java.util.*;

public class SnakeManager {
	private final Object2ObjectMap<ServerPlayerEntity, SnakePlayer> snakes;

	private SnakeManager(Object2ObjectMap<ServerPlayerEntity, SnakePlayer> snakes) {
		this.snakes = snakes;
	}

	public static SnakeManager create(ServerWorld world, Collection<ServerPlayerEntity> players, SnakeMap map, Random random) {
		var snakes = new Object2ObjectOpenHashMap<ServerPlayerEntity, SnakePlayer>();

		var spawns = new ArrayList<>(map.getSpawns());
		Collections.shuffle(spawns, random);

		var colors = new ArrayList<>(List.of(DyeColor.values()));
		Collections.shuffle(colors, random);

		int index = 0;
		for (var player : players) {
			snakes.put(player, SnakePlayer.setup(
					world,
					player,
					colors.get(index % colors.size()),
					spawns.get(index)
			));
			index++;
		}

		return new SnakeManager(snakes);
	}

	public void tickStarting(PlayerOps playerOps) {
		snakes.values().forEach(p -> p.tick(playerOps, false));
	}

	public void tickPlaying(PlayerOps playerOps, EliminateCallback eliminateCallback) {
		snakes.values().forEach(p -> p.checkCollisions(snakes.values(), eliminateCallback));
		snakes.values().forEach(p -> p.tick(playerOps, true));
	}

	public void removePlayer(ServerPlayerEntity player) {
		snakes.remove(player);
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

	public Optional<SnakePlayer> getSnake(ServerPlayerEntity player) {
		return Optional.ofNullable(snakes.get(player));
	}

	public Collection<SnakePlayer> getSnakes() {
		return snakes.values();
	}
}
