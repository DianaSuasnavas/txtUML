package hu.elte.txtuml.examples.pingpong.model.signals;

import hu.elte.txtuml.api.model.Interface;

public interface PingInterface extends Interface {
	void reception(PingSignal p); // must be 'reception'
}
