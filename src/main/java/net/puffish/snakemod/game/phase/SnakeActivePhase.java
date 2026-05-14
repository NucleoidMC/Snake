package net.puffish.snakemod.game.phase;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.GameType;
import net.puffish.snakemod.SnakeMod;
import net.puffish.snakemod.event.SnakeEvents;
import net.puffish.snakemod.game.FoodManager;
import net.puffish.snakemod.game.ScoreboardManager;
import net.puffish.snakemod.game.SnakeManager;
import net.puffish.snakemod.game.map.SnakeMap;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;

import java.util.Random;

public abstract class SnakeActivePhase extends SnakePhase {
	protected final SnakeManager snakeManager;
	protected final FoodManager foodManager;
	protected final ScoreboardManager scoreboardManager;

	protected final Random random = new Random();

	protected SnakeActivePhase(GameSpace gameSpace, ServerLevel level, SnakeMap map, SnakeManager snakeManager, FoodManager foodManager, ScoreboardManager scoreboardManager) {
		super(gameSpace, level, map);
		this.snakeManager = snakeManager;
		this.foodManager = foodManager;
		this.scoreboardManager = scoreboardManager;
	}

	protected void applyListeners(GameActivity activity) {
		activity.listen(GamePlayerEvents.ACCEPT, this::acceptPlayer);
		activity.listen(GamePlayerEvents.LEAVE, this::leavePlayer);
		activity.listen(GamePlayerEvents.ADD, this::addPlayer);
		activity.listen(GamePlayerEvents.REMOVE, this::removePlayer);
		activity.listen(SnakeEvents.TICK_START, this::tick);
	}

	protected void tick() {
		scoreboardManager.set((player, builder) -> {
			var optSnake = snakeManager.getSnake(player);
			builder.add(
					SnakeMod.createTranslatable("sidebar", "alive")
							.withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN),
					Component.literal(Integer.toString(snakeManager.getAliveCount()))
							.withStyle(ChatFormatting.WHITE)
			);
			builder.add(
					SnakeMod.createTranslatable("sidebar", "dead")
							.withStyle(ChatFormatting.BOLD, ChatFormatting.RED),
					Component.literal(Integer.toString(snakeManager.getDeadCount()))
							.withStyle(ChatFormatting.WHITE)
			);
			optSnake.ifPresent(snake -> {
				builder.add(
						SnakeMod.createTranslatable("sidebar", "kills")
								.withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW),
						Component.literal(Integer.toString(snake.getKills()))
								.withStyle(ChatFormatting.WHITE));
				builder.add(
						SnakeMod.createTranslatable("sidebar", "length")
								.withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA),
						Component.literal(Integer.toString(snake.getLength()))
								.withStyle(ChatFormatting.WHITE)
				);
			});
		});
	}

	private JoinAcceptorResult acceptPlayer(JoinAcceptor acceptor) {
		return acceptor.teleport(
				this.level,
				this.map.getWaitingSpawns().get(random.nextInt(map.getWaitingSpawns().size()))
		).thenRunForEach(player -> player.setGameMode(GameType.SPECTATOR));
	}

	protected void leavePlayer(ServerPlayer player) {
		snakeManager.removePlayer(player);
	}

	protected void addPlayer(ServerPlayer player) {
		scoreboardManager.addPlayer(player);
	}

	protected void removePlayer(ServerPlayer player) {
		scoreboardManager.removePlayer(player);
	}
}
