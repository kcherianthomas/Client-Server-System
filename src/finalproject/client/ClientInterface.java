package finalproject.client;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import finalproject.entities.People;

public class ClientInterface extends JFrame {

	private static final long serialVersionUID = 1L;

	public static final int DEFAULT_PORT = 8001;

	// For building UI
	private static final int FRAME_WIDTH = 600;
	private static final int FRAME_HEIGHT = 500;
	private static final int AREA_ROWS = 16;
	private static final int AREA_COLUMNS = 48;
	JComboBox peopleSelect;
	JFileChooser jFileChooser;
	JLabel dbLabel;
	JLabel connectionLabel;
	JTextArea textQueryArea;

	// The below are the constants used in program
	private static final String INACTIVE = "<None>";
	private static final String ACTIVE_DB = "Active DB: ";
	private static final String NO_ACTIVE_DB = "No active DB";
	private static final String DATABASE_ERROR = "Database error: unable to establish connection with ";
	private static final String CONNECTION = "Connection: ";
	private static final String CONNECTION_CLOSED = "Connection closed";
	private static final String NO_DATA_TO_SEND = "No data to send to Server";
	private static final String EMPTY = "<Empty>";
	private static final String SUCCESS = "Success";
	private static final String OPEN_A_CONNECTION = "Please open a connection";
	private static final String SOCKET_CLOSED = "Socket closed";
	private static final String ERROR_IN_RESPONSE = "Error while getting response from Server";
	private static final String ERROR_IN_QUERY_BY_ID = "Unable to query people object by id";
	private static final String ERROR_IN_UPDATING_CLIENT = "Exception while updating Client database";
	private static final String ERROR_IN_QUERYING = "Error while querying from client db: ";
	private static final String DB_HEADER = "***********************************************************************************";
	private static final String DB_VALUE = "DB Value:";
	private static final String CLIENT_ALREADY_CONNECTED = "Client is already connected";
	private static final String LOCALHOST = "localhost";
	private static final String CONNECTED = "Connected";
	private static final String CONNECTION_FAILTURE = "Connection Failure";
	private static final String CONNECTION_ALREADY_CLOSED = "Connection is already closed";

	// The queries required
	private static final String QUERY_FOR_COMBOBOX = "Select * from People where sent = 0";
	private static final String QUERY_CLIENT_DATA = "select * from people";
	private static final String QUERY_PEOPLE_DATA_BY_ID = "select * from people where id = ?";
	private static final String UPDATE_SENT_BY_ID = "update people set sent=1 where id = ?";

	String fileSeparator = System.getProperty("file.separator");

	ObjectOutputStream toServer;
	ObjectInputStream fromServer;
	Connection con;
	Socket socket;
	int port;

	// Constructors
	public ClientInterface() {
		this(DEFAULT_PORT);
	}

	public ClientInterface(int port) {
		this.port = port;
		this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		createFileMenu();
		jFileChooser = new JFileChooser(".");
		createDblabelConnectionLabelComboBox();
		createButtonAndTextArea();
	}

