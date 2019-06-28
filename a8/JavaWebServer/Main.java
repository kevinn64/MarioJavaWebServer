import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.awt.Desktop;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;


class Main
{
    static int client = 0;
    static int id;
    static int marioPos = 50;
    static int bowserPos = 200;
    static int p1chat = 0;
    static int p2chat = 0;
    static String chat = "";
    static String present_message = "";
    static ArrayList<String> chat1 = new ArrayList<String>();
    static ArrayList<String> chat2 = new ArrayList<String>();        
        
    static String getServerTime()
    {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateFormat.format(calendar.getTime());
    }

    static void sendLine(PrintWriter out, String line)
    {
        out.print(line); // Send over the socket
        out.print("\r\n");
        System.out.println(line); // Print it to the console too, just to make debugging easier
    }

    static void onGet(OutputStream os, String url) throws Exception
    {
        PrintWriter out = new PrintWriter(os, true);
        String filename = url.substring(1); // cut off the initial "/"
        File f = new File(filename);
        Path path = Paths.get(filename);
        String dateString = getServerTime();
        System.out.println("~~~~~~~~~~ The server replied: ~~~~~~~~~~");
        if(f.exists() && !f.isDirectory())
        {
                // Read the file from disk
                byte[] fileContents = Files.readAllBytes(path);

                // Send the headers
                sendLine(out, "HTTP/1.1 200 OK");
                sendLine(out, "Content-Type: " + Files.probeContentType(path));
                sendLine(out, "Content-Length: " + Integer.toString(fileContents.length));
                sendLine(out, "Date: " + dateString);
                sendLine(out, "Last-Modified: " + dateString);
                sendLine(out, "Connection: close");
                sendLine(out, "");
                out.flush();

                // Send the payload
                os.write(fileContents);
                String blobHead = fileContents.length < 60 ? new String(fileContents) : new String(fileContents, 0, 60) + "...";
                System.out.println(blobHead);
        }
        else
        {
            // Make an error message
            String payload = "404 - File not found: " + filename;

            // Send HTTP headers
            sendLine(out, "HTTP/1.1 200 OK");
            sendLine(out, "Content-Type: text/html");
            sendLine(out, "Content-Length: " + Integer.toString(payload.length()));
            sendLine(out, "Date: " + dateString);
            sendLine(out, "Last-Modified: " + dateString);
            sendLine(out, "Connection: close");
            sendLine(out, "");

            // Send the payload
            sendLine(out, payload);
            }
    }

