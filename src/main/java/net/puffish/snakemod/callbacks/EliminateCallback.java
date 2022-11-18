package net.puffish.snakemod.callbacks;

import net.minecraft.server.network.ServerPlayerEntity;

public interface EliminateCallback {
	void accept(ServerPlayerEntity killer, ServerPlayerEntity killed);
}
