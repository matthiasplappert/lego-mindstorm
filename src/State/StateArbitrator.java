package State;

import lejos.hardware.Button;
import lejos.robotics.subsumption.Behavior;

public class StateArbitrator {

	private Behavior[] _behavior;

	public boolean keepRunning = true;

	private Monitor monitor;
	private SharedState _sharedState;
	private Behavior _activeBehavior;

	public StateArbitrator(Behavior[] behaviorList, SharedState sharedState) {
		_behavior = behaviorList;
		_sharedState = sharedState;

		monitor = new Monitor();
		monitor.setDaemon(true);
	}

	public void start() {
		monitor.start();
		while (true) {
			boolean found = false;
			for (Behavior b : _behavior) {
				if (b.takeControl()) {
					found = true;
					_activeBehavior = b;
					b.action();
					break;
				}
			}
			if (!found) {
				return;
			}
			Thread.yield();
		}
	}

	public void stop() {
		keepRunning = false;
	}

	private class Monitor extends Thread {
		public void run() {
			while (keepRunning) {
				Behavior b = _activeBehavior;
				if (b != null && Button.ESCAPE.isDown()) {
					b.suppress();
					_sharedState.setState(MyState.ShutDownState);
				}
				Thread.yield();
			}
		}
	}
}
