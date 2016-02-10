package HAL;

import java.util.Objects;

import Behaviors.LineType;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

public class HAL implements IHAL {
	private RegulatedMotor motorLeft;
	private RegulatedMotor motorRight;
	private RegulatedMotor motorUltrasonic;
	private EV3GyroSensor gyro;
	private EV3UltrasonicSensor ultrasonic;
	private EV3ColorSensor colorsensor;
	private EV3TouchSensor touchSensor;
	private float[] sample = new float[1];
	float lastGyroAngleBeforeRotation = 0.f;
	float rotateToAngle = 0.f;
	public static final float THRESHOLD_BLACK = 0.09f;
	public static final float THRESHOLD_BORDER = 0.20f;
	private static final float MEAN_AMBIENT_TRESHOLD = 0.15f;

	private int forwardSpeed;
	private int backwardSpeed;
	private int rotateSpeed;
	private int turnSpeedInner;
	private int turnSpeedOuter;

	private SensorSampler sensorSampler;
	private float courseFollowingAngle = 0;

	public HAL() {
		this.motorLeft = new EV3LargeRegulatedMotor(MotorPort.A);
		this.motorRight = new EV3LargeRegulatedMotor(MotorPort.B);
		this.motorLeft.synchronizeWith(new RegulatedMotor[] { this.motorRight });

		this.motorUltrasonic = new EV3MediumRegulatedMotor(MotorPort.C);
		this.gyro = new EV3GyroSensor(SensorPort.S4);
		this.ultrasonic = new EV3UltrasonicSensor(SensorPort.S3);
		this.colorsensor = new EV3ColorSensor(SensorPort.S1);
		this.touchSensor = new EV3TouchSensor(SensorPort.S2);
		this.motorUltrasonic.setSpeed(150);
		// SensorMeanFilter spans a new thread for continoous measurements
		sensorSampler = new SensorSampler(gyro, ultrasonic, colorsensor);
		sensorSampler.start();

		this.setSpeed(Speed.Fast);
	}

	public void destroy(){
		this.motorLeft.close();
		this.motorRight.close();
		this.motorUltrasonic.close();
		
		sensorSampler.suppress();
		this.gyro.close();
		this.ultrasonic.close();
		this.colorsensor.close();
		this.touchSensor.close();
	}
	@Override
	public void printOnDisplay(String text, int row, final long waitDuration) {
		LCD.clear(row);
		if (text.isEmpty() || text == null)
			throw new IllegalArgumentException();
		Objects.requireNonNull(waitDuration);
		LCD.drawString(text, 0, row);
		if (waitDuration > 0)
			Delay.msDelay(waitDuration);
	}

	@Override
	public void forward() {
		this.motorLeft.setSpeed(forwardSpeed);
		this.motorRight.setSpeed(forwardSpeed);

		this.motorLeft.startSynchronization();
		this.motorLeft.forward();
		this.motorRight.forward();
		this.motorLeft.endSynchronization();
	}

	@Override
	public void backward() {
		this.motorLeft.setSpeed(backwardSpeed);
		this.motorRight.setSpeed(backwardSpeed);

		this.motorLeft.startSynchronization();
		this.motorLeft.backward();
		this.motorRight.backward();
		this.motorLeft.endSynchronization();
	}

	@Override
	public void stop() {
		this.motorLeft.startSynchronization();
		this.motorLeft.stop();
		this.motorRight.stop();
		this.motorLeft.endSynchronization();
	}

	@Override
	/*
	 * angle >= 0: right angle < 0: left
	 */
	public void rotate(int angle) {
		rotateToAngle = angle;
		lastGyroAngleBeforeRotation = this.getCurrentGyro();
		int sign = (int) Math.signum(angle);

		this.motorLeft.setSpeed(rotateSpeed);
		this.motorRight.setSpeed(rotateSpeed);

		this.motorLeft.startSynchronization();
		if (sign >= 0) {
			this.motorLeft.forward();
			this.motorRight.backward();
		} else {
			this.motorLeft.backward();
			this.motorRight.forward();
		}
		this.motorLeft.endSynchronization();
	}

	@Override
	public void rotateTo(int angle, boolean rotateFastestWay) {
		int rotationDifference = (int) (angle - this.getCurrentGyro());

		if (rotateFastestWay) {
			rotationDifference %= 360;

			// dont turn left around if right around is faster
			if (rotationDifference < -180) {
				rotationDifference += 360;
			}

			// dont turn right around if left around is faster
			if (rotationDifference > 180) {
				rotationDifference -= 360;
			}
		}

		//this.printOnDisplay("RotateTo: " + rotationDifference, 7, 0);
		rotate(rotationDifference);
	}

