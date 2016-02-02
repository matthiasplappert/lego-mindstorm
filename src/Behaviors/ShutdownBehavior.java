package Behaviors;

import lejos.hardware.Button;
import lejos.robotics.subsumption.Behavior;

public class ShutdownBehavior implements Behavior {
	@Override
	public boolean takeControl() {
		return Button.ESCAPE.isDown();
	}

	@Override
	public void action() {
		System.exit(0);
		
	}

	@Override
	public void suppress() {
		// Nothing to do here.
	}
}