	// Crating the jFile for client
	private void createFileMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem menuItemOpen = new JMenuItem("Open DB");
		menu.add(menuItemOpen);
		JMenuItem menuItemExit = new JMenuItem("Exit");
		menu.add(menuItemExit);
		menuItemOpen.addActionListener(e -> openDB());
		menuItemExit.addActionListener(event -> System.exit(0));
		menuBar.add(menu);
		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new GridLayout(1, 1));
		menuPanel.add(menuBar);
		this.setJMenuBar(menuBar);
	}

	// Crating the labels and Combobox for client
	private void createDblabelConnectionLabelComboBox() {
		JPanel dbLabelPanel = new JPanel();
		dbLabel = new JLabel(ACTIVE_DB + INACTIVE);
		dbLabelPanel.add(dbLabel);
		dbLabelPanel.setVisible(true);

		JPanel connectionLabelPanel = new JPanel();
		connectionLabel = new JLabel(CONNECTION + INACTIVE);
		connectionLabelPanel.add(connectionLabel);
		connectionLabelPanel.setVisible(true);

		JPanel jComboBoxPanel = new JPanel();
		// This is just to show initially its empty
		String[] comboValues = { EMPTY };
		peopleSelect = new JComboBox(comboValues);
		jComboBoxPanel.add(peopleSelect);
		jComboBoxPanel.setVisible(true);

		JPanel dblabelAndConnectionLabelComboBoxPanel = new JPanel();
		dblabelAndConnectionLabelComboBoxPanel.add(dbLabelPanel);
		dblabelAndConnectionLabelComboBoxPanel.add(connectionLabelPanel);
		dblabelAndConnectionLabelComboBoxPanel.add(jComboBoxPanel);
		dblabelAndConnectionLabelComboBoxPanel.setLayout(new GridLayout(3, 1));
		this.add(dblabelAndConnectionLabelComboBoxPanel, BorderLayout.NORTH);
	}

	// Creating the buttons and text area
	private void createButtonAndTextArea() {
		JPanel openConnectionAndCloseConnectionButtonPanel = new JPanel();
		JButton openConnectionButton = new JButton("Open Connection");
		openConnectionButton.addActionListener(new OpenConnectionListener());
		openConnectionAndCloseConnectionButtonPanel.add(openConnectionButton);
		JButton closeConnectionButton = new JButton("Close Connection");
		closeConnectionButton.addActionListener(new CloseConnectionListener());
		openConnectionAndCloseConnectionButtonPanel.add(closeConnectionButton);

		JPanel sendDataAndQueryDbDataButtonPanel = new JPanel();
		JButton sendDataButton = new JButton("Send Data");
		sendDataButton.addActionListener(e -> verifyAndSendToServer());
		sendDataAndQueryDbDataButtonPanel.add(sendDataButton);
		JButton queryDbDataButton = new JButton("Query Db Data");
		queryDbDataButton.addActionListener(e -> queryFromDataBase());
		sendDataAndQueryDbDataButtonPanel.add(queryDbDataButton);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(2, 2));
		buttonPanel.add(openConnectionAndCloseConnectionButtonPanel);
		buttonPanel.add(sendDataAndQueryDbDataButtonPanel);

		textQueryArea = new JTextArea(AREA_ROWS, AREA_COLUMNS);
		textQueryArea.setEditable(false);

		JScrollPane scrollPane = new JScrollPane(textQueryArea);
		JPanel textPanel = new JPanel();
		textPanel.add(scrollPane);

		JPanel buttonsAndTextArea = new JPanel();
		buttonsAndTextArea.setLayout(new BorderLayout());
		buttonsAndTextArea.add(buttonPanel, BorderLayout.NORTH);
		buttonsAndTextArea.add(textPanel, BorderLayout.CENTER);
		this.add(buttonsAndTextArea, BorderLayout.CENTER);
	}

	// This function we be used to set the values for combobox
	private void fillComboBox() throws SQLException {
		List<ComboBoxItem> l = getNames();
		peopleSelect.setModel(new DefaultComboBoxModel(l.toArray()));
		if (l.isEmpty()) {
			ComboBoxItem temp = new ComboBoxItem(-1, EMPTY);
			l.add(temp);
			peopleSelect.setModel(new DefaultComboBoxModel(l.toArray()));
		}
	}

	// This function gets all values for the combobox
	private List<ComboBoxItem> getNames() throws SQLException {
		PreparedStatement stmt = con.prepareStatement(QUERY_FOR_COMBOBOX);
		ResultSet rset = stmt.executeQuery();
		List<ComboBoxItem> comboBoxList = new ArrayList<>();
		while (rset.next()) {
			ComboBoxItem comboBoxItem = new ComboBoxItem(rset.getInt(6), rset.getString(1));
			comboBoxList.add(comboBoxItem);
		}
		return comboBoxList;
	}

	// This function is used to select db file
	private void openDB() {
		int returnVal = jFileChooser.showOpenDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String dbFileName = jFileChooser.getSelectedFile().getAbsolutePath();
			dbFileName = dbFileName.substring(dbFileName.lastIndexOf(fileSeparator) + 1);
			try {
				con = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
				dbLabel.setText(ACTIVE_DB + dbFileName);
				fillComboBox();
			} catch (Exception e) {
				System.out.println(DATABASE_ERROR + dbFileName);
				con = null;
				dbLabel.setText(ACTIVE_DB + INACTIVE);
			}

		}

	}

	/*
	 * This function checks if connection and db is active and if active calls
	 * sendToServer function
	 */
	private void verifyAndSendToServer() {
		if (connectionLabel.getText().equals(CONNECTION + INACTIVE)) {
			if (textQueryArea.getText().length() > 0) {
				textQueryArea.append("\n");
			}
			textQueryArea.append(OPEN_A_CONNECTION);
		} else if (dbLabel.getText().equals(ACTIVE_DB + INACTIVE)) {
			if (textQueryArea.getText().length() > 0) {
				textQueryArea.append("\n");
			}
			textQueryArea.append(NO_ACTIVE_DB);
		} else {
			sendToServer();
		}
	}

	/*
	 * This function will send people data to server and gets the response from
	 * server and if the people data was successfully updated in server database the
	 * function will update client database with the sent value for the people data
	 * to 1 and modifies the combo box.
	 */
	private void sendToServer() {
		try {
			// I am using object streams as i was having issues with buffered streams
			toServer = new ObjectOutputStream(socket.getOutputStream());
			fromServer = new ObjectInputStream(socket.getInputStream());
		} catch (IOException ex) {
			if (textQueryArea.getText().length() > 0) {
				textQueryArea.append("\n");
			}
			textQueryArea.append(ex.toString());
			textQueryArea.append("\n" + CONNECTION_CLOSED);
			connectionLabel.setText(CONNECTION + INACTIVE);
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println(SOCKET_CLOSED);
			}
		}

		try {
			ComboBoxItem personEntry = (ComboBoxItem) peopleSelect.getSelectedItem();
			People peopleObjectForServer = getPeopleObjectToSendToServer(personEntry.getId());
			if (peopleObjectForServer == null) {
				if (textQueryArea.getText().length() > 0) {
					textQueryArea.append("\n");
				}
				textQueryArea.append(NO_DATA_TO_SEND);
			} else {
				toServer.writeObject(peopleObjectForServer);
				String response = (String) fromServer.readObject();
				if (response.contains(SUCCESS)) {
					updateClientDbAndComboBox(peopleObjectForServer);
				} else {
					socket.close();
					if (textQueryArea.getText().length() > 0) {
						textQueryArea.append("\n");
					}
					textQueryArea.append(ERROR_IN_RESPONSE);
					textQueryArea.append("\n");
					textQueryArea.append(CONNECTION_CLOSED);
					connectionLabel.setText(CONNECTION + INACTIVE);
				}
			}
		} catch (Exception excep) {
			System.out.println(excep);
		}
	}

	/*
	 * This function gets the people object to be sent to server
	 */
	private People getPeopleObjectToSendToServer(int id) {
		try {
			PreparedStatement stmt = con.prepareStatement(QUERY_PEOPLE_DATA_BY_ID);
			stmt.setInt(1, id);
			ResultSet rset = stmt.executeQuery();
			List<People> peopleList = new ArrayList<>();
			while (rset.next()) {
				People people = new People();
				people.setLast(rset.getString(1));
				people.setFirst(rset.getString(2));
				people.setAge(rset.getInt(3));
				people.setCity(rset.getString(4));
				people.setId(rset.getString(6));
				peopleList.add(people);
			}
			return peopleList.get(0);
		} catch (Exception e) {
			System.out.println(ERROR_IN_QUERY_BY_ID);
		}
		return null;
	}

	/*
	 * Update the sent value to 1 for the people object that was successfully
	 * inserted in server db and calls function to update combobox
	 */
	private void updateClientDbAndComboBox(People people) {
		try {
			PreparedStatement stmt = con.prepareStatement(UPDATE_SENT_BY_ID);
			stmt.setString(1, people.getId());
			stmt.executeUpdate();
			fillComboBox();
		} catch (Exception e) {
			System.out.println(ERROR_IN_UPDATING_CLIENT + e);
		}
	}

	class ComboBoxItem {
		private int id;
		private String name;

		public ComboBoxItem(int id, String name) {
			this.id = id;
			this.name = name;
		}

		public int getId() {
			return this.id;
		}

		public String getName() {
			return this.name;
		}

		public String toString() {
			return this.name;
		}

	}

	/*
	 * This function prints values in client database in the text area
	 */
	private void queryFromDataBase() {
		try {
			if (dbLabel.getText().equals(ACTIVE_DB + INACTIVE)) {
				if (textQueryArea.getText().length() > 0) {
					textQueryArea.append("\n");
				}
				textQueryArea.append(NO_ACTIVE_DB);
				return;
			}
			PreparedStatement stmt = con.prepareStatement(QUERY_CLIENT_DATA);
			ResultSet rset = stmt.executeQuery();
			ResultSetMetaData rsmd = rset.getMetaData();
			int numColumns = rsmd.getColumnCount();
			StringBuilder build = new StringBuilder(textQueryArea.getText());
			if (build.length() > 0) {
				build.append("\n");
			}
			build.append(DB_VALUE + "\n");
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
			System.out.println(ERROR_IN_QUERYING + e);
		}
	}

	/*
	 * This opens connection with the server
	 */
	class OpenConnectionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				if (!connectionLabel.getText().equals(CONNECTION + INACTIVE)) {
					if (textQueryArea.getText().length() > 0) {
						textQueryArea.append("\n");
					}
					textQueryArea.append(CLIENT_ALREADY_CONNECTED);
					return;
				}
				socket = new Socket(LOCALHOST, port);
				if (textQueryArea.getText().length() > 0) {
					textQueryArea.append("\n");
				}
				textQueryArea.append(CONNECTED);
				connectionLabel.setText(LOCALHOST + ":" + port);
			} catch (IOException e1) {
				if (textQueryArea.getText().length() > 0) {
					textQueryArea.append("\n");
				}
				textQueryArea.append(CONNECTION_FAILTURE);
			}
		}
	}

	/*
	 * This closes connection with the server
	 */
	class CloseConnectionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				if (connectionLabel.getText().equals(CONNECTION + INACTIVE)) {
					if (textQueryArea.getText().length() > 0) {
						textQueryArea.append("\n");
					}
					textQueryArea.append(CONNECTION_ALREADY_CLOSED);
					return;
				}
				socket.close();
				if (textQueryArea.getText().length() > 0) {
					textQueryArea.append("\n");
				}
				textQueryArea.append(CONNECTION_CLOSED);
				connectionLabel.setText(CONNECTION + INACTIVE);
			} catch (Exception e1) {
				System.out.println(e);
			}
		}
	}

	public static void main(String[] args) {
		ClientInterface ci = new ClientInterface();
		ci.setVisible(true);
	}
}