	@Override
	public boolean motorsAreMoving() {
		return this.motorLeft.isMoving() || this.motorRight.isMoving();
	}

	@Override
	public boolean isTouchButtonPressed() {
		boolean result;

		touchSensor.getTouchMode().fetchSample(sample, 0); // ger current sample
		if (sample[0] == 0) { // not pressed
			result = false;
		} else { // pressed
			result = true;
		}
		return result;
	}

	/*
	 * Returns the Distance measured by the ultrasonic sensor returns the
	 * distance in cm
	 */
	@Override

	public float getMeanDistance() {
		return sensorSampler.getMeanUltrasonic() * 100.f;
	}

	@Override
	public float getCurrentGyro() {
		return sensorSampler.getCurrentGyro();
	}

	/*
	 * Rotates the Distance Sensor to a given position 0: right 1: right-down 2:
	 * down 3: left-down 4: left
	 */
	@Override
	public void moveDistanceSensorToPosition(DistanceSensorPosition position) {
		int angle;
		switch (position) {
		case Labyrinth:
			angle = -10;
			break;
		case DOWN:
			angle = -90;
			break;
		case SAFE:
			angle = 35;
			break;
		// alternative: angle=90 if deployed on the left-hand side of the robot
		case UP:
		default:
			angle = 0;
			break;
		}
		this.moveDistanceSensorToPosition(angle);
	}

	@Override
	public void moveDistanceSensorToPosition(int angle) {
		this.motorUltrasonic.rotateTo(angle, false);
	}

	// Resets the gyroscope to zero
	@Override
	public void resetGyro() {
		this.sensorSampler.resetGyro();
	}

	// Returns the current angle(degrees) measured by the gyroscope
	@Override
	public float getMeanGyro() {
		return sensorSampler.getMeanGyro();
	}

	@Override
	public EV3ColorSensor getColorSensor() {
		return colorsensor;
	}

	@Override
	public EV3UltrasonicSensor getUltrasonicSensor() {
		return this.ultrasonic;
	}

	public boolean isRotating() {
		final float offset = 3;  // stop rotation earlier b/c the motors take some time to shut down
		return Math.abs(this.getCurrentGyro() - lastGyroAngleBeforeRotation) < Math.abs(rotateToAngle) - offset;
	}

	@Override
	public float getMeanColor() {
		return this.sensorSampler.getMeanColorValue();
	}

	@Override
	public void setColorMode(ColorMode cm) {
		switch (cm) {

		case RED:
			this.sensorSampler.enableRedMode();
			break;
		case AMBIENT_LIGHT:
			this.sensorSampler.enableAmbientMode();
			break;
		default:
			break;
		}
	}

	@Override
	public ColorMode getColorMode() {
		return this.sensorSampler.getColorMode();
	}

	@Override
	public boolean isRedColorMode() {
		return this.getColorMode().equals(ColorMode.RED);
	}

	@Override
	public LineType getLineType() {
		if (this.getColorMode().equals(ColorMode.RED)) {
			final float data = this.getMeanColor();
			if (data <= THRESHOLD_BLACK)
				return LineType.BLACK;
			else if (data > THRESHOLD_BLACK && data <= THRESHOLD_BORDER)
				return LineType.BORDER;
			else if (data > THRESHOLD_BORDER && data <= 1.0f)
				return LineType.LINE;
			else
				return LineType.UNDEFINED;
		} else
			return LineType.UNDEFINED;
	}
	
	public void turnTo(int angle, boolean turnFastestWay) {
		int turnDifference = (int) (angle - this.getCurrentGyro());

		// only turn if necessary
		if (Math.abs(turnDifference) > 1) {

			if (turnFastestWay) {
				turnDifference %= 360;

				// dont turn left around if right around is faster
				if (turnDifference < -180) {
					turnDifference += 360;
				}

				// dont turn right around if left around is faster
				if (turnDifference > 180) {
					turnDifference -= 360;
				}
			}
			turn(turnDifference);
		}
	}

	@Override
	/*
	 * angle >= 0: right angle < 0: left
	 * 
	 * stopInnerChain = true: inner chain nearly stops and outer rotates
	 * stopInnerChain = false: inner chain slows down and outer speeds up
	 */
	public void turn(int angle) {
		this.turn(angle, false);
	}

