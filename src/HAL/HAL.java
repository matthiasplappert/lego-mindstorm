package HAL;

import java.util.Objects;

import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
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
	private RegulatedMotor motorUltrasonic;
	private EV3GyroSensor gyro;
	private EV3UltrasonicSensor ultrasonic;
	private EV3ColorSensor colorsensor;
	private EV3TouchSensor touchSensor;
	private float[] sample = new float[1];

	public HAL() {
		this.motorLeft = new EV3LargeRegulatedMotor(MotorPort.A);
		this.motorRight = new EV3LargeRegulatedMotor(MotorPort.B);
		this.motorUltrasonic = new EV3MediumRegulatedMotor(MotorPort.C);
		this.gyro = new EV3GyroSensor(SensorPort.S4);
		this.ultrasonic = new EV3UltrasonicSensor(SensorPort.S3);
		this.colorsensor = new EV3ColorSensor(SensorPort.S1);
		this.touchSensor = new EV3TouchSensor(SensorPort.S2);
		
		this.motorUltrasonic.setSpeed(50);		
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
		boolean rotateWithGyro = true;
		int sign = (int) Math.signum(angle);
		int rotationStep = 10;
				
		if(rotateWithGyro){
			this.resetGyro();
			do{
				this.motorLeft.rotate(sign*rotationStep, true);
				this.motorRight.rotate(-sign*rotationStep, true);
			}while(Math.abs(this.getGyroValue()) < Math.abs(angle));
		}else{
			this.motorLeft.rotate(angle, true);
			this.motorRight.rotate(-angle, true);
			if (immediateReturn) {
				return;
			}
			while (this.motorsAreMoving())
				Thread.yield();
		}
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
		return 0;
		
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
	 */
	@Override
	public void moveDistanceSensorToPosition(DistanceSensorPosition position, boolean returnImmediately) {
		switch (position) {
		case RIGHT:
			motorUltrasonic.rotateTo(0);
			break;
		case RIGHT_DOWN:
			motorUltrasonic.rotateTo(-45);
			break;
		case DOWN:
			motorUltrasonic.rotateTo(-90);
			break;
		case LEFT_DOWN:
			motorUltrasonic.rotateTo(-135);
			break;
		case LEFT:
			motorUltrasonic.rotateTo(-180);
			break;
		default:
			motorUltrasonic.rotateTo(0);
		}
		while (!returnImmediately && this.motorUltrasonic.isMoving()) {
			Thread.yield();
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

	@Override
	public EV3ColorSensor getColorSensor() {
		// TODO Auto-generated method stub
		return colorsensor;
	}

	@Override
	public EV3UltrasonicSensor getUltrasonicSensor() {
		return this.ultrasonic;
	}
}
