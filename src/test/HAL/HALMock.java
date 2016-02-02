package test.HAL;

import java.util.ArrayList;
import java.util.List;

import HAL.HALHelper;
import HAL.IHAL;

public class HALMock implements IHAL{
	List<Object> storage;
	public HALMock() {
		this.storage = new ArrayList<Object>();
	}
	public HALMock(List<Object> storage) {
		this.storage = new ArrayList<Object>();
		this.storage.add(storage);
	}
	
	public List<Object> getStorage(){
		return java.util.Collections.unmodifiableList(this.storage);
	}
	@Override
	public void printOnDisplay(String text, long waitDuration) {
		this.storage.add(text);
		if(waitDuration > 0)
			HALHelper.sleep(waitDuration);
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
