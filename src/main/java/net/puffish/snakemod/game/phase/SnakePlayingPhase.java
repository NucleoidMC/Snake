package net.puffish.snakemod.game.phase;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.puffish.snakemod.SnakeMod;
import net.puffish.snakemod.game.FoodManager;
import net.puffish.snakemod.game.ScoreboardManager;
import net.puffish.snakemod.game.SnakeManager;
import net.puffish.snakemod.game.map.SnakeMap;
import xyz.nucleoid.plasmid.api.game.GameSpace;

public class SnakePlayingPhase extends SnakeActivePhase {
	private final int minAliveCount;

	public SnakePlayingPhase(GameSpace gameSpace, ServerLevel level, SnakeMap map, SnakeManager snakeManager, FoodManager foodManager, ScoreboardManager scoreboardManager, int minAliveCount) {
		super(gameSpace, level, map, snakeManager, foodManager, scoreboardManager);
		this.minAliveCount = minAliveCount;
	}

	public static SnakePlayingPhase create(GameSpace gameSpace, SnakeActivePhase oldPhase) {
		return new SnakePlayingPhase(
				gameSpace,
				oldPhase.level,
				oldPhase.map,
				oldPhase.snakeManager,
				oldPhase.foodManager,
				oldPhase.scoreboardManager,
				oldPhase.snakeManager.getCount() == 1 ? 1 : 2
		);
	}

	public static void open(SnakeActivePhase oldPhase) {
		oldPhase.gameSpace.setActivity(activity -> {
			var phase = SnakePlayingPhase.create(activity.getGameSpace(), oldPhase);

			phase.applyRules(activity);
			phase.applyListeners(activity);
		});
	}

	protected void tick() {
		foodManager.tick(snakeManager.getAliveSnakes());
		snakeManager.tickPlaying(this::eliminate);

		super.tick();

		if (snakeManager.getAliveCount() < minAliveCount) {
			SnakeEndingPhase.open(this);
		}
	}

	private void eliminate(ServerPlayer killer, ServerPlayer player) {
		if (killer == player) {
			gameSpace.getPlayers().sendMessage(
					SnakeMod.createTranslatable(
							"text",
							"eliminated",
							Component.empty().withStyle(ChatFormatting.WHITE).append(player.getDisplayName())
					).withStyle(ChatFormatting.DARK_AQUA)
			);
		} else {
			gameSpace.getPlayers().sendMessage(
					SnakeMod.createTranslatable(
							"text",
							"eliminated.by",
							Component.empty().withStyle(ChatFormatting.WHITE).append(player.getDisplayName()),
							Component.empty().withStyle(ChatFormatting.WHITE).append(killer.getDisplayName())
					).withStyle(ChatFormatting.DARK_AQUA)
			);
		}
	}
}
