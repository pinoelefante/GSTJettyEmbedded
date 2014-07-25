package gst.programma;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class FrameLoading extends Thread{
	private JLabel label_caricamento;
	private JProgressBar progress;
	private JFrame frame;
	
	public void run(){
		frame = new JFrame();
		frame.setIconImage(Resource.getIcona("res/icona32.png").getImage());
		frame.setTitle("Gestione Serie TV");
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setBounds(screen.width / 2 - 300 / 2, screen.height / 2 - 80, 300, 80);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE );
		frame.setVisible(true);
		frame.setAlwaysOnTop(false);
		
		label_caricamento=new JLabel("");
		progress=new JProgressBar(0, 10);
		
		frame.setLayout(new BorderLayout());
		frame.add(label_caricamento, BorderLayout.SOUTH);

		frame.add(progress);
	}
	public void settext(String text){
		label_caricamento.setText(text);
	}
	public void setprog(int i){
		progress.setValue(i);
	}
	public void chiudi(){
		frame.dispose();
		frame=null;
	}
	public JFrame getFrame(){
		return frame;
	}
}