package HAL;

import Behaviors.LineType;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;

public interface IHAL {

	void printOnDisplay(String text, int row, long waitDuration);
	
	void setSpeed(Speed speed);
	void backward();
	void forward();
	void stop();
	void rotate(int angle);	
	void turn(int angle);
	
	void moveDistanceSensorToPosition(DistanceSensorPosition position);
	void moveDistanceSensorToPosition(int angle);
	boolean motorsAreMoving();
	boolean isRotating();
	
//	float getRGB();
	float getMeanDistance();	
	void resetGyro();
	float getCurrentGyro();
	float getMeanGyro();
	
	EV3ColorSensor getColorSensor(); //remove later
	EV3UltrasonicSensor getUltrasonicSensor();

	boolean isTouchButtonPressed();

	LineType getLineType();

	float getMeanColor();

	ColorMode getColorMode();

	void setColorMode(ColorMode cm);

	boolean isRedColorMode();
}
