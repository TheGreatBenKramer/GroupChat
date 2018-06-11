/**
 * GroupChat Client Edition
 * Written by Ben Kramer
 * This code allows users to connect to a GroupChat Server to send and receive
 * messages
 * 
 * Last Edited 6/10/2018
 */

package groupchatclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class GroupChatClient
{
    BufferedReader in; //Reads messages from the Server
    PrintWriter out; //Writes messages to the Server
    JFrame frame = new JFrame("Group Chat Client"); //Client's Program Frame with Title "Group Chat Client"
    JTextField textField = new JTextField(40); //Used for Clients to write messages
    JTextArea messageArea = new JTextArea(8, 40); //Displays messages to the Client
    int port = 53274; //Port number of the server being connected to
    String serverAddress = "107.13.156.111";  //Address to connect to
    String name; //Client Display Name
    int panelSizeX; //X dimension of the Client display
    int panelSizeY; //Y dimension of the Client display
    
    public GroupChatClient()
    {   
        //Preparing the Program Window 
        textField.setEditable(false); //Prevents the user from attempting to send messages
        messageArea.setEditable(false); //Prevents the user from altering the contents of the chat history
        frame.getContentPane().add(textField, "South"); //Place the textfield along the bottom of the frame
        //Create a scroll pane using the message area and center it in the frame
        frame.getContentPane().add(new JScrollPane(messageArea), "Center"); 
        frame.pack(); //confirm the layout of the frame
        textField.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e)
            {
                out.println(textField.getText()); //submits the message in the field to the server
                textField.setText(""); //resets the field to blank
            }
        }); //handles the sending of messages to the Server
    }
    
    /**
     * getServerAddress
     * Prompts the user to enter the address of their desired server
     * Returns a string containing the desired IP address
     * 
     */
    private String getServerAddress() 
    {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Group Chat Client",
            JOptionPane.QUESTION_MESSAGE);
    }
    private int getPort()
    {   
        return Integer.parseInt(JOptionPane.showInputDialog(
            frame,
                "Enter Port to Connect to:",
                "Welcome to the Group Chat Client",
                JOptionPane.QUESTION_MESSAGE));   
    }
    private String getName() 
    {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE);
    }
    private void run() throws IOException {
        //Prompt the user to autoconnect or enter their own network settings
        int autoConnectPrompt = JOptionPane.showConfirmDialog(frame,"Would you like to connect using default settings?",
                                                              "AutoConnect",JOptionPane.YES_NO_OPTION);
        
        // Make connection and initialize streams
        if(autoConnectPrompt == JOptionPane.NO_OPTION)
        {
            serverAddress = getServerAddress();
            port = getPort();
        }
        Socket socket = new Socket(serverAddress, port);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Process all messages from server, according to the protocol.
        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
                out.println(name = getName());
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            }
        }
    }    
    public static void main(String[] args) throws Exception 
    {
        GroupChatClient client = new GroupChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}
