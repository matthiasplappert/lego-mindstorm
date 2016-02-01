package common;


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
}
