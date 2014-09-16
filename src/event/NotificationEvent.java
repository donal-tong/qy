package event;

public class NotificationEvent {
	
	private String msg;
	
	public NotificationEvent(String msg) {
		this.msg = msg;
	}
	
	public String getMsg() {
		return msg;
	}
}
