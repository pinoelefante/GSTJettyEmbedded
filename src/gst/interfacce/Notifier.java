package gst.interfacce;

public interface Notifier {
	public void subscribe(Notificable e);
	public void unsubscribe(Notificable e);
	public void inviaNotifica(String text);
}
