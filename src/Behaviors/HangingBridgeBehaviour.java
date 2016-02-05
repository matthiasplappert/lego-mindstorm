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

	private boolean suppressed = false;
	private float target_dist;
	private float offset = 0f;
	private static final int DELAY = 5;

	private int maxTurnAngle = 1;

	@Override
	public void action() {
		// this.hal.testMotor();
		float difference;
		this.hal.printOnDisplay("HangingBridgeBehaviour started", 0, 1000);
		//
		// // Follow line
		// // After loosing the line
		float distance = this.hal.getMeanDistance();
		// // if (distance > 10) {
		// // search line
		// // }
		// this.hal.setSpeed(Speed.Slow);
		this.hal.setSpeed(Speed.Medium);
		this.hal.forward();
		Delay.msDelay(1000);
		// difference = 200;
		while (this.hal.getCurrentDistance() < 17.f && !suppressed) {
			this.hal.forward();
			Delay.msDelay(500);
			difference = this.hal.getMeanDistance() - distance;
			if (Math.abs(difference) > 0.3f) {
				this.hal.turn((int) Math.signum(difference));
				Delay.msDelay(100);
			}
		}
		this.hal.stop();
		//
		// this.target_dist = this.hal.getMeanDistance();
		//
		// for (int i = 0; i < 250; i++) {
		// if (this.suppressed) {
		// break;
		// }
		// if (isTooClose(this.hal.getMeanDistance())) {
		// // Sound.beep();
		// this.hal.turn(-this.maxTurnAngle);
		//
		// while (!this.suppressed && this.hal.isRotating()) {
		// if (!this.isTooClose(this.hal.getMeanDistance())) {
		// break;
		// }
		// // Delay.msDelay(DELAY);
		// }
		//
		// } else if (isTooFar(this.hal.getMeanDistance())) {
		// // Sound.buzz();
		// this.hal.turn(this.maxTurnAngle);
		//
		// while (!this.suppressed && this.hal.isRotating()) {
		// if (!this.isTooFar(this.hal.getMeanDistance())) {
		// break;
		// }
		// // Delay.msDelay(DELAY);
		// }
		// }
		// this.hal.forward();
		//
		// Delay.msDelay(DELAY);
		// this.hal.printOnDisplay("i = " + i, 2, 0);
		// }
		// this.hal.stop();
		// this.hal.setSpeed(Speed.Medium);
		 Sound.buzz();
		 this.hal.resetGyro();
		 Delay.msDelay(100);
		 this.hal.setCourseFollowingAngle((int) this.hal.getMeanGyro());
		 while (!this.suppressed) // && no end_barcode found
		 {
		 // LCD.clear();
		 // this.hal.printOnDisplay(String.valueOf(this.hal.getCurrentGyro()),
		 // 2, 100);
		 this.hal.performCourseFollowingStep();
		 Delay.msDelay(10);
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
		suppressed = true;
	}

	private boolean isTooFar(float distance) {
		return distance > this.target_dist + this.offset;
	}

	private boolean isTooClose(float distance) {
		return distance < this.target_dist;
	}
}
