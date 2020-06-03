package finalproject.server;

/* 
 * Intro to JAVA : FINAL PROJECT
 * Cherian Thomas
 * kct298@nyu.edu
 * 11 May 2020
 * 20 Hours
 * The project is to build a client/server system that reads data from a DB
 * into an object and sends the object to the server. The server then writes 
 * that data into its DB.
 */
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import finalproject.entities.People;

public class Server extends JFrame implements Runnable {

	// For UI
	private static final int FRAME_WIDTH = 600;
	private static final int FRAME_HEIGHT = 800;
	private static final int AREA_ROWS = 42;
	private static final int AREA_COLUMNS = 47;
	JTextArea textQueryArea;

	// Various constants used
	private static final String SERVER_DB_NAME = "server.db";
	private static final String DB_VALUE = "DB Value:";
	private static final String ERROR_QUERYING_FROM_DB = "Error while querying from Server db: ";
	private static final String ERROR_STARTING_SERVER = "Exception while starting Server";
	private static final String DB_HEADER = "*************************************************************************************";
	private static final String SERVER_STARTED_AT = "Server started at ";
	private static final String LISTENING_ON_PORT = "Listening on port :";
	private static final String LISTENING_FOR_INPUT = "Listening for input from client ";
	private static final String DATABASE_ERROR = "Database error: unable to establish connection with ";
	private static final String GOT_PEOPLE_OBJECT = "Got People object from client ";
	private static final String CLOSING_CONNECTION = "Closing connection from client ";
	private static final String ERROR_WHILE_LISTENING = "Error while listening in server ";
	private static final String EXCEPTION_IN_HANDLE_THREAD = "Exception in handle client thread ";
	private static final String ERROR_WHILE_INSERTING = "Error while inserting data to Server DB";
	private static final String STARTING_THREAD = "Starting thread for client ";
	private static final String AT = " at ";
	private static final String CLIENT = "Client ";
	private static final String HOST_NAME = "'s host name is ";
	private static final String IP_ADDRESS = "'s IP Address is ";
	private static final String SUCCESS = "Success";
	private static final String FAILED = "Failed";
	private static final String INSERTED_SUCCESSFULLY = "Inserted successfully";

	// For querying
	private static final String QUERY_SERVER_DATA = "select * from people";
	private static final String INSERT_SERVER_DATA = "INSERT INTO People (Last, First, age, city,sent) VALUES (?, ?, ?, ?,?)";

	private Connection con;
	public static final int DEFAULT_PORT = 8001;
	private static int clientNo = 0;

	// The below two value will be set by the constructor
	int portNo;
	String dbName;

	// The Constructors
	public Server() throws IOException, SQLException {
		this(DEFAULT_PORT, SERVER_DB_NAME);
	}

	public Server(String dbFile) throws IOException, SQLException {
		this(DEFAULT_PORT, dbFile);
	}

	public Server(int port, String dbFile) throws IOException, SQLException {
		this.portNo = port;
		this.dbName = dbFile;
		this.setSize(Server.FRAME_WIDTH, Server.FRAME_HEIGHT);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		createMenus();
		createDblabelAndQueryDbButton();
		createTextAreaForServerQuery();
		connectToServerDb();
		Thread t = new Thread(this);
		t.start();
	}

