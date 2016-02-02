package test.HAL;

import static org.junit.Assert.fail;

import HAL.IHAL;

public class DefaultTestHAL implements IHAL{

	@Override
	public void printOnDisplay(String text, long waitDuration) {
		fail();
	}

	@Override
	public void backward() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forward() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rotate(int angle, boolean returnImmediately) {
		// TODO Auto-generated method stub
		
	}

}