    static void onPost(OutputStream os, String url, char[] incomingPayload)
    {
        String response = "";
        
        // Parse the incoming payload
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        String payload = String.valueOf(incomingPayload);
                    
        Json ob = Json.parse(payload);
                    
        id = (int)ob.getLong("id");
                    
        if (id == 1)
        {
            marioPos = (int)ob.getLong("marioPos");
        //    System.out.println("Mario's Position: " + marioPos);
        }
        else if (id == 2)
        {
            bowserPos = (int)ob.getLong("bowserPos");
         //   System.out.println("Bowser's Position: " + bowserPos);
        }
                    
        if (id == -1)
        {
            client++;
            if(client < 3)
                id = client;
        }
        else if (id == 1)
            id = 1;
        else if (id == 2)
            id = 2;
                    
        present_message = ob.getString("message");
    //     System.out.println("message right now to be added: " + present_message);
        if(present_message != null && !present_message.isEmpty())
        {
            chat1.add(present_message);
    //     System.out.println("Size of first chat after add: " + chat1.size());
            chat2.add(present_message);
    //     System.out.println("Size of second chat after add: " + chat2.size());
                
            if(id == 1)
            {
    //     System.out.println("client id trying to get info :" + id);
    //     System.out.println("Size of chat2 before removal: " + chat2.size());
                chat = chat2.get(p2chat);
    //     System.out.println("1st element of chat2: " + chat2.get(chat2p));
                chat2.remove(p2chat);
    //     System.out.println("Size of chat2 after removal: " + chat2.size());
            }
            else if (id == 2)
            {
    //     System.out.println("Id of client trying to take info :" + id);
    //     System.out.println("Size of chat1 before removal: " + chat1.size());
                chat = chat1.get(p1chat);
    //     System.out.println("1st element of chat1: " + chat1.get(p1chat));
                chat1.remove(p1chat);
    //     System.out.println("Size of chat1 after removal: " + chat1.size());
            }
        }
        else 
            chat = "";
                
        if(present_message.length() == 0)
        {
    //     System.out.println("Null message, checking size of chat1 " + chat1.size());
    //     System.out.println("Null message, checking size of chat2 " + chat2.size());
            if(id == 1)
            {
                if(chat2.size() > 0)
                {
    //     System.out.println("Id of client trying to take info :" + id);
    //     System.out.println("Size of chat2 before removal: " + chat2.size());
                    chat = chat2.get(p2chat);
    //     System.out.println("1st element of chat2: " + chat2.get(chat2p));
                    chat2.remove(p2chat);
    //     System.out.println("Size of chat2 after removal: " + chat2.size());
                }
            }
            else if (id == 2)
            {
                if(chat1.size() > 0)
                {
    //     System.out.println("Id of client trying to take info :" + id);
    //     System.out.println("Size of chat1 before removal: " + chat1.size());
                    chat = chat1.get(p1chat);
    //     System.out.println("1st element of chat1: " + chat1.get(chat1p));
                    chat1.remove(p1chat);
    //     System.out.println("Size of chat1 after removal: " + chat1.size());
                }
            }
        }
        
      //  System.out.println("Payload: " + payload);

        
        // Make a response
        if (id == 1)
            response = "{\"message\":" + "\"" + chat + "\", \"client\":" + Integer.toString(id) + ", \"bowserPos\":" + Integer.toString(bowserPos) + "}";
        else if (id == 2)
            response = "{\"message\":" + "\"" + chat + "\", \"client\":" + Integer.toString(id) + ", \"marioPos\":" + Integer.toString(marioPos) + "}";
        else
            response = "{\"message\":" + "\"" + chat + "\", \"client\":" + Integer.toString(id) + ", \"marioPos\":" + Integer.toString(marioPos) + ", \"bowserPos\":" + Integer.toString(bowserPos) + "}";
            
        // Send HTTP headers
        System.out.println("~~~~~~~~~~ The server replied: ~~~~~~~~~~");
        String dateString = getServerTime();
        PrintWriter out = new PrintWriter(os, true);
        sendLine(out, "HTTP/1.1 200 OK");
        sendLine(out, "Content-Type: application/json");
        sendLine(out, "Content-Length: " + Integer.toString(response.length()));
        sendLine(out, "Date: " + dateString);
        sendLine(out, "Last-Modified: " + dateString);
        sendLine(out, "Connection: close");
        sendLine(out, "");
                    
        // Send the response
        sendLine(out, response);
        out.flush();
    }

    public static void main(String[] args) throws Exception
    {
        // Make a socket to listen for clients
        int port = 1234;
        String ServerURL = "http://localhost:" + Integer.toString(port) + "/page.html";
        ServerSocket serverSocket = new ServerSocket(port);

    // Start the web browser
    if(Desktop.isDesktopSupported())
    {
        Desktop.getDesktop().browse(new URI(ServerURL));
        Desktop.getDesktop().browse(new URI(ServerURL));
    }
    else
        System.out.println("Please direct your browser to " + ServerURL);

        // Handle requests from clients
        while(true)
        {
            Socket clientSocket = serverSocket.accept(); // This call blocks until a client connects
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream os = clientSocket.getOutputStream();

            // Read the HTTP headers
            String headerLine;
            int requestType = 0;
            int contentLength = 0;
            String url = "";
            System.out.println("~~~~~~~~~~ A client said: ~~~~~~~~~~");
            while ((headerLine = in.readLine()) != null)
            {
                System.out.println(headerLine);
                if(headerLine.length() > 3 && headerLine.substring(0, 4).equals("GET "))
                {
                    requestType = 1;
                    url = headerLine.substring(4, headerLine.indexOf(" ", 4));
                }
                else if(headerLine.length() > 4 && headerLine.substring(0, 5).equals("POST "))
                {
                    requestType = 2;
                    url = headerLine.substring(5, headerLine.indexOf(" ", 5));
                }
                else if(headerLine.length() > 15 && headerLine.substring(0, 16).equals("Content-Length: "))
                    contentLength = Integer.parseInt(headerLine.substring(16));
                if(headerLine.length() < 2) // Headers are terminated by a "\r\n" line
                    break;
            }

        // Send a response
        if(requestType == 1)
        {
            onGet(os, url);
        }
        else if(requestType == 2)
        {
        // Read the incoming payload
            char[] incomingPayload = new char[contentLength];
            in.read(incomingPayload, 0, contentLength);
            String blobHead = incomingPayload.length < 60 ? new String(incomingPayload) : new String(incomingPayload, 0, 60) + "...";
            System.out.println(blobHead);
            onPost(os, url, incomingPayload);
        }
        else
            System.out.println("Received bad headers. Ignoring.");

        // Hang up
        os.flush();
        clientSocket.close();
        }
    }
}
