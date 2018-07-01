import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;



public class Participant extends JFrame //implements Runnable
{
	static Socket socket;
	static Scanner inStream;
	static PrintWriter outStream;
	static JButton clientCommitBtn;
	static JButton clientAbortBtn;
	static String state ="";
	static JTextArea clientTxtDisplay;
	static String mainMessage="";
	static int counter =0,participant_counter=0;
	Date currentdate = new Date();
	DateFormat dateformat= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",Locale.US);
	static String dateString="";
	static String voteDecision="";
	static String httpStringPost="";
	public static Timer timer = new Timer();
	/*
	 * TimerTask which sends out a decision request
	 * which sends out a global commit or global abort
	 * after a vote is made
	 */
	public static TimerTask timertask=new TimerTask() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(counter<2) {
				//state="";
				System.out.println("sending decision request");
				voteDecision= httpStringPost+ mainMessage.length() + "\r\n"+ "Date: "+dateString 
						+ "\r\n"+"Body: "+"DESICION_REQUEST" +"\r\n";
				outStream.println(voteDecision);
				//outStream.println("DESICION_REQUEST");
				outStream.flush();

			}
			
		}
	};
	
	Participant() 
	{
		httpStringPost="POST HTTP/1.1\r\n Host:localhost\r\n User-Agent: HTTPTool/1.0\r\n"
				+ "Content-Type: text/html\r\n" + "Content-Length: " ;
		dateString=  dateformat.format(currentdate) ;
		clientCommitBtn = new JButton("COMMIT");
		clientAbortBtn = new JButton("ABORT");
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		clientAbortBtn.setEnabled(false);
		clientCommitBtn.setEnabled(false);
		clientCommitBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{

			}
		});

		clientTxtDisplay = new JTextArea();
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addGap(29)
						.addComponent(clientCommitBtn)
						.addGap(18)
						.addComponent(clientAbortBtn)
						.addContainerGap(220, Short.MAX_VALUE))
				.addGroup(groupLayout.createSequentialGroup()
						.addGap(67)
						.addComponent(clientTxtDisplay)
						.addGap(232))
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addGap(66)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(clientAbortBtn)
								.addComponent(clientCommitBtn))
						.addPreferredGap(ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
						.addComponent(clientTxtDisplay, GroupLayout.PREFERRED_SIZE, 127, GroupLayout.PREFERRED_SIZE)
						.addGap(18))
				);
		getContentPane().setLayout(groupLayout);
		setSize(500, 250);
		readFromFile();
	}
	
	/*
	 * Launching the GUI
	 */
	public static void main(String[] args)
	{
		try
		{
			new Participant();
			try {
				socket = new Socket("127.0.0.1",8888);
				outStream =new PrintWriter(socket.getOutputStream());
				inStream = new Scanner(socket.getInputStream());
				while(true) {
					Receive();

				}
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}

	}
	/*
	 * function to read from file upon loading the process
	 */
	public static void readFromFile() {
		try {
			BufferedReader input = new BufferedReader(
					new FileReader( "participant1.txt" ) );
			//System.out.println(input.readLine());
			clientTxtDisplay.setText(input.readLine()+"\n");
				

			input.close();

		} catch( IOException e ) {
			System.out.println("\nProblem reading from file!\n" +
					"Try again.");
			e.printStackTrace();
		}
	}

	/*
	 * Function to receive message from Coordinator
	 * and checks whether its a GLOBAL COMMIT or GLOBAL ABORT
	 * 
	 */
	public static void Receive() 
	{
		Scanner input;
		
		try {
			input = new Scanner(socket.getInputStream());
			//clientTxtDisplay.setText("");
			if(input.hasNext())
			{
				String Message = input.nextLine();
				if(Message.contains("VOTE_REQUEST"))
				{
					counter=1;
					clientTxtDisplay.setText("");
					clientCommitBtn.setEnabled(true);
					clientAbortBtn.setEnabled(true);
					clientTxtDisplay.append(Message+"\n");
					mainMessage=Message.substring(18, Message.length());
					System.out.println(mainMessage);
					clientCommitBtn.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							//System.out.println("in commit");
							state="READY";
							voteDecision= httpStringPost+ mainMessage.length() + "\r\n"+ "Date: "+dateString 
									+ "\r\n"+"Body: "+"VOTE_COMMIT" +"\r\n";
							outStream.println(voteDecision);
							//outStream.flush();
							outStream.flush();
							//Schedules a timer for 20 seconds to wait for a global commit or abort
							timer.schedule(timertask, 20000);
						}
					});
					/*
					 * If client abort button pressed, then vote abort sent has HTTP message
					 */
					clientAbortBtn.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							state="ABORT";
							voteDecision= httpStringPost+ mainMessage.length() + "\r\n"+ "Date: "+dateString 
									+ "\r\n"+"Body: "+"VOTE_ABORT" +"\r\n";
							outStream.println(voteDecision);
							//outStream.println("VOTE_ABORT");
							outStream.flush();
							timer.schedule(timertask, 20000);
						}
					});

				}
				else 
				{
					clientTxtDisplay.append(Message+"\n");
					if(Message.contains("PARTICIPANT_GLOBAL_COMMIT"))
					{
						participant_counter++;
						

					}
					else if(Message.contains("PARTICIPANT_GLOBAL_ABORT"))
						{
							//counter=2;
							clientAbortBtn.setEnabled(false);
							clientCommitBtn.setEnabled(false);
							state = "ABORT";
							voteDecision= httpStringPost+ mainMessage.length() + "\r\n"+ "Date: "+dateString 
									+ "\r\n"+"Body: "+"GLOBAL_ABORT" +"\r\n";
							outStream.println(voteDecision);
							//outStream.println("GLOBAL_ABORT");
							outStream.flush();
							
						}
					//checks if received a global commit or global abort received from
					//any of the participants
					if(participant_counter==2) {
						voteDecision= httpStringPost+ mainMessage.length() + "\r\n"+ "Date: "+dateString 
								+ "\r\n"+"Body: "+"GLOBAL_COMMIT" +"\r\n";
						outStream.println(voteDecision);
						//outStream.println("GLOBAL_COMMIT");
						outStream.flush();
						
					}
					if(Message.contains("GLOBAL_COMMIT"))
					{	//checks if received a global commit or global abort received from
						//any of the coordinator
						counter=2;
						clientAbortBtn.setEnabled(false);
						clientCommitBtn.setEnabled(false);
						char str = Message.charAt(0);
						state = "COMMIT";
						System.out.println("am writting into file  - content to be written "+mainMessage);
						writeToFile(mainMessage,str);
						System.out.println(state);

					}
					else if(Message.contains("GLOBAL_ABORT"))
						{
							counter=2;
							clientAbortBtn.setEnabled(false);
							clientCommitBtn.setEnabled(false);
							state = "ABORT";
							System.out.println(state);
							clientTxtDisplay.setText("****ABORT****\n");
							
						}
					else if(Message.contains("DESICION_REQUEST")) {
						if(state.equals("READY")) {
							voteDecision= httpStringPost+ mainMessage.length() + "\r\n"+ "Date: "+dateString 
									+ "\r\n"+"Body: "+"PARTICIPANT_GLOBAL_COMMIT" +"\r\n";
							outStream.println(voteDecision);
						//	outStream.println("PARTICIPANT_GLOBAL_COMMIT");
							outStream.flush();
						}
						else {
							voteDecision= httpStringPost+ mainMessage.length() + "\r\n"+ "Date: "+dateString 
									+ "\r\n"+"Body: "+"PARTICIPANT_GLOBAL_ABORT" +"\r\n";
							outStream.println(voteDecision);
							//	outStream.println("PARTICIPANT_GLOBAL_ABORT");
								outStream.flush();
							
						}
					}
				}
			}
			//input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	/*
	 * Writes to file upon global commit
	 */
	public static void writeToFile(String entailmentResult,char str) {
		try {
			BufferedWriter output = new BufferedWriter(
					new FileWriter( "participant"+str+".txt" ) );

					output.write( entailmentResult);
				

			output.close();

		} catch( IOException e ) {
			System.out.println("\nProblem writing to the output file!\n" +
					"Try again.");
			e.printStackTrace();
		}
	}
}
