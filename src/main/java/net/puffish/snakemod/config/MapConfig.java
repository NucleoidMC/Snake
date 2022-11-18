package net.puffish.snakemod.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public record MapConfig(Identifier id, int time) {
	public static final Codec<MapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("id").forGetter(MapConfig::id),
			Codec.INT.optionalFieldOf("time", 6000).forGetter(MapConfig::time)
	).apply(instance, MapConfig::new));
}
