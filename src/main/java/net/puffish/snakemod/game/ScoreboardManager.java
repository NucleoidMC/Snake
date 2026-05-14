package net.puffish.snakemod.game;

import eu.pb4.sidebars.api.Sidebar;
import eu.pb4.sidebars.api.lines.LineBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;
import net.puffish.snakemod.SnakeMod;
import xyz.nucleoid.plasmid.api.game.common.widget.SidebarWidget;

import java.util.function.BiConsumer;

public class ScoreboardManager {
	private final Object2ObjectMap<ServerPlayer, Sidebar> sidebars = new Object2ObjectOpenHashMap<>();

	private ScoreboardManager() {

	}

	public static ScoreboardManager create(){
		return new ScoreboardManager();
	}

	public void set(BiConsumer<ServerPlayer, LineBuilder> consumer){
		sidebars.forEach((player, sidebar) -> sidebar.set(builder -> consumer.accept(player, builder)));
	}

	public void addPlayer(ServerPlayer player) {
		var sidebar = new SidebarWidget(SnakeMod.createTranslatable("sidebar", "title").withStyle(ChatFormatting.BOLD));
		sidebar.addPlayer(player);
		sidebars.put(player, sidebar);
	}

	public void removePlayer(ServerPlayer player) {
		var sidebar = sidebars.remove(player);
		if(sidebar != null){
			sidebar.removePlayer(player);
		}
	}
}
