package net.puffish.snakemod.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;

public record SnakeConfig(PlayerConfig players, MapConfig map) {
	public static final Codec<SnakeConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.players),
			MapConfig.CODEC.fieldOf("map").forGetter(SnakeConfig::map)
	).apply(instance, SnakeConfig::new));
}
