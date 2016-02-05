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
	RollBoxState,
	FindLineState,
	SensorDataState,
	DriveByState,
	mazeState,
	BarcodeState;
	
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
			return State.LineSearch;
		}
	}
	
	public static State getInitState(){
		// TODO: change this
		return State.SensorDataState;
	}
}
