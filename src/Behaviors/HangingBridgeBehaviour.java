package Behaviors;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.State;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;
import HAL.Speed;

public class HangingBridgeBehaviour extends StateBehavior {	
	
	public HangingBridgeBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean surpressed =  false;
	private boolean finished = false;
	
	@Override
	public void action() {
		this.hal.printOnDisplay("HangingBridgeBehaviour started", 0, 1000);
		float followAngle = this.hal.getMeanGyro();
		float currentAngle = followAngle;	
		
		this.hal.printOnDisplay("followAngle = " + followAngle, 1, 1000);
		
		while(!this.surpressed && !this.finished){
			LCD.clear(2);
			LCD.clear(3);
			LCD.clear(4);
			this.hal.forward(Speed.Slow);
			
			currentAngle = this.hal.getCurrentGyro();			
			this.hal.printOnDisplay("currentAngle = " + currentAngle, 2, 10);
			
			if(Math.abs(currentAngle - followAngle) >= 1){
				this.hal.printOnDisplay("turning = " + (currentAngle - followAngle), 3, 10);
				this.hal.turn((int)(currentAngle - followAngle), false, true);
				
				while(this.hal.isRotating() && !this.surpressed){
					this.hal.printOnDisplay("current Gyro = " + this.hal.getCurrentGyro(), 4, 10);
				}									
			}else{
				this.hal.printOnDisplay("Straight ahead", 3, 10);
				Delay.msDelay(50);
			}
		}
					
		this.sharedState.reset(true);
		Thread.yield();
	}

	@Override
	State getTargetState() {
		return State.HangingBridgeState;
	}

	@Override
	public void suppress() {
		surpressed = true;
	}
}
