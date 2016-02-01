package State;

public enum State {
	LineSearch,
	T1, TestState;
	public static State getInitState(){
		return State.LineSearch;
	}
}
