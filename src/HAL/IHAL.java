package HAL;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;

public interface IHAL {

	void printOnDisplay(String text, long waitDuration);
	
	void backward();
	void forward();
	void stop();
	void rotate(int angle, boolean returnImmediately);
	float getRGB();
	float getDistance();
	void moveDistanceSensorToPosition(DistanceSensorPosition position, boolean returnImmediately);
	boolean motorsAreMoving();
	void resetGyro();
	float getGyroValue();
	
	EV3ColorSensor getColorSensor();
	EV3UltrasonicSensor getUltrasonicSensor();

	boolean isTouchButtonPressed();
}
