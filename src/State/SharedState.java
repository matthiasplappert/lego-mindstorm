package State;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SharedState {
	private MyState state;
	
	private Lock lock;
	
	public SharedState(MyState initialState) {
		this.lock = new ReentrantLock();
		this.state = initialState;
	}

	public MyState getState() {
		this.lock.lock();
		final MyState state = this.state;
		this.lock.unlock();
		return state;
	}
	
	public void setState(MyState state) {
		this.lock.lock();
		this.state = state;
		this.lock.unlock();
	}
	
	public void reset(boolean yield){
		this.setState(MyState.getInitState());
		if (yield)
			Thread.yield();
	}
}
