package HAL;

import Behaviors.LineType;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;

public interface IHAL {

	void printOnDisplay(String text, int row, long waitDuration);
	
	void backward();
	void forward();
	void forward(Speed speed);
	void stop();
	void rotate(int angle, boolean returnImmediately);	
	void turn(int angle, boolean stopInnerChain, boolean immediateReturn);
	
	void moveDistanceSensorToPosition(DistanceSensorPosition position, boolean immediateReturn);
	void moveDistanceSensorToPosition(int angle, boolean immediateReturn);
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
