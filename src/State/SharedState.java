package State;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SharedState {
	private State state;
	
	private Lock lock;
	
	public SharedState(State initialState) {
		this.lock = new ReentrantLock();
		this.state = initialState;
	}

	public State getState() {
		this.lock.lock();
		final State state = this.state;
		this.lock.unlock();
		return state;
	}
	
	public void setState(State state) {
		this.lock.lock();
		this.state = state;
		this.lock.unlock();
	}
	
	public void reset(boolean yield){
		this.setState(State.getInitState());
		if (yield)
			Thread.yield();
	}
}
