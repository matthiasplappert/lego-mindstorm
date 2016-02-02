package State;

public enum State {
	
	T1, TestState,
	BossState,
	BridgeState,
	ElevatorState,
	FreeTrackState,
	HangingBridgeState,
	LineSearch,
	MazeState,
	RockerState,
	RollBoxState;
	public static State getInitState(){
		return State.LineSearch;
	}
}
