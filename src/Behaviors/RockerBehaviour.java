package Behaviors;

import HAL.IHAL;
import State.SharedState;
import State.State;
import lejos.hardware.lcd.LCD;

public class RockerBehaviour extends StateBehavior {	
	
	public RockerBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean surpressed =  false;
	private boolean finished = false;
	
	@Override
	public void action() {
		LCD.drawString("Sample test", 0, 0);
		this.hal.printOnDisplay("HangingBridgeBehaviour started", 0, 1000);
		while(!this.surpressed){
			float value = this.hal.getMeanDistance();
			LCD.drawString("Distance value:" + value, 0, 1);
			
//			finished = true;
			
		}
		
		this.sharedState.reset(true);
		Thread.yield();
	}

	@Override
	State getTargetState() {
		return State.RockerState;
	}

	@Override
	public void suppress() {
		surpressed = true;
	}
}
