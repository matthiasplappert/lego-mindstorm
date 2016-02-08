package State;

public class StateChangeEvent {
	private MyState oldState, newState;
	public StateChangeEvent(MyState oldState, MyState newState){
		this.oldState = oldState;
		this.newState = newState;
	}
	public MyState getOldState(){ return this.oldState;}
	public MyState getNewState(){ return this.newState;}
}
