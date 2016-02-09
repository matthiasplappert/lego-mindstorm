package State;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Behaviors.Direction;

public class SharedState {
	private MyState state;
	
	// Use this to hint to the line follow algorithm that it should first look in this direction.
	private Direction lineFollowHint = Direction.LEFT;
	
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
	
	public void setLineFollowHint(Direction hint) {
		this.lock.lock();
		this.lineFollowHint = hint;
		this.lock.unlock();
	}
	
	public Direction getLineFollowHint() {
		Direction hint;
		this.lock.lock();
		hint = this.lineFollowHint;
		this.lock.unlock();
		return hint;
	}
}
