package State;

public enum State {
	
	LineSearchState,
	BarcodeState,
	BridgeState,
	ElevatorState,
	FreeTrackState,
	HangingBridgeState,
	RockerState,
	RollBoxState,
	MazeState,
	FindLineState,
	SensorDataState,
	DriveByState,
	ObstacleEndState,
	BossState,
	ShutDownState,
	ExitState;
	
	public static State getFromBarcode(int barcode) {
		switch (barcode) {
		case 3:
			return State.BridgeState;
		case 4:
			return State.RockerState;
		case 5:
			return State.HangingBridgeState;
		case 6:
			return State.RollBoxState;
		case 1:
			return State.FreeTrackState;
		default:
			return State.LineSearchState;
		}
	}
	
	public static State getInitState(){
		return State.ObstacleEndState;
	}
}
