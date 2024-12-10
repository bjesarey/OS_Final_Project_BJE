import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author brandonesarey Interacts with an int value on a server.
 */
public class Client {
	public static void main(String[] args) {
		try (Socket socket = new Socket("localhost", 1224)) {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			System.out.println(in.readLine());
			Scanner sc = new Scanner(System.in);
			String serverResponse;
			while (true) {
				String userInput = sc.nextLine();
				out.println(userInput);
				if ("exit".equalsIgnoreCase(userInput)) {
					System.out.println("Exiting program...");
					sc.close();
					break;
				} // if
				while ((serverResponse = in.readLine()) != null) {
					System.out.println(serverResponse);
					if (serverResponse.contains("Goodbye")) {
						System.out.println("Disconnecting...");
						sc.close();
						return;
					} // if
					if (serverResponse.contains("Please try again")) {
						break;
					} // if
				} // while #2
			} // while #1
		} catch (IOException e) {
			e.printStackTrace();
		} // try/catch
	}// Main
}// Client()