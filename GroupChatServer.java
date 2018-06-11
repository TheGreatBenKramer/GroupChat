package groupchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

public class GroupChatServer 
{
    private static final int PORT = 53274;
    private static final HashSet<String> names = new HashSet<>();
    private static final HashSet<PrintWriter> writers = new HashSet<>();
    private static final boolean open = true;
    static Scanner input = new Scanner(System.in);
    
    public static void main(String[] args) throws Exception 
    {
        System.out.println("The chat server at PORT " + PORT + " is running.");
        try 
        (ServerSocket listener = new ServerSocket(PORT)) {
            while(true)
            {
                while (open) 
                {
                    new Handler(listener.accept()).start();
                }
            }
        }
    }
    public static class Handler extends Thread 
    {
        private String name;
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        
        public Handler(Socket socket) 
        {
          this.socket = socket;
        }
        
        @Override
        public void run() {
            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.  Note that
                // checking for the existence of a name and adding the name
                // must be done while locking the set of names.
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            for(PrintWriter writer : writers)
                            {
                                writer.println("MESSAGE NOTICE: " + name + " has joined the lobby");
                            }
                            break;
                        }
                    }
                }
                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                out.println("NAMEACCEPTED");
                writers.add(out);
                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {
                    String input = in.readLine();
                    System.out.println(input);
                    if (input == null) {
                        return;
                    }
                    else if (input.startsWith(".")) 
                    {
                        String[] localOut = processCommand(input);
                        for (String localOut1 : localOut) {
                            out.println("MESSAGE " + localOut1);
                        }
                    }
                    else if(input.startsWith("NTC"))
                    {
                        for(PrintWriter writer : writers) {
                            writer.println("MESSAGE NOTICE: " + input.substring(4));
                        }
                    }
                    else
                    {
                    for (PrintWriter writer : writers) {
                            writer.println("MESSAGE " + name + ": " + input);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (name != null) {
                    for(PrintWriter writer : writers)
                    {
                        writer.println("MESSAGE NOTICE: " + name + " has left the lobby");
                    }
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    
                }
            }
        }
     }
     public static String[] processCommand(String input)
    {
        if(input.toUpperCase().startsWith(".WHO")) //lists all users
        {
            String[] result = new String[names.size()+1];
            result[0] = "Who:\n ";
            Iterator itr = names.iterator();
            int i = 1;
            while(itr.hasNext())
            {
                result[i] = itr.next() + "\n";
                i++;
            }
            return result;
        }
        else if(input.toUpperCase().startsWith(".HELP")) //lists all commands
        {
            String[] result = new String[3];
            result[0] = "Help:\n";
            result[1] = ".who - lists all users in the chat room\n";
            result[2] = ".help - lists all available commands\n";
            return result;
        }
        else
        {
            String [] result = new String[2];
            result[0] = "NTC: Command Not Recognized";
            result[1] = "Type \".help\" for a list of recognized commands";
            return result;
        }       
    }
} 