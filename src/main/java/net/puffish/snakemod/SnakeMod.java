package net.puffish.snakemod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.puffish.snakemod.config.SnakeConfig;
import net.puffish.snakemod.event.SnakeEvents;
import net.puffish.snakemod.game.phase.SnakeWaitingPhase;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import xyz.nucleoid.plasmid.api.game.GameTypes;

public class SnakeMod implements ModInitializer {
	public static final String ID = "snake";

	@Override
	public void onInitialize() {
		ServerTickEvents.START_LEVEL_TICK.register(world -> {
			var game = GameSpaceManager.get().byLevel(world);
			if (game != null) {
				try {
					game.getBehavior().propagatingInvoker(SnakeEvents.TICK_START).onTickStart();
				} catch (Throwable t) {
					game.close(GameCloseReason.ERRORED);
				}
			}
		});

		GameTypes.register(
				createIdentifier("snake"),
				SnakeConfig.CODEC,
				SnakeWaitingPhase::open
		);
	}

	public static Identifier createIdentifier(String path) {
		return Identifier.fromNamespaceAndPath(ID, path);
	}

	public static MutableComponent createTranslatable(String type, String path, Object... args) {
		return Component.translatable(Util.makeDescriptionId(type, createIdentifier(path)), args);
	}
}
