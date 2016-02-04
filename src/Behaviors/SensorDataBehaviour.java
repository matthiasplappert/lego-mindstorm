package Behaviors;

import HAL.IHAL;
import State.SharedState;
import State.State;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class SensorDataBehaviour extends StateBehavior {	
	
	public SensorDataBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean surpressed =  false;
	
	@Override
	public void action() {
		LCD.drawString("Sensor Data Test", 0, 0);

		this.hal.printOnDisplay("Sensor Data", 0, 1000);
		Sound.beepSequence();
		Delay.msDelay(1000);
		Sound.beep();
		while(!this.surpressed){
//			float value = this.hal.getMeanDistance();
			LCD.drawString("Distance value:" + this.hal.getMeanDistance(), 0, 1);
			LCD.drawString("Curr gyro:" + this.hal.getCurrentGyro(), 0, 2);
			LCD.drawString("Mean gyro:" + this.hal.getMeanGyro(), 0, 3);
			LCD.drawString("Line Type:" + this.hal.getLineType(), 0, 4);
			Delay.msDelay(10);
//			finished = true;
			
		}
		
		this.sharedState.reset(true);
		Thread.yield();
	}

	@Override
	State getTargetState() {
		return State.SensorDataState;
	}

	@Override
	public void suppress() {
		surpressed = true;
	}
}
