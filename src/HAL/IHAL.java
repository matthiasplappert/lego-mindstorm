package HAL;


public interface IHAL {

	void printOnDisplay(String text, long waitDuration);
	
	public void backward();
	public void forward();
	public void rotate(int angle, boolean returnImmediately);
}
