package net.puffish.snakemod.game.phase;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import net.puffish.snakemod.config.SnakeConfig;
import net.puffish.snakemod.game.map.SnakeMap;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
import xyz.nucleoid.plasmid.api.game.*;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.player.JoinOfferResult;

import java.util.Random;

public class SnakeWaitingPhase extends SnakePhase {
	private final Random random = new Random();

	protected SnakeWaitingPhase(GameSpace gameSpace, ServerLevel level, SnakeMap map) {
		super(gameSpace, level, map);
	}

	private static Either<GameOpenProcedure, Exception> tryOpen(GameOpenContext<SnakeConfig> context) {
		var config = context.config();

		return SnakeMap.create(context.server(), config.map()).flatMap(map -> {
			if (config.players().minPlayers() <= 0) {
				return Either.right(new IllegalStateException("Invalid game config!"));
			}

			var levelConfig = new RuntimeLevelConfig()
					.setGenerator(map.createGenerator(context.server()));
					//.setTimeOfDay(config.map().time());

			return Either.left(context.openWithLevel(levelConfig, (activity, level) -> {
				GameWaitingLobby.addTo(activity, config.players());

				var phase = new SnakeWaitingPhase(activity.getGameSpace(), level, map);

				phase.applyRules(activity);
				phase.applyListeners(activity);
			}));
		});
	}

	public static GameOpenProcedure open(GameOpenContext<SnakeConfig> context) {
		return tryOpen(context)
				.mapRight(e -> new GameOpenException(Component.literal(e.getMessage()), e))
				.orThrow();
	}

	@Override
	protected void applyListeners(GameActivity activity) {
		super.applyListeners(activity);

		activity.listen(GameActivityEvents.REQUEST_START, this::requestStart);
		activity.listen(GamePlayerEvents.OFFER, this::offerPlayer);
		activity.listen(GamePlayerEvents.ACCEPT, this::acceptPlayer);
		activity.listen(GameActivityEvents.TICK, this::tick);
	}

	private GameResult requestStart() {
		SnakeStartingPhase.open(this);
		return GameResult.ok();
	}

	private JoinOfferResult offerPlayer(JoinOffer offer) {
		if (this.gameSpace.getPlayers().size() + offer.players().size() > map.getSpawns().size()) {
			return offer.reject(GameComponents.Join.gameFull());
		}
		return offer.accept();
	}

	private JoinAcceptorResult acceptPlayer(JoinAcceptor acceptor) {
		return acceptor.teleport(
				this.level,
				getRandomWaitingSpawn()
		).thenRunForEach(player -> player.setGameMode(GameType.ADVENTURE));
	}

	private void tick() {
		gameSpace.getPlayers().forEach(player -> {
			if(!map.getBounds().contains(BlockPos.containing(player.position()))){
				Vec3 pos = getRandomWaitingSpawn();
				player.randomTeleport(pos.x, pos.y, pos.z, false);
			}
		});
	}

	private Vec3 getRandomWaitingSpawn(){
		return map.getWaitingSpawns().get(random.nextInt(map.getWaitingSpawns().size()));
	}
}
