
public class ElevatorProxy {
	
	public static String FREE_STRING =  "FREE";
	public static String BUSY_STRING =  "BUSY";
	public static String OK_STRING =  "OK";
	public static String BAD_STRING = "BAD, BAD ROBOT";
	//call path: (go_up,go_down)
	private String url;
	private boolean goUpCalled;
	
	
	public ElevatorProxy(String url){
		this.url = url;
		this.goUpCalled = false;
		//TODO: initialize rest api
	}
	
	/**
	 * Notificate the elevator that we will go  up.
	 * @return true if elevator was avaiable, false if elevator is busy.
	 */
	public boolean go_up(){
//		if(this.isFree()){//only call elevator when it is avaiable
			//call /go_up/ and store returned String in rest_result
			String rest_result = "BUSY";//call to rest
			if(rest_result.equalsIgnoreCase(OK_STRING)){
				this.goUpCalled = true;
				return true;
			}
			else if(rest_result.equalsIgnoreCase(BUSY_STRING)){
				return false;
			}
			else
				throw new IllegalStateException("/go_up does not return specified values");

//		}
//		return false;
	}
	
	public boolean go_down(){
		if(!this.goUpCalled){//only call /go_down if we have previously called go_up. TODO: check for 30 sec time diff.
			throw new IllegalStateException("go_up has not been called");
		}
			String rest_result = BAD_STRING;//call to rest
			if(rest_result.equalsIgnoreCase(OK_STRING)){
				this.goUpCalled = false;
				return true;
			}
			else if(rest_result.equalsIgnoreCase(BAD_STRING)){
				return false;
			}
			else{
				throw new IllegalStateException("/go_down does not return specified values");

			}
		}
	}
