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
	RollBoxState, FindLineState, DriveByState;
	public static State getInitState(){
		return State.LineSearch;
	}
}
