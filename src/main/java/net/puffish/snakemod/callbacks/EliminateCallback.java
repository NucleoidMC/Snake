package net.puffish.snakemod.callbacks;

import net.minecraft.server.level.ServerPlayer;

public interface EliminateCallback {
	void accept(ServerPlayer killer, ServerPlayer killed);
}
