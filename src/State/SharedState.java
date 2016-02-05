package State;


public class SharedState {
	private State state;
	
	public SharedState(State initialState) {
		this.state = initialState;
	}

	public State getState() {
		return this.state;
	}
	
	public void setState(State state) {
		this.state = state;
	}
	
	public void reset(boolean yield){
		this.ensureState(State.getInitState());
		if (yield)
			Thread.yield();
	}
	/**
	 * If the internal state is equals to the expectedState, set the internal state to newState. Otherwise do nothing.
	 * @param expectedState
	 * @param newState
	 */
	public void setAndCheckState(State expectedState, State newState){
		if(this.state.equals(expectedState))
			this.setState(newState);
	}
	/**
	 * Ensure that the internal state is set to expectedState. The variable content is only changed if the internal state is not expectedState.
	 * @param expectedState
	 */
	public void ensureState(State expectedState){
		if(!this.state.equals(expectedState))
			this.setState(expectedState);
	}
	/**
	 * Use this method if you want to set the state within an Behavior.
	 * Switch the state to the given one and yield current thread. 
	 * @param newState
	 */
	public void switchState(State newState){
		this.setState(newState);
		Thread.yield();
	}
}