	@Override
	public void turn(int angle, boolean reverse) {
		rotateToAngle = angle;
		lastGyroAngleBeforeRotation = this.getCurrentGyro();
		int sign = (int) Math.signum(angle);

		if (sign >= 0) {
			this.motorLeft.setSpeed(turnSpeedOuter);
			this.motorRight.setSpeed(turnSpeedInner);
		} else {
			this.motorLeft.setSpeed(turnSpeedInner);
			this.motorRight.setSpeed(turnSpeedOuter);
		}

		this.motorLeft.startSynchronization();
		if (reverse) {
			this.motorLeft.backward();
			this.motorRight.backward();
		} else {
			this.motorLeft.forward();
			this.motorRight.forward();
		}
		this.motorLeft.endSynchronization();
	}

	@Override
	public void setSpeed(Speed speed) {
		switch (speed) {
		case VerySlow:
			forwardSpeed = 50;
			backwardSpeed = 50;
			rotateSpeed = 50;
			turnSpeedInner = 10;
			turnSpeedOuter = 60;
			break;
		case Slow:
			forwardSpeed = 100;
			backwardSpeed = 100;
			rotateSpeed = 100;
			turnSpeedInner = 60;
			turnSpeedOuter = 120;
			break;
		case Medium:
			forwardSpeed = 150;
			backwardSpeed = 150;
			rotateSpeed = 150;
			turnSpeedInner = 120;
			turnSpeedOuter = 180;
			break;
		case VeryFast:
			forwardSpeed = 450;
			backwardSpeed = 450;
			rotateSpeed = 350;
			turnSpeedInner = 340;
			turnSpeedOuter = 400;
			break;
			
		case FollowLine:
			forwardSpeed = 500;
			backwardSpeed = 500;
			rotateSpeed = 250;
			turnSpeedInner = 180;
			turnSpeedOuter = 250;
			break;
			
		case Labyrinth:
			forwardSpeed = 400;
			backwardSpeed = 400;
			rotateSpeed = 350;
			turnSpeedInner = 160;
			turnSpeedOuter = 350;
			break;
		
		case HangingBridge:
			forwardSpeed = 150;					
			backwardSpeed = 350;
			rotateSpeed = 350;
			turnSpeedInner = 100;
			turnSpeedOuter = 180;
			break;
			
		case BridgeLeft:
			forwardSpeed = 500;
			backwardSpeed = 500;
			rotateSpeed = 350;
			turnSpeedInner = 220;
			turnSpeedOuter = 400;
			break;
			
		case BridgeRight:
			forwardSpeed = 500;
			backwardSpeed = 500;
			rotateSpeed = 350;
			turnSpeedInner = 280;
			turnSpeedOuter = 400;
			break;
			
		case Fast:
		default:
			forwardSpeed = 300;
			backwardSpeed = 200;
			rotateSpeed = 200;
			turnSpeedInner = 180;
			turnSpeedOuter = 250;
			break;
		}
	}

	@Override
	public void setCourseFollowingAngle(int followAngle) {
		this.courseFollowingAngle = followAngle;
	}

	@Override
	public void performCourseFollowingStep() {
		this.performCourseFollowingStep(false);
	}

	@Override
	public void performCourseFollowingStep(boolean reverse) {
		// The gyro angle is exactly opposite to the motor rotation angle
		float currentAngle = this.getCurrentGyro();
		if (Math.abs(this.courseFollowingAngle - currentAngle) >= 1) {
			this.turn((int) (this.courseFollowingAngle - currentAngle), reverse);
		} else {
			if (reverse) {
				this.backward();
			} else {
				this.forward();
			}
		}
	}

	@Override
	public float convertTachoCountToDistance(int tachoCount) {
		float factor = 0.0423f; // measured
		return tachoCount * factor;
	}

	@Override
	public int getLeftTachoCount() {
		return this.motorLeft.getTachoCount();
	}

	@Override
	public int getRightTachoCount() {
		return this.motorRight.getTachoCount();
	}

	@Override
	public void resetLeftTachoCount() {
		this.motorLeft.resetTachoCount();
	}

	@Override
	public void resetRightTachoCount() {
		this.motorRight.resetTachoCount();
	}

	@Override
	public float getLeftTachoDistance() {
		return this.convertTachoCountToDistance(this.getLeftTachoCount());
	}

	@Override
	public float getRightTachoDistance() {
		return this.convertTachoCountToDistance(this.getRightTachoCount());
	}

	@Override
	public float getCurrentDistance() {
		return sensorSampler.getCurrentUltrasonic() * 100.f;
	}
	
	@Override
	public boolean isAmbientLightOn(){
		return sensorSampler.getMeanAmbientLight() > MEAN_AMBIENT_TRESHOLD;
	}
}
