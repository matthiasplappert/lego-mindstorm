package HAL;

import Behaviors.LineType;
import lejos.hardware.sensor.EV3ColorSensor;

public interface IHAL {

	void printOnDisplay(String text, int row, long waitDuration);
	
	void backward();
	void forward();
	void forward(Speed speed);
	void stop();
	void rotate(int angle, boolean returnImmediately);	
	void turn(int angle, boolean stopInnerChain, boolean immediateReturn);
	
	void moveDistanceSensorToPosition(int position);
	boolean motorsAreMoving();
	boolean isRotating();
	
	float getRGB();
	float getDistance();	
	void resetGyro();
	float getGyroValue();
	
	EV3ColorSensor getColorSensor(); //remove later

	boolean isTouchButtonPressed();

	float getRedColorSensorValue();

	void enableRedMode();

	LineType getLineType();

	void disableRedMode();

	void resetRedMode();

	boolean isRedMode();
}
