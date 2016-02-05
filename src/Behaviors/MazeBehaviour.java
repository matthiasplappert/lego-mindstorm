package Behaviors;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.State;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class MazeBehaviour extends StateBehavior {

	private boolean suppressed;
	private DrivebyReturnType returnType;
	private final int target_dist;

	public DrivebyReturnType getReturnType() {
		return returnType;
	}

	private static final Speed DefaultSpeed = Speed.Labyrinth;
	private static final int DELAY = 5;

	final private int holeOffset;
	final private int min_speed;
	final private int max_speed;
	final private int basic_speed;
	final static private int LeftTurnAngle = 80;
	private static final float DISTANCE_UPPER_BOUND = 30;

	// TODO: MeanFilter smaller
	public MazeBehaviour(SharedState sharedState, IHAL hal) {
		this(sharedState, hal, 10, 150, 50, 50, 30);
	}

	public MazeBehaviour(SharedState sharedState, IHAL hal, int target_dist, int basic_speed, int max_speed_diff,
			int min_speed_diff, int holeOffset) {
		super(sharedState, hal);
		this.suppressed = false;
		this.target_dist = target_dist;
		this.holeOffset = holeOffset;
		// this.initDistance = initDistance;
		this.basic_speed = basic_speed;
		this.min_speed = basic_speed - min_speed_diff;
		this.max_speed = basic_speed + max_speed_diff;
	}

	private int getSpeedInLimit(int speed) {
//		return Math.min(Math.max(this.min_speed, speed), this.max_speed);
		
		
		
		return Math.max(Math.min(speed, this.max_speed),this.min_speed);
	}

	@Override
	public void action() {
		LCD.drawString("Maze", 0, 0);
//		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.Labyrinth);
		// this.hal.getMeanDistance();
		// Sound.beep();
		Delay.msDelay(1000);
		Sound.twoBeeps();
		float initial_degree = this.hal.getMeanGyro();

		// this.startupSequence(initial_degree);
		// this.hal.setSpeed(DefaultSpeed);
		while (!this.suppressed /* && this.hal.getLineType() != LineType.LINE */) {

			// Get (filtered) distance
			if (this.hal.isTouchButtonPressed()) {
				if (this.hal.getMeanDistance() > this.holeOffset) {
					moveBackAndTurn(Direction.RIGHT);
				} else {
					moveBackAndTurn(Direction.LEFT);
				}
				this.hal.stop();
			} else {
				float distance = Math.min(this.hal.getCurrentDistance(), DISTANCE_UPPER_BOUND);
				LCD.drawString("dist to wall: " + distance, 0, 1);
				int diff = Math.round(distance - this.target_dist) * 2;
				// target_dist

				int outerChain = this.getSpeedInLimit(this.basic_speed + diff);
				int innerChain = this.getSpeedInLimit(this.basic_speed - (diff));

				LCD.drawString("Outer chain: " + outerChain + "   ", 0, 2);
				LCD.drawString("Inner chain: " + innerChain + "   ", 0, 3);
				LCD.drawString("Diff: " + diff + "   ", 0, 4);
//				this.hal.forward(outerChain, innerChain);

				Delay.msDelay(10);
				this.sharedState.reset(true);
			}
		}
		if (this.hal.getLineType() == LineType.LINE) {
			Sound.beepSequence();
		}
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.UP);
		this.hal.stop();
		this.sharedState.reset(true);
	}

	private void targetLineFound(final float initalDegree) {
		this.hal.stop();
		// compute roation degree
		float currentDegree = this.hal.getMeanGyro();
		// compute degree that have been left to achieve final position
		float diff_degree = (initalDegree + 180 - currentDegree);

}

	// private void startupSequence(final float initalDegree) {
	//
	// // get inital gyro
	//
	// this.hal.setSpeed(Speed.Fast);
	// this.hal.forward();
	// Delay.msDelay(250);
	// this.hal.setCourseFollowingAngle((int) initalDegree + 45);
	// // long ts = System.currentTimeMillis();
	//
	// // follow course until we hit the wall
	// float diff = this.hal.getCurrentDistance() - (0.5f * this.target_dist);
	// while (!this.suppressed && !(diff < this.offset)) {
	// this.hal.performCourseFollowingStep();
	// Delay.msDelay(10);
	// diff = this.hal.getCurrentDistance() - (0.5f * this.target_dist);
	// }
	// // now rotate to the left
	// moveBackAndTurn(Direction.LEFT);
	//
	// }

	private void moveBackAndTurn(Direction dir) {
		this.hal.setSpeed(Speed.Fast);
		this.hal.backward();
		Delay.msDelay(1000);
		this.hal.stop();
		this.hal.setSpeed(DefaultSpeed);

		this.hal.rotate(dir.getMultiplierForDirection() * LeftTurnAngle);
		while (this.hal.isRotating()) {
			Delay.msDelay(10);
		}
		this.hal.stop();

	}

	@Override
	public void suppress() {
		this.suppressed = true;

	}

	@Override
	State getTargetState() {
		return State.mazeState;
	}

}
