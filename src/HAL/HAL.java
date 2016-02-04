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

	private final int forwardSpeedVeryFast = 350;
	private final int forwardSpeedFast = 200;
	private final int forwardSpeedMedium = 150;
	private final int forwardSpeedSlow = 100;
	private final int forwardSpeedVerySlow = 50;
			
	private final int backwardSpeed = 200;
	private final int rotateSpeed = 200;
	private final int turnSpeedInnerStops = 20;
	private final int turnSpeedOuterStops = 200;
	private final int turnSpeedInner = 100;
	private final int turnSpeedOuter = 200;
	private final int rotationStep = 1;	


	private SensorMeanFilter meanFilter;
	
	public HAL() {
		this.motorLeft = new EV3LargeRegulatedMotor(MotorPort.A);
		this.motorRight = new EV3LargeRegulatedMotor(MotorPort.B);
		this.motorUltrasonic = new EV3MediumRegulatedMotor(MotorPort.C);
		this.gyro = new EV3GyroSensor(SensorPort.S4);
		this.ultrasonic = new EV3UltrasonicSensor(SensorPort.S3);
		this.colorsensor = new EV3ColorSensor(SensorPort.S1);
		this.touchSensor = new EV3TouchSensor(SensorPort.S2);
		this.motorUltrasonic.setSpeed(50);
		//SensorMeanFilter spans a new thread for continoous measurements
		meanFilter = new SensorMeanFilter(gyro, ultrasonic, colorsensor);
		meanFilter.start();
	}

	@Override
	public void printOnDisplay(String text, int row, final long waitDuration) {
		if (text.isEmpty() || text == null)
			throw new IllegalArgumentException();
		Objects.requireNonNull(waitDuration);
		LCD.drawString(text, 0, row);
		if (waitDuration > 0)
			Delay.msDelay(waitDuration);
	}

	@Override
	public void forward() {
		this.motorLeft.setSpeed(forwardSpeedFast);
		this.motorRight.setSpeed(forwardSpeedFast);
		
		this.motorLeft.forward();
		this.motorRight.forward();
	}
	
	@Override
	public void forward(Speed speed) {
		int s = forwardSpeedFast;
		
		switch(speed){
		case VerySlow: 	s = forwardSpeedVerySlow; break;
		case Slow: 		s = forwardSpeedSlow; break;
		case Medium:	s = forwardSpeedMedium; break;
		case Fast: 		s = forwardSpeedFast; break;
		case VeryFast: 	s = forwardSpeedVeryFast; break;
		}
				
		this.motorLeft.setSpeed(s);
		this.motorRight.setSpeed(s);
		
		this.motorLeft.forward();
		this.motorRight.forward();
	}

	@Override
	public void backward() {
		this.motorLeft.setSpeed(backwardSpeed);
		this.motorRight.setSpeed(backwardSpeed);
		
		this.motorLeft.backward();
		this.motorRight.backward();
	}

	@Override
	public void stop() {
		this.motorLeft.stop();
		this.motorRight.stop();
	}

	@Override
	/*
	 * angle >= 0: right
	 * angle < 0: left
	 */
	public void rotate(int angle, boolean immediateReturn) {
		boolean rotateWithGyro = true;
		rotateToAngle = angle;
		lastGyroAngleBeforeRotation = this.getCurrentGyro();
		int sign = (int) Math.signum(angle);	
		
		this.motorLeft.setSpeed(rotateSpeed);
		this.motorRight.setSpeed(rotateSpeed);
		
		if (rotateWithGyro) {			
			if (immediateReturn) {
				if(sign >= 0){
					this.motorLeft.forward();
					this.motorRight.backward();
				}else{
					this.motorLeft.backward();
					this.motorRight.forward();
				}							
			} else { // block until rotation is done
				do {
					this.motorLeft.rotate(sign * rotationStep, true);
					this.motorRight.rotate(-sign * rotationStep, true);
				} while (Math.abs(this.getCurrentGyro() - lastGyroAngleBeforeRotation) < Math.abs(angle));
				this.stop();
			}
		} else {
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



	/*
	 * Returns the Distance measured by the ultrasonic sensor returns the
	 * distance in cm
	 */
	@Override

	public float getMeanDistance() {
		return meanFilter.getMeanUltrasonic() * 100.f;
	}

	@Override
	public float getCurrentGyro() {
		float[] sample = new float[1];
		this.gyro.getAngleMode().fetchSample(sample, 0);
		return sample[0];
	}
	
	/*
	 * Rotates the Distance Sensor to a given position 0: right 1: right-down 2:
	 * down 3: left-down 4: left
	 */
	@Override
	public void moveDistanceSensorToPosition(DistanceSensorPosition position, boolean immediateReturn) {
		int angle;
		switch (position) {
		case DOWN:
			angle = -90;
			break;
			// alternative: angle=90 if deployed on the left-hand side of the robot
		case UP:
		default:
			angle = 0;
		}
		this.moveDistanceSensorToPosition(angle, immediateReturn);
	}
	
	@Override
	public void moveDistanceSensorToPosition(int angle, boolean immediateReturn) {
		this.motorUltrasonic.rotateTo(angle, immediateReturn);
	}

	// Resets the gyroscope to zero
	@Override
	public void resetGyro() {
		gyro.reset();
	}

	// Returns the current angle(degrees) measured by the gyroscope
	@Override
	public float getMeanGyro() {
		return meanFilter.getMeanGyro();
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
		return Math.abs(this.getCurrentGyro() - lastGyroAngleBeforeRotation) < Math.abs(rotateToAngle);		
	}
	@Override
	public float getMeanColor(){
		return this.meanFilter.getMeanColorValue();

	}
	@Override
	public void setColorMode(ColorMode cm){
		switch(cm){
		case COLORID:
			this.meanFilter.enableColorIDMode();
			break;
		case RED:
			this.meanFilter.enableRedMode();
			break;
		case RGB:
			this.meanFilter.enableRGBMode();
			break;
		default:
			break;
			
		}
	}
	
	@Override
	public ColorMode getColorMode(){
		return this.meanFilter.getColorMode();
	}
	
	@Override
	public boolean isRedColorMode(){
		return this.getColorMode().equals(ColorMode.RED);
	}
	
	@Override
	public LineType getLineType(){
		if(this.getColorMode().equals(ColorMode.RED)){
		final float data = this.getMeanColor();
			if(data <= THRESHOLD_BLACK) return LineType.BLACK;
			else if(data > THRESHOLD_BLACK && data <= THRESHOLD_BORDER) return LineType.BORDER;
			else if(data > THRESHOLD_BORDER && data <= 1.0f) return LineType.LINE;
			else return LineType.UNDEFINED;
		}
		else return LineType.UNDEFINED;
	}
	
	
	
	@Override
	/*
	 * angle >= 0: right
	 * angle < 0: left
	 * 
	 * stopInnerChain = true: inner chain nearly stops and outer rotates
	 * stopInnerChain = false: inner chain slows down and outer speeds up
	 */
	public void turn(int angle, boolean stopInnerChain, boolean immediateReturn) {		
		rotateToAngle = angle;
		lastGyroAngleBeforeRotation = this.getCurrentGyro();
		int sign = (int) Math.signum(angle);
		
		if(stopInnerChain){
			if(sign >= 0){				
				this.motorLeft.setSpeed(turnSpeedOuterStops);
				this.motorRight.setSpeed(turnSpeedInnerStops);				
			}else{
				this.motorLeft.setSpeed(turnSpeedInnerStops);				
				this.motorRight.setSpeed(turnSpeedOuterStops);
			}				
		}else{
			if(sign >= 0){				
				this.motorLeft.setSpeed(turnSpeedOuter);
				this.motorRight.setSpeed(turnSpeedInner);				
			}else{
				this.motorLeft.setSpeed(turnSpeedInner);				
				this.motorRight.setSpeed(turnSpeedOuter);
			}
		}
		this.motorLeft.forward();
		this.motorRight.forward();
		if (!immediateReturn) {// block until rotation is done			
			do {
				Delay.msDelay(10);
			} while (Math.abs(this.getCurrentGyro() - lastGyroAngleBeforeRotation) < Math.abs(angle));
			this.stop();
		}		
	}


}
