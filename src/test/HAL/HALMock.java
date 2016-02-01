package test.HAL;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

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
	public void printOnDisplay(String text, Optional<Long> waitDuration) {
		this.storage.add(text);
	}

	

}
