package State;

public class StateChangeEvent {
	private State oldState, newState;
	public StateChangeEvent(State oldState, State newState){
		this.oldState = oldState;
		this.newState = newState;
	}
	public State getOldState(){ return this.oldState;}
	public State getNewState(){ return this.newState;}
}
