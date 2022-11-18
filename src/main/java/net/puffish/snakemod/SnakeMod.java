package net.puffish.snakemod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.puffish.snakemod.config.SnakeConfig;
import net.puffish.snakemod.game.phase.SnakeWaitingPhase;
import xyz.nucleoid.plasmid.game.GameType;

public class SnakeMod implements ModInitializer {
	public static final String ID = "snake";

	@Override
	public void onInitialize() {
		GameType.register(
				createIdentifier("snake"),
				SnakeConfig.CODEC,
				SnakeWaitingPhase::open
		);
	}

	public static Identifier createIdentifier(String path) {
		return new Identifier(ID, path);
	}

	public static Text createTranslatable(String type, String path, Object... args) {
		return Text.translatable(Util.createTranslationKey(type, createIdentifier(path)), args);
	}
}
