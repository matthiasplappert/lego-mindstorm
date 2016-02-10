package Behaviors;

import java.io.IOException;

import HAL.ColorMode;
import HAL.IHAL;
import State.SharedState;
import communication.Communication;
import State.MyState;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class SensorDataBehaviour extends StateBehavior {	
	
	public SensorDataBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean suppressed = false;
	
	@Override
	public void action() {
		this.suppressed = false;
		
		//LCD.drawString("Sensor Data Test", 0, 0);

		//this.hal.printOnDisplay("Sensor Data", 0, 0);
		//Sound.beepSequence();
		//Delay.msDelay(1000);
		//Sound.beep();
		this.hal.setColorMode(ColorMode.AMBIENT_LIGHT);
		//LCD.clear();
		while(!this.suppressed){
//			float value = this.hal.getMeanDistance();
			//LCD.drawString("Distance value:" + this.hal.getMeanDistance(), 0, 1);
			//LCD.drawString("Curr gyro:" + this.hal.getCurrentGyro(), 0, 2);
			///LCD.drawString("Mean gyro:" + this.hal.getMeanGyro(), 0, 3);
			//LCD.drawString("Ambient Light on:" + this.hal.isAmbientLightOn(), 0,4);
			
			/*try {
				//L/CD.drawString("Elevator Status is:", 0, 5);
				//LCD.drawString(""+Communication.getModule().requestStatus(), 0, 6);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/

			//			LCD.drawString("Line Type:" + this.hal.getLineType(), 0, 4);
			Delay.msDelay(10);
//			finished = true;
			
		}
		
		this.sharedState.reset(true);
		Thread.yield();
	}

	@Override
	MyState getTargetState() {
		return MyState.SensorDataState;
	}

	@Override
	public void suppress() {
		suppressed = true;
	}
}
