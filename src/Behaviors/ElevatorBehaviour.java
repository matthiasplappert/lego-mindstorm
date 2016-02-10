package Behaviors;

import java.io.IOException;


import HAL.ColorMode;
import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import communication.ComModule;
import communication.Communication;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;
import State.MyState;

public class ElevatorBehaviour extends StateBehavior {

	private static final int ELEVATOR_MOVING_DURATION = 6;
	private static final int ANGLE = -33;
	private static final int MAX_MOVE_ON_PLATOON_DISTANCE = 80;
	private static final int BACK_DISTANCE_ON_PLATOON = -11;
	private static final float MIN_DIST = 1.5f;
	private static final float DISTANCE_TOLERANCE = 0.5f;
	private static final int TURN_ANGLE = 5;
	private static final Speed forwardSpeed = Speed.Fast;
	private static final float MAX_RANGE_PLATOON = 30;
	private static final int Gyro_Tolerance = 3;
	private static final int correction_back_distance = -5;
	private static final int correction_angle = 3;
	private ComModule comm;

	public ElevatorBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
		this.comm = Communication.getModule();
	}

	private boolean suppressed = false;
	private boolean finished = false;

	@Override
	public void action() {
		this.suppressed = false;
		try {
			//LCD.clear();
			this.hal.printOnDisplay("ElevatorBehaviour started", 0, 0);
			this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.SAFE);
			while (!this.suppressed && !this.finished) {// HERE IS outer loop!
				boolean status = false;


				this.hal.resetLeftTachoCount();
				this.hal.resetGyro();
				this.hal.setSpeed(Speed.Fast);
				this.hal.setCourseFollowingAngle(ANGLE);
				while (this.hal.getLeftTachoDistance() < MAX_MOVE_ON_PLATOON_DISTANCE
						&& !this.hal.isTouchButtonPressed() && !this.suppressed) {// move
																					// forward
																					// to
																					// stand
																					// on
																					// lightened
																					// platoon
					this.hal.performCourseFollowingStep();

					Delay.msDelay(10);
				}
				this.hal.stop();
				// move back
				go_back(BACK_DISTANCE_ON_PLATOON);
				this.hal.rotate(Math.abs(ANGLE));
				while (this.hal.isRotating() && !this.suppressed) {
					Delay.msDelay(10);
				}
				this.hal.stop();

				while (status == false && !this.suppressed) {// wait for
																// status=true

					status = this.comm.requestStatus();
					//LCD.drawString("status is false       ", 1, 0);
					Delay.msDelay(100);

				}
				this.hal.setColorMode(ColorMode.AMBIENT_LIGHT);
				//LCD.drawString("Request Elevator       ", 1, 0);

				if (this.comm.requestElevator()) {// reserve elevator

					// wait for safe signal
					this.wait_for_ambient_light_on();
					// enter elevator
					//LCD.drawString("Ambient Light off      ", 1, 0);
					this.followWallUntilElevatorEnd();
					this.hal.setColorMode(ColorMode.RED);
					// request elevator to move
					//LCD.drawString("Request Elevator       ", 1, 0);
					//LCD.drawString("to go down       ", 2, 0);
					//Sound.beep();
					if (this.comm.moveElevatorDown()) {
						Delay.msDelay(ELEVATOR_MOVING_DURATION * 1000);
						//LCD.drawString("Try to leave Elevator      ", 1, 0);
						//LCD.drawString("                           ", 2, 0);

						this.move_until_line();
						finished = true;
						this.sharedState.reset(true);

					} else {
						// TODO: Move back to safe position and go to outer loop
						throw new IllegalStateException("Error in elevator down!");
						// break;//goto outer loop
					}
				} else {
					break;// go to outer loop
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.sharedState.reset(true);

		// Thread.yield();

	}

	private void move_until_line() {
		this.hal.setSpeed(forwardSpeed);
		this.hal.forward();
		while (this.hal.getLineType() != LineType.LINE && !this.suppressed) {
			//LCD.drawString("No Line Found", 1, 0);
			Delay.msDelay(10);
		}
		this.hal.stop();

	}

	private void followWallUntilElevatorEnd() {
		
		//Sound.beep();
//		this.hal.setSpeed(Speed.Slow);
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.SAFE);
//		while(!this.suppressed && this.hal.getMeanDistance()> MAX_RANGE_PLATOON){
//			this.hal.forward();
//			Delay.msDelay(10);
//		}
		this.hal.resetGyro();
		this.hal.setSpeed(Speed.Medium);
		while (!this.suppressed && !this.hal.isTouchButtonPressed()) {
			float current_gyro = this.hal.getCurrentGyro();
			if(current_gyro < -Gyro_Tolerance){//too far to left
				this.hal.backward();
				Delay.msDelay(10);
				this.hal.rotate(correction_angle);
			}
			else if(current_gyro > Gyro_Tolerance){//too far right
				this.hal.backward();
				Delay.msDelay(10);
				this.hal.rotate(-correction_angle);
			}
			else{
				this.hal.forward();
			}
			Delay.msDelay(10);

		}

		this.hal.stop();
		go_back(-2);
		this.hal.rotateTo(0, true);
		while(!this.suppressed && this.hal.isRotating()){
			Delay.msDelay(10);
		}
		this.hal.stop();
		this.hal.setSpeed(forwardSpeed);
	}

	private void go_back(int distance) {
		//Sound.beep();
		int neg_dist = -1 * Math.abs(distance);
		this.hal.stop();
		this.hal.setSpeed(Speed.Medium);
		this.hal.resetLeftTachoCount();
		this.hal.backward();

		while (this.hal.getLeftTachoDistance() > neg_dist && !this.suppressed) {
			Delay.msDelay(10);
		}
		this.hal.stop();
		this.hal.setSpeed(forwardSpeed);
	}

	private void wait_for_ambient_light_on() {
		while (!this.hal.isAmbientLightOn() && !this.suppressed) {
			//LCD.drawString("Ambient Light is off       ", 1, 0);
			Delay.msDelay(10);
		}

	}

	@Override
	MyState getTargetState() {
		return MyState.ElevatorState;
	}

	@Override
	public void suppress() {
		suppressed = true;
	}
}
