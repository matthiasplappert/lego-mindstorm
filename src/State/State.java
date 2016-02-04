package State;

public enum State {
	
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
