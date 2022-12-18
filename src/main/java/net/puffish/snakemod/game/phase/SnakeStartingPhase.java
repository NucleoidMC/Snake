package net.puffish.snakemod.game.phase;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.puffish.snakemod.SnakeMod;
import net.puffish.snakemod.game.FoodManager;
import net.puffish.snakemod.game.ScoreboardManager;
import net.puffish.snakemod.game.SnakeManager;
import net.puffish.snakemod.game.map.SnakeMap;
import xyz.nucleoid.plasmid.game.GameSpace;

import java.util.Random;

public class SnakeStartingPhase extends SnakeActivePhase {
	private int countdown = 4 * 20;

	protected SnakeStartingPhase(GameSpace gameSpace, ServerWorld world, SnakeMap map, SnakeManager snakeManager, FoodManager foodManager, ScoreboardManager scoreboardManager) {
		super(gameSpace, world, map, snakeManager, foodManager, scoreboardManager);
	}

	public static SnakeStartingPhase create(GameSpace gameSpace, SnakeWaitingPhase oldPhase) {
		var world = oldPhase.world;
		var map = oldPhase.map;

		var random = new Random();

		var players = gameSpace.getPlayers()
				.stream()
				.toList();

		var snakeManager = SnakeManager.create(world, players, map, random);
		var foodManager = FoodManager.create(world, map, random, 0.01f);
		var scoreboardManager = ScoreboardManager.create();

		return new SnakeStartingPhase(
				gameSpace,
				world,
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
		foodManager.tick(snakeManager.getSnakes());
		snakeManager.tickStarting();

		super.tick();

		var players = gameSpace.getPlayers();

		if (countdown % 20 == 0) {
			int seconds = countdown / 20;
			if (seconds <= 3) {
				players.showTitle(SnakeMod.createTranslatable("text", "countdown." + seconds), 10, 20, 10);
				players.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.PLAYERS, 1.0f, seconds == 0 ? 2.0f : 1.0f);
			}
			if (seconds == 0) {
				SnakePlayingPhase.open(this);
			}
		}
		countdown--;
	}
}
