package pingpong.j.model.signals;

import hu.elte.txtuml.api.model.Interface;

public interface PongInterface extends Interface {
	void reception(PongSignal p);
}
