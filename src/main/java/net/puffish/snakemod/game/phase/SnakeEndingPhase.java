package net.puffish.snakemod.game.phase;

import net.minecraft.server.world.ServerWorld;
import net.puffish.snakemod.SnakeMod;
import net.puffish.snakemod.game.FoodManager;
import net.puffish.snakemod.game.ScoreboardManager;
import net.puffish.snakemod.game.SnakeManager;
import net.puffish.snakemod.game.SnakePlayer;
import net.puffish.snakemod.game.map.SnakeMap;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;

public class SnakeEndingPhase extends SnakeActivePhase {
	private int countdown = 5 * 20;

	protected SnakeEndingPhase(GameSpace gameSpace, ServerWorld world, SnakeMap map, SnakeManager snakeManager, FoodManager foodManager, ScoreboardManager scoreboardManager) {
		super(gameSpace, world, map, snakeManager, foodManager, scoreboardManager);
	}

	public static SnakeEndingPhase create(GameSpace gameSpace, SnakeActivePhase oldPhase) {
		return new SnakeEndingPhase(
				gameSpace,
				oldPhase.world,
				oldPhase.map,
				oldPhase.snakeManager,
				oldPhase.foodManager,
				oldPhase.scoreboardManager
		);
	}

	public static void open(SnakeActivePhase oldPhase) {
		oldPhase.gameSpace.setActivity(activity -> {
			var phase = SnakeEndingPhase.create(activity.getGameSpace(), oldPhase);

			phase.applyRules(activity);
			phase.applyListeners(activity);

			phase.start();
		});
	}

	private void start(){
		snakeManager.getAliveSnakes()
				.forEach(winner -> gameSpace.getPlayers().sendMessage(
						SnakeMod.createTranslatable(
								"text",
								"won",
								winner.getPlayer().getDisplayName()
						)
				));
	}

	protected void tick() {
		if (countdown % 20 == 0 || countdown % 20 == 15) {
			snakeManager.getAliveSnakes()
					.forEach(SnakePlayer::spawnFirework);
		}

		foodManager.tick(snakeManager.getAliveSnakes());
		snakeManager.tickPlaying((killer, killed) -> {});

		super.tick();

		if (countdown <= 0) {
			gameSpace.close(GameCloseReason.FINISHED);
		}
		countdown--;
	}
}
