package HAL;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;

public interface IHAL {

	void printOnDisplay(String text, int row, long waitDuration);
	
	void backward();
	void forward();
	void stop();
	void rotate(int angle, boolean returnImmediately);	
	void turn(int angle, boolean stopInnerChain, boolean immediateReturn);
	
	void moveDistanceSensorToPosition(DistanceSensorPosition position, boolean immediateReturn);
	boolean motorsAreMoving();
	boolean isRotating();
	
	float getRGB();
	float getDistance();	
	void resetGyro();
	float getGyroValue();
	
	EV3ColorSensor getColorSensor(); //remove later
	EV3UltrasonicSensor getUltrasonicSensor();

	boolean isTouchButtonPressed();
}
