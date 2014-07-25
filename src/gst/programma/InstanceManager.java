package gst.programma;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class InstanceManager {
	private ServerSocket socket;
	private static int listen_port=54564;
	
	public InstanceManager() {
		this(listen_port);
	}
	public InstanceManager(int port) {
		listen_port=port;
		try {
			socket=new ServerSocket(listen_port);
			server();
		}
		catch (IOException e) {
			client();
		}
	}
	private void server(){
		class ThreadServer extends Thread {
			public void run() {
				try {
					while(true){
		    			socket.accept();
		    			System.out.println("Tentativo di avviare una nuova istanza");
		    			//TODO open browser
		    			
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		ThreadServer ts=new ThreadServer();
		ts.start();
	}
	private void client() {
		System.out.println("Un'istance di GST è già avviata");
		Socket socket_stream;
		try {
			socket_stream = new Socket(InetAddress.getLocalHost(), listen_port);
			/*
			BufferedOutputStream bos=new BufferedOutputStream(socket_stream.getOutputStream());
			bos.write("-show-interface\n".getBytes());
			*/
			socket_stream.close();
			System.exit(0);
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			//caso in cui non fosse possibile scrivere sul socket o socket server non raggiungibile
			e.printStackTrace();
		}
	}
	public void disconnect(){
		if(socket!=null)
			try {
				socket.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
	}
}
