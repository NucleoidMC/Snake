package net.puffish.snakemod.event;

import xyz.nucleoid.stimuli.event.StimulusEvent;

public class SnakeEvents {
	public static final StimulusEvent<TickStart> TICK_START = StimulusEvent.create(TickStart.class, ctx -> () -> {
		try {
			for (var listener : ctx.getListeners()) {
				listener.onTickStart();
			}
		} catch (Throwable throwable) {
			ctx.handleException(throwable);
		}
	});

	public interface TickStart {
		void onTickStart();
	}
}
