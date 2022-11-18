package net.puffish.snakemod.game.phase;

import net.minecraft.server.world.ServerWorld;
import net.puffish.snakemod.game.map.SnakeMap;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;

public abstract class SnakePhase {
	protected final GameSpace gameSpace;
	protected final ServerWorld world;
	protected final SnakeMap map;

	protected SnakePhase(GameSpace gameSpace, ServerWorld world, SnakeMap map) {
		this.gameSpace = gameSpace;
		this.world = world;
		this.map = map;
	}

	protected void applyRules(GameActivity activity) {
		activity.deny(GameRuleType.CRAFTING);
		activity.deny(GameRuleType.HUNGER);
		activity.deny(GameRuleType.PVP);
		activity.deny(GameRuleType.FALL_DAMAGE);
		activity.deny(GameRuleType.FIRE_TICK);
		activity.deny(GameRuleType.ICE_MELT);
		activity.deny(GameRuleType.BREAK_BLOCKS);
		activity.deny(GameRuleType.PLACE_BLOCKS);
		activity.deny(GameRuleType.MODIFY_ARMOR);
		activity.deny(GameRuleType.MODIFY_INVENTORY);
		activity.deny(GameRuleType.SWAP_OFFHAND);
		activity.deny(GameRuleType.THROW_ITEMS);
		activity.deny(GameRuleType.DISMOUNT_VEHICLE);
		activity.deny(GameRuleType.INTERACTION);
		activity.deny(GameRuleType.USE_BLOCKS);
		activity.deny(GameRuleType.USE_ENTITIES);
		activity.deny(GameRuleType.USE_ITEMS);
	}

	protected void applyListeners(GameActivity activity) {

	}
}
