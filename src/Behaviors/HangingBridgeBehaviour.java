package Behaviors;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.MyState;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;
import HAL.Speed;

public class HangingBridgeBehaviour extends StateBehavior {	
	
	public HangingBridgeBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean suppressed = false;
	

	
	
	
	@Override
	public void action() {
		this.suppressed = false;
		
		float difference;
		this.hal.printOnDisplay("HangingBridgeBehaviour started", 0, 0);

		// Follow line
		// After loosing the line
		float distance = this.hal.getMeanDistance();
		if (distance > 10) {
			// search line
		}
		this.hal.setSpeed(Speed.Slow);
		difference = 200;
		while (Math.abs(difference) > 0.2f && this.hal.getCurrentDistance() < 20.f && !suppressed) {
			this.hal.forward();
			Delay.msDelay(400);
			difference = this.hal.getMeanDistance() - distance;
			if (Math.abs(difference) > 0.3f) {
				this.hal.turn((int) Math.signum(difference));
				Delay.msDelay(200);
			}
		}
		this.hal.stop();
		this.hal.setSpeed(Speed.Medium);
		Sound.buzz();
		this.hal.resetGyro();
		Delay.msDelay(100);
		this.hal.setCourseFollowingAngle((int)this.hal.getMeanGyro());
		while (!this.suppressed)
		{
			//LCD.clear();
			//this.hal.printOnDisplay(String.valueOf(this.hal.getCurrentGyro()), 2, 100);
			this.hal.performCourseFollowingStep();
			Delay.msDelay(10);
		}
		this.sharedState.reset(true);
		Thread.yield();

	}

	@Override
	MyState getTargetState() {
		return MyState.HangingBridgeState;
	}

	@Override
	public void suppress() {
		suppressed = true;
	}
}
