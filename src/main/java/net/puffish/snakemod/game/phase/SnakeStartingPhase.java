package net.puffish.snakemod.game.phase;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.ChatFormatting;
import net.puffish.snakemod.SnakeMod;
import net.puffish.snakemod.game.FoodManager;
import net.puffish.snakemod.game.ScoreboardManager;
import net.puffish.snakemod.game.SnakeManager;
import net.puffish.snakemod.game.map.SnakeMap;
import xyz.nucleoid.plasmid.api.game.GameSpace;

import java.util.Random;

public class SnakeStartingPhase extends SnakeActivePhase {
	private int countdown = 4 * 20;

	protected SnakeStartingPhase(GameSpace gameSpace, ServerLevel level, SnakeMap map, SnakeManager snakeManager, FoodManager foodManager, ScoreboardManager scoreboardManager) {
		super(gameSpace, level, map, snakeManager, foodManager, scoreboardManager);
	}

	public static SnakeStartingPhase create(GameSpace gameSpace, SnakeWaitingPhase oldPhase) {
		var level = oldPhase.level;
		var map = oldPhase.map;

		var random = new Random();

		var players = gameSpace.getPlayers()
				.stream()
				.toList();

		var snakeManager = SnakeManager.create(level, players, map, random);
		var foodManager = FoodManager.create(level, map, random, 0.01f);
		var scoreboardManager = ScoreboardManager.create();

		return new SnakeStartingPhase(
				gameSpace,
				level,
				map,
				snakeManager,
				foodManager,
				scoreboardManager
		);
	}

	public static void open(SnakeWaitingPhase oldPhase) {
		oldPhase.gameSpace.setActivity(activity -> {
			var phase = SnakeStartingPhase.create(activity.getGameSpace(), oldPhase);

			phase.applyRules(activity);
			phase.applyListeners(activity);
		});
	}

	protected void tick() {
		foodManager.tick(snakeManager.getAliveSnakes());
		snakeManager.tickStarting();

		super.tick();

		var players = gameSpace.getPlayers();

		if (countdown % 20 == 0) {
			int seconds = countdown / 20;
			if (seconds <= 3) {
				players.showTitle(SnakeMod.createTranslatable(
						"text",
						"countdown." + seconds
				).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), 10, 20, 10);
				players.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 1.0f, seconds == 0 ? 2.0f : 1.0f);
			}
			if (seconds == 0) {
				SnakePlayingPhase.open(this);
			}
		}
		countdown--;
	}
}
