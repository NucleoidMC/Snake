package net.puffish.snakemod.game.phase;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.puffish.snakemod.config.SnakeConfig;
import net.puffish.snakemod.game.map.SnakeMap;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;

import java.util.Random;

public class SnakeWaitingPhase extends SnakePhase {
	private final Random random = new Random();

	protected SnakeWaitingPhase(GameSpace gameSpace, ServerWorld world, SnakeMap map) {
		super(gameSpace, world, map);
	}

	private static Either<GameOpenProcedure, Exception> tryOpen(GameOpenContext<SnakeConfig> context) {
		var config = context.config();

		return SnakeMap.create(context.server(), config.map()).flatMap(map -> {
			if (config.players().minPlayers() <= 0 || config.players().maxPlayers() < config.players().minPlayers()) {
				return Either.right(new IllegalStateException("Invalid game config!"));
			}
			if (config.players().maxPlayers() > map.getSpawns().size()) {
				return Either.right(new IllegalStateException("Not enough spawns!"));
			}

			var worldConfig = new RuntimeWorldConfig()
					.setGenerator(map.createGenerator(context.server()))
					.setTimeOfDay(config.map().time());

			return Either.left(context.openWithWorld(worldConfig, (activity, world) -> {
				GameWaitingLobby.addTo(activity, config.players());

				var phase = new SnakeWaitingPhase(activity.getGameSpace(), world, map);

				phase.applyRules(activity);
				phase.applyListeners(activity);
			}));
		});
	}

	public static GameOpenProcedure open(GameOpenContext<SnakeConfig> context) {
		return tryOpen(context)
				.mapRight(e -> new GameOpenException(Text.literal(e.getMessage()), e))
				.orThrow();
	}

	@Override
	protected void applyListeners(GameActivity activity) {
		super.applyListeners(activity);

		activity.listen(GameActivityEvents.REQUEST_START, this::requestStart);
		activity.listen(GamePlayerEvents.OFFER, this::playerOffer);
		activity.listen(GameActivityEvents.TICK, this::tick);
	}

	private GameResult requestStart() {
		SnakeStartingPhase.open(this);
		return GameResult.ok();
	}

	private PlayerOfferResult playerOffer(PlayerOffer offer) {
		var player = offer.player();

		return offer.accept(
				this.world,
				getRandomWaitingSpawn()
		).and(() -> player.changeGameMode(GameMode.ADVENTURE));
	}

	private void tick() {
		gameSpace.getPlayers().forEach(player -> {
			if(!map.getBounds().contains(BlockPos.ofFloored(player.getPos()))){
				Vec3d pos = getRandomWaitingSpawn();
				player.teleport(pos.x, pos.y, pos.z);
			}
		});
	}

	private Vec3d getRandomWaitingSpawn(){
		return map.getWaitingSpawns().get(random.nextInt(map.getWaitingSpawns().size()));
	}
}
