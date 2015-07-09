package com.ppi.oorapinterpreter;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

public class RAPGUI extends WindowAdapter implements WindowListener, KeyListener, ActionListener, InputMethodListener, Runnable
{
	private JFrame frame;
	private JTextArea textArea;
	private JButton button;
	private JTextArea consoleInputBox;
	private JTextArea codeEditBox;
	private Thread reader;
	private Thread reader2;
	private boolean quit;
					
	private final PipedInputStream pin=new PipedInputStream(); 
	private final PipedInputStream pin2=new PipedInputStream(); 
	private final InputStream cin = new PipedInputStream();

	Thread errorThrower; // just for testing (Throws an Exception at this Console
	
	PipedOutputStream outstr = new PipedOutputStream();
	PipedInputStream instr;
	BufferedReader stdin;
	
	Thread RAPThread=null;
	
	public RAPGUI()
	{
		// create all components and add them
		frame=new JFrame("Java Console");
		Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize=new Dimension((int)(screenSize.width/2),(int)(screenSize.height/2));
		int x=(int)(frameSize.width/2);
		int y=(int)(frameSize.height/2);
		frame.setBounds(x,y,frameSize.width,frameSize.height);
		
		textArea=new JTextArea();
		textArea.setEditable(false);
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		
		
		button=new JButton("RUN SCRIPT");
		consoleInputBox=new JTextArea();
		consoleInputBox.setEditable(true);
		codeEditBox=new JTextArea();
		consoleInputBox.setEditable(true);
		codeEditBox.setPreferredSize(new Dimension(500,500));
		
		caret = (DefaultCaret)consoleInputBox.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		codeEditBox.setText(""+
				"console write line Hello!\n"+
			  "loop forever\n"+
				"  console write line \"\"\n"+
				"  console write line \"Guess a Number!\"\n"+
				"  console write text \"> \"\n"+
				"  define guess as console read number\n"+
				"  if guess = 4\n"+
				"    console write line \"THAT IS A FOUR!!!\"\n"+
				"  else\n"+
				"    console write line \"I prefer fours.\"\n");

		frame.getContentPane().setLayout(null);//new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		//frame.getContentPane().setBorder(new EmptyBorder(new Insets(40, 60, 40, 60)));

		int width=500;
		int height=600;
		JScrollPane jsp = new JScrollPane(codeEditBox);
		jsp.setBounds(0,0,width,height);
		frame.getContentPane().add(jsp);
		
		button.setBounds(0,height,width,100);
		frame.getContentPane().add(button);
		
		JScrollPane jsp2 = new JScrollPane(textArea);
		jsp2.setBounds(width,0,width,height);
		frame.getContentPane().add(jsp2);
		
		consoleInputBox.setBounds(width,height,width,100);
		frame.getContentPane().add(consoleInputBox);
		
		frame.setVisible(true);		
		
		frame.addWindowListener(this);		
		button.addActionListener(this);
		consoleInputBox.addKeyListener(this);
		
		try
		{
			PipedOutputStream pout=new PipedOutputStream(this.pin);
			System.setOut(new PrintStream(pout,true)); 
		} 
		catch (java.io.IOException io)
		{
			textArea.append("Couldn't redirect STDOUT to this console\n"+io.getMessage());
		}
		catch (SecurityException se)
		{
			textArea.append("Couldn't redirect STDOUT to this console\n"+se.getMessage());
	    } 
		
		try 
		{
			PipedOutputStream pout2=new PipedOutputStream(this.pin2);
			System.setErr(new PrintStream(pout2,true));
		} 
		catch (java.io.IOException io)
		{
			textArea.append("Couldn't redirect STDERR to this console\n"+io.getMessage());
		}
		catch (SecurityException se)
		{
			textArea.append("Couldn't redirect STDERR to this console\n"+se.getMessage());
	    } 		
			
		quit=false; // signals the Threads that they should exit
				
		// Starting two seperate threads to read from the PipedInputStreams				
		//
		reader=new Thread(this);
		reader.setDaemon(true);	
		reader.start();	
		//
		reader2=new Thread(this);	
		reader2.setDaemon(true);	
		reader2.start();
				

		
		
		try {
			instr = new PipedInputStream(outstr);
			stdin = new BufferedReader(new InputStreamReader(instr));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		
	}
	
	public synchronized void windowClosed(WindowEvent evt)
	{
		quit=true;
		this.notifyAll(); // stop all threads
		try { reader.join(1000);pin.close();   } catch (Exception e){}		
		try { reader2.join(1000);pin2.close(); } catch (Exception e){}
		System.exit(0);
	}		
		
	public synchronized void windowClosing(WindowEvent evt)
	{
		frame.setVisible(false); // default behaviour of JFrame	
		frame.dispose();
	}
	
	
	
	@SuppressWarnings("deprecation")
	public synchronized void actionPerformed(ActionEvent evt)
	{
		textArea.setText("");
		if (RAPThread!=null)
			RAPThread.stop();
		RAPThread = new Thread(new Runnable() {
	     public void run() {
	    	try {
	   			new OORAPInterpreter(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(codeEditBox.getText().getBytes()))), stdin).run();
	   		  System.out.println("PROGRAM FININSHED!");
	    	} catch (Exception e) {
	   			System.out.println(e);
	   		}
	     }
		});  
		RAPThread.start();
	}

	public synchronized void run()
	{
		try
		{			
			while (Thread.currentThread()==reader)
			{
				try { this.wait(100);}catch(InterruptedException ie) {}
				if (pin.available()!=0)
				{
					String input=this.readLine(pin);
					textArea.append(input);
				}
				if (quit) return;
			}
		
			while (Thread.currentThread()==reader2)
			{
				try { this.wait(100);}catch(InterruptedException ie) {}
				if (pin2.available()!=0)
				{
					String input=this.readLine(pin2);
					textArea.append(input);
				}
				if (quit) return;
			}			
		} catch (Exception e)
		{
			textArea.append("\nConsole reports an Internal error.");
			textArea.append("The error is: "+e);			
		}
		
		// just for testing (Throw a Nullpointer after 1 second)
		if (Thread.currentThread()==errorThrower)
		{
			try { this.wait(1000); }catch(InterruptedException ie){}
			throw new NullPointerException("Application test: throwing an NullPointerException It should arrive at the console");
		}

	}
	
	public synchronized String readLine(PipedInputStream in) throws IOException
	{
		String input="";
		do
		{
			int available=in.available();
			if (available==0) break;
			byte b[]=new byte[available];
			in.read(b);
			input=input+new String(b,0,b.length);														
		}while( !input.endsWith("\n") &&  !input.endsWith("\r\n") && !quit);
		return input;
	}	
		
	public static void main(String[] arg)
	{
		new RAPGUI(); // create console with not reference	
	}

	@Override
	public void caretPositionChanged(InputMethodEvent arg0) {
	// TODO Auto-generated method stub
	System.out.println("huh");
	}

	@Override
	public void inputMethodTextChanged(InputMethodEvent arg0) {
	// TODO Auto-generated method stub
		System.out.println("huh");
	}

	@Override
	public void keyPressed(KeyEvent e) {
	// TODO Auto-generated method stub
		if (e.getKeyChar() == '\n') {
			try {
				outstr.write((consoleInputBox.getText()+"\n").getBytes());
				System.out.println(consoleInputBox.getText());
				consoleInputBox.setText("");
				e.consume();
			} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			}
		}
		
			//System.out.println(consoleInputBox.getText());
	
	}

	@Override
	public void keyReleased(KeyEvent e) {
	// TODO Auto-generated method stub
	
	}

	@Override
	public void keyTyped(KeyEvent e) {
	// TODO Auto-generated method stub
	
	}			
}