package HAL;

import java.util.Objects;

import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.RegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;

/**
 * @author David
 *
 */
public class HAL implements IHAL {
	private RegulatedMotor motorLeft;
	private RegulatedMotor motorRight;
	private EV3GyroSensor gyro;
	private EV3UltrasonicSensor ultrasonic;
	private EV3ColorSensor colorsensor;
	private EV3TouchSensor touchSensor;
	private float[] sample = new float[1];

	public HAL() {
		this.motorLeft = new EV3LargeRegulatedMotor(MotorPort.A);
		this.motorRight = new EV3LargeRegulatedMotor(MotorPort.B);
		this.gyro = new EV3GyroSensor(SensorPort.S4);
		this.ultrasonic = new EV3UltrasonicSensor(SensorPort.S3);
		this.colorsensor = new EV3ColorSensor(SensorPort.S1);
		this.touchSensor = new EV3TouchSensor(SensorPort.S2);
		Motor.C.setSpeed(50);
	}

	@Override
	public void printOnDisplay(String text, final long waitDuration) {
		if (text.isEmpty() || text == null)
			throw new IllegalArgumentException();
		Objects.requireNonNull(waitDuration);
		LCD.drawString(text, 0, 0);
		if (waitDuration > 0)
			HALHelper.sleep(waitDuration);
	}

	@Override
	public void forward() {
		this.motorLeft.forward();
		this.motorRight.forward();
	}

	@Override
	public void backward() {
		this.motorLeft.backward();
		this.motorRight.backward();
	}

	@Override
	public void stop() {
		this.motorLeft.stop();
		this.motorRight.stop();
	}

	@Override
	public void rotate(int angle, boolean immediateReturn) {
		this.motorLeft.rotate(angle, true);
		this.motorRight.rotate(-angle, true);
		if (immediateReturn) {
			return;
		}
		while (this.motorsAreMoving())
			Thread.yield();
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

	@Override
	public float getRGB() {
		
	}

	/*Returns the Distance measured by the ultrasonic sensor
	 * returns the distance in cm
	 */
	@Override
	
	public float getDistance() {
		// TODO Auto-generated method stub
		ultrasonic.enable();
		ultrasonic.getDistanceMode().fetchSample(sample, 0);
		ultrasonic.disable();
		return sample[0]/100;
	}

	
	/* Rotates the Distance Sensor to a given position
	 * 0: right
	 * 1: right-down
	 * 2: down
	 * 3: left-down
	 * 4: left
	 */
	@Override
	public void moveDistanceSensorToPosition(int position) {
		switch (position) {
		case 0:
			Motor.C.rotateTo(0);
			break;
		case 1:
			Motor.C.rotateTo(-45);
			break;
		case 2:
			Motor.C.rotateTo(-90);
			break;
		case 3:
			Motor.C.rotateTo(-135);
			break;
		case 4:
			Motor.C.rotateTo(-180);
			break;
		default:
			Motor.C.rotateTo(0);
		}

	}

	//Resets the gyroscope to zero
	@Override
	public void resetGyro() {
		gyro.reset();
	}

	//Returns the current angle(degrees) measured by the gyroscope
	@Override
	public float getGyroValue() {
		gyro.getAngleMode().fetchSample(sample, 0);
		return sample[0];
	}
}
