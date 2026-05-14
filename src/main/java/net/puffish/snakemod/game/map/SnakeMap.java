package net.puffish.snakemod.game.map;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.puffish.snakemod.config.MapConfig;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.api.game.level.generator.TemplateChunkGenerator;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

public class SnakeMap {
	private final MapTemplate template;
	private final List<Vec3> waitingSpawns;
	private final List<Vec3> spawns;
	private final List<TemplateRegion> foodSpawns;

	private SnakeMap(MapTemplate template, List<Vec3> waitingSpawns, List<Vec3> spawns, List<TemplateRegion> foodSpawns) {
		this.template = template;
		this.waitingSpawns = waitingSpawns;
		this.spawns = spawns;
		this.foodSpawns = foodSpawns;
	}

	public static Either<SnakeMap, Exception> create(MinecraftServer server, MapConfig config) {
		MapTemplate mapTemplate;
		try {
			mapTemplate = MapTemplateSerializer.loadFromResource(server, config.id());
		} catch (IOException e) {
			return Either.right(e);
		}

		var spawns = regionsToCenters(
				mapTemplate.getMetadata()
						.getRegions("spawn")
		).toList();

		if (spawns.isEmpty()) {
			return Either.right(new RuntimeException("No spawns!"));
		}

		var waitingSpawns = regionsToCenters(
				mapTemplate.getMetadata()
						.getRegions("waiting_spawn")
		).toList();

		if (waitingSpawns.isEmpty()) {
			waitingSpawns = spawns;
		}

		var foodSpawns = mapTemplate.getMetadata()
				.getRegions("food_spawn")
				.toList();

		return Either.left(new SnakeMap(mapTemplate, waitingSpawns, spawns, foodSpawns));
	}

	private static Stream<Vec3> regionsToCenters(Stream<TemplateRegion> stream) {
		return stream.map(TemplateRegion::getBounds)
				.map(BlockBounds::center);
	}

	public List<Vec3> getWaitingSpawns() {
		return waitingSpawns;
	}

	public List<Vec3> getSpawns() {
		return spawns;
	}

	public List<TemplateRegion> getFoodSpawns() {
		return foodSpawns;
	}

	public BlockBounds getBounds() {
		return template.getBounds();
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, template);
	}
}
