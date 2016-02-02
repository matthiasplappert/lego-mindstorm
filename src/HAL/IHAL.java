package HAL;


public interface IHAL {

	void printOnDisplay(String text, long waitDuration);
	
	void backward();
	void forward();
	void stop();
	void rotate(int angle, boolean returnImmediately);
	float getRGB();
	float getDistance();
	void moveDistanceSensorToPosition(int position);
	boolean motorsAreMoving();
	void resetGyro();
	float getGyroValue();

	boolean isTouchButtonPressed();
}
