package net.puffish.snakemod.game.phase;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.puffish.snakemod.SnakeMod;
import net.puffish.snakemod.game.FoodManager;
import net.puffish.snakemod.game.ScoreboardManager;
import net.puffish.snakemod.game.SnakeManager;
import net.puffish.snakemod.game.map.SnakeMap;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;

public class SnakePlayingPhase extends SnakeActivePhase {
	protected SnakePlayingPhase(GameSpace gameSpace, ServerWorld world, SnakeMap map, SnakeManager snakeManager, FoodManager foodManager, ScoreboardManager scoreboardManager) {
		super(gameSpace, world, map, snakeManager, foodManager, scoreboardManager);
	}

	public static SnakePlayingPhase create(GameSpace gameSpace, SnakeActivePhase oldPhase) {
		return new SnakePlayingPhase(
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
			var phase = SnakePlayingPhase.create(activity.getGameSpace(), oldPhase);

			phase.applyRules(activity);
			phase.applyListeners(activity);
		});
	}

	@Override
	public void applyListeners(GameActivity activity) {
		super.applyListeners(activity);

		activity.listen(GameActivityEvents.TICK, this::tick);
	}

	protected void tick() {
		foodManager.tick(snakeManager.getSnakes());
		snakeManager.tickPlaying(gameSpace.getPlayers(), this::eliminate);

		super.tick();

		if (snakeManager.getAliveCount() <= (snakeManager.getSnakes().size() == 1 ? 0 : 1)) {
			SnakeEndingPhase.open(this);
		}
	}

	private void eliminate(ServerPlayerEntity killer, ServerPlayerEntity player) {
		if (killer == player) {
			gameSpace.getPlayers().sendMessage(
					SnakeMod.createTranslatable("text", "eliminated", player.getDisplayName())
			);
		} else {
			gameSpace.getPlayers().sendMessage(
					SnakeMod.createTranslatable("text", "eliminated.by", player.getDisplayName(), killer.getDisplayName())
			);
		}
	}
}