	// Creating the Jmenu for server
	private void createMenus() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem menuItemExit = new JMenuItem("Exit");
		menu.add(menuItemExit);
		menuItemExit.addActionListener(event -> System.exit(0));
		menuBar.add(menu);
		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new GridLayout(1, 1));
		menuPanel.add(menuBar);
		this.setJMenuBar(menuBar);
	}

	// Creating DB label and query Db button
	private void createDblabelAndQueryDbButton() {
		JPanel dbLabelPanel = new JPanel();
		JLabel dbLabel = new JLabel("DB: " + dbName);
		dbLabelPanel.add(dbLabel);
		dbLabelPanel.setVisible(true);

		JPanel dBButtonPanel = new JPanel();
		JButton button = new JButton("Query DB");
		dBButtonPanel.add(button);
		button.addActionListener(e -> queryFromData());
		JPanel dblabelAndQueryDbButtonPanel = new JPanel();
		dblabelAndQueryDbButtonPanel.add(dbLabelPanel);
		dblabelAndQueryDbButtonPanel.add(dBButtonPanel);
		dblabelAndQueryDbButtonPanel.setLayout(new GridLayout(2, 1));
		this.add(dblabelAndQueryDbButtonPanel, BorderLayout.NORTH);
	}

	// Creating the text area
	private void createTextAreaForServerQuery() {
		textQueryArea = new JTextArea(AREA_ROWS, AREA_COLUMNS);
		textQueryArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textQueryArea);
		JPanel textPanel = new JPanel();
		textPanel.add(scrollPane);
		this.add(textPanel, BorderLayout.CENTER);
	}

	// This is used to get connection to db
	private void connectToServerDb() {
		try {
			con = DriverManager.getConnection("jdbc:sqlite:" + dbName);
		} catch (Exception e) {
			System.out.println(DATABASE_ERROR + dbName);
		}
	}

	/*
	 * This function queries all the data in server database and prints it in the
	 * text area
	 */
	private void queryFromData() {
		try {
			PreparedStatement stmt = con.prepareStatement(QUERY_SERVER_DATA);
			ResultSet rset = stmt.executeQuery();
			ResultSetMetaData rsmd = rset.getMetaData();
			int numColumns = rsmd.getColumnCount();
			StringBuilder build = new StringBuilder(textQueryArea.getText());
			if (build.length() > 0) {
				build.append("\n");
			}
			build.append(DB_VALUE);
			build.append("\n");
			build.append(DB_HEADER);
			build.append("\n");
			for (int i = 1; i <= numColumns; i++) {
				build.append(rsmd.getColumnName(i));
				if (i < numColumns) {
					build.append("\t");
				}
			}
			build.append("\n");
			build.append(DB_HEADER);
			build.append("\n");
			while (rset.next()) {
				String rowString = "";
				for (int i = 1; i <= numColumns; i++) {
					Object o = rset.getObject(i);
					// There would be cases when sent attribute is zero
					if (null == o) {
						o = "";
					}
					rowString += o.toString() + "\t";
				}
				rowString = rowString.trim();
				build.append(rowString);
				build.append("\n");
			}
			textQueryArea.setText(build.toString().trim());
		} catch (Exception e) {
			System.out.println(ERROR_QUERYING_FROM_DB + e);
		}
	}

	public static void main(String[] args) {
		Server sv;
		try {
			sv = new Server(SERVER_DB_NAME);
			sv.setVisible(true);
		} catch (IOException | SQLException e) {
			System.out.println(ERROR_STARTING_SERVER + e);
		}
	}

	// The below creates the server socket and listens for connections
	@Override
	public void run() {
		try (ServerSocket serverSocket = new ServerSocket(this.portNo)) {
			try {
				// Create a server socket
				textQueryArea.append(SERVER_STARTED_AT + new Date() + '\n');
				textQueryArea.append(LISTENING_ON_PORT + this.portNo);
				while (true) {
					Socket socket = serverSocket.accept();
					clientNo++;

					textQueryArea.append("\n");
					textQueryArea.append(STARTING_THREAD + clientNo + AT + new Date() + '\n');
					InetAddress inetAddress = socket.getInetAddress();
					textQueryArea.append(CLIENT + clientNo + HOST_NAME + inetAddress.getHostName() + "\n");
					textQueryArea.append(CLIENT + clientNo + IP_ADDRESS + inetAddress.getHostAddress() + "\n");
					new Thread(new HandleAClient(socket, clientNo)).start();
				}
			} catch (IOException ex) {
				System.out.println(ex);
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

// This class defines the thread class for handling new connection
	class HandleAClient implements Runnable {
		private Socket socket;
		private int clientNum;
		ObjectInputStream inputFromClient;
		ObjectOutputStream outputToClient;

		public HandleAClient(Socket socket, int clientNum) {
			this.socket = socket;
			this.clientNum = clientNum;
		}

		public void run() {
			try {
				textQueryArea.append(LISTENING_FOR_INPUT + this.clientNum);
				while (true) {
					try {
						/*
						 * I was having delays with buffered reader so using Object streams in both
						 * client and server
						 */
						inputFromClient = new ObjectInputStream(socket.getInputStream());
						outputToClient = new ObjectOutputStream(socket.getOutputStream());

						Object object = inputFromClient.readObject();
						People people = (People) object;
						System.out.println(people.toString());
						textQueryArea.append("\n");
						textQueryArea.append(GOT_PEOPLE_OBJECT + this.clientNum + " " + people.toString());
						boolean status = insertPeopleObjectToServer(people);
						System.out.println(status);

						if (status) {
							outputToClient.writeObject(SUCCESS);
							outputToClient.flush();
							textQueryArea.append("\n");
							textQueryArea.append(INSERTED_SUCCESSFULLY);
						} else {
							outputToClient.writeObject(FAILED);
							outputToClient.flush();
						}
					} catch (Exception e) {
						textQueryArea.append("\n");
						textQueryArea.append(CLOSING_CONNECTION + this.clientNum);
						System.out.println(ERROR_WHILE_LISTENING + e);
						socket.close();
						break;
					}
				}
			} catch (Exception e) {
				System.out.println(EXCEPTION_IN_HANDLE_THREAD + e);
			}
		}

		/*
		 * This function inserts into server db and returns true if successfully
		 * inserted, false otherwise.
		 */
		private boolean insertPeopleObjectToServer(People people) {
			PreparedStatement stmt;
			try {
				stmt = con.prepareStatement(INSERT_SERVER_DATA);
				stmt.setString(1, people.getLast());
				stmt.setString(2, people.getFirst());
				stmt.setInt(3, people.getAge());
				stmt.setString(4, people.getCity());
				stmt.setBoolean(5, false);
				stmt.execute();
				return true;
			} catch (SQLException e) {
				System.out.println(ERROR_WHILE_INSERTING);
				return false;
			}
		}
	}
}
