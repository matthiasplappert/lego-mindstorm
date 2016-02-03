package Behaviors;

import HAL.IHAL;
import State.SharedState;
import State.State;
import lejos.hardware.lcd.LCD;

public class BridgeBehaviour extends StateBehavior {	
	
	public BridgeBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean surpressed =  false;
	
	@Override
	public void action() {
		LCD.drawString("BridgeBehavior", 0, 0);
		while (!this.surpressed) {
			float distance = this.hal.getDistance();
			LCD.drawString(Float.toString(distance), 0, 1);
		}
		this.sharedState.reset(true);
	}

	@Override
	State getTargetState() {
		return State.BridgeState;
	}

	@Override
	public void suppress() {
		surpressed = true;
	}
}
