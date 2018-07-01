import javax.swing.JFrame;
import javax.swing.JTextField;


import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;

public class Coordinator extends JFrame //implements Runnable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8448163882421419811L;
	static Socket socket;
	static BufferedReader inStream;
	static PrintWriter outStream;
	public static JTextField coordMsgSend;
	public static javax.management.timer.Timer timer_news;
	static String coordinatorMessage;
	static String state= "INIT";
	static int secondsCounter=0;
	public static Coordinator obj;
	static int counter=0;
	static String httpStringGet="";
	static String messageToClients ="",dateString="";
	Date currentdate = new Date();
	DateFormat dateformat= new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Timer timer = new Timer();
	TimerTask timertask=new TimerTask() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(counter!=3) {
				sendFinalResultToClient("GLOBAL_ABORT");
			}
			
		}
	};
	public Coordinator()
	{
		getContentPane().setLayout(null);
		httpStringGet="GET /path/file.html HTTP/1.0\r\n User-Agent: HTTPTool/1.0\r\n"
				+ "Content-Type: text/html\r\n" + "Content-Length\r\n" + dateformat.format(currentdate) 
				+"Body";
		dateString=  dateformat.format(currentdate) ;
		coordMsgSend = new JTextField();
		coordMsgSend.setBounds(6, 28, 213, 26);
		getContentPane().add(coordMsgSend);
		coordMsgSend.setColumns(10);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		JButton btnSendToClient = new JButton("SEND TO CLIENTS");
		btnSendToClient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageToClients=coordMsgSend.getText();
				coordinatorMessage = httpStringGet + messageToClients.length() +"\r\n"+ "Date: "+dateString 
						+ "\r\n"+"Body: "+"VOTE_REQUEST" + messageToClients+"\r\n";
				//sending the message to server
				//outStream.println(coordinatorMessage);
				//sending the message to server
				outStream.println(coordinatorMessage);
				outStream.flush();
				state= "WAIT";

				if(state=="WAIT")
				{
					timer.schedule(timertask,10000);
					timerfunction(System.currentTimeMillis());
					/*try {
						obj.wait(20000);
						if(counter!=3) {
							sendFinalResultToClient("GLOBAL_ABORT");
						}
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}*/
				}

			}
		});
		btnSendToClient.setBounds(256, 28, 149, 29);
		getContentPane().add(btnSendToClient);

		JLabel lblEnterTextHere = new JLabel("Enter Text Here");
		lblEnterTextHere.setBounds(17, 6, 117, 16);
		getContentPane().add(lblEnterTextHere);
		setSize(500, 100);
	}

	public static void main(String[] args)
	{	

		try
		{
			socket = new Socket("127.0.0.1",8888);
			outStream =new PrintWriter(socket.getOutputStream());
			inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			//getting the message from GUI text box

			obj=new Coordinator();
			//Thread cordinatorThread = new Thread(obj);

			//Thread clientThread=new Thread(new Participant());
			//clientThread.start();
		}
		catch (Exception e) 
		{
			// TODO: handle exception
		}

	}

	public static void timerfunction(long startedTime)
	{



		String clientMessage="";
		String finaldecision= "GLOBAL_ABORT";
		System.out.println("Inside Timer function");
		try
		{


		
			while(true)	
			{	
				System.out.println("Inside while true-----");

				System.out.println(System.currentTimeMillis()-startedTime);
				//long currentTime = System.currentTimeMillis();
				/*if(System.currentTimeMillis()-startedTime==20) {
							sendFinalResultToClient("GLOBAL_ABORT");
							break;
						}*/
				if(System.currentTimeMillis()-startedTime >20000) {
					sendFinalResultToClient("GLOBAL_ABORT");
					break;
				}
				//inStream.
				else {
					clientMessage=inStream.readLine();
					System.out.println("the client message"+clientMessage);
					if(clientMessage.equals("")) {
						System.out.println("in if");
						continue;


					}
					//Checks if the message has Vote Commit.
					//if Yes, then finaldecision made global commit.
					else
					{
						
						if(clientMessage.contains("VOTE_COMMIT")) {
							
							finaldecision = "GLOBAL_COMMIT";
							counter ++;
						}
						//Checks if at least one of the clients have sent
						// a vote abort
						else if(clientMessage.contains("VOTE_ABORT")) {
							System.out.println("inside vote abort");
							finaldecision = "GLOBAL_ABORT";
							counter=0;
							sendFinalResultToClient(finaldecision);
							break;
						}
						//counter to check if we have received vote from
						//all the three participants
						if(counter == 3) {
							System.out.println("inside counter==3");
							sendFinalResultToClient(finaldecision);
							break;
						}
						
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		

	}
	/*
	 * Function to send the final Decision to all the clients
	 * whether its a global commit or global abort.
	 */
	public static void sendFinalResultToClient(String decision) {
		
		coordinatorMessage = httpStringGet + decision.length() +"\r\n"+ "Date: "+dateString 
				+ "\r\n"+"Body: "+decision+"\r\n";
		//sending the message to server
		outStream.println(coordinatorMessage);
		outStream.flush();
	}



}

