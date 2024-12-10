import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * @author brandonesarey Provides an int value that the Client class can
 *         interact with.
 */
public class Server {
	private static final Semaphore SEMAPHORE = new Semaphore(1);
	private static final BlockingQueue<ClientHandler> CLIENTQUEUE = new LinkedBlockingQueue<>();
	private static int remainingTickets = 30;

	/**
	 * @return the clientqueue
	 */
	public static BlockingQueue<ClientHandler> getClientqueue() {
		return CLIENTQUEUE;
	}// getClientQueue()

	public static void main(String[] args) throws Exception {
		try (ServerSocket server = new ServerSocket(1224)) {
			System.out.println("Server started on port 1224");

			while (true) {
				Socket client = server.accept();
				System.out.println("New client connected: " + client.getInetAddress().getHostAddress());

				ClientHandler clientSock = new ClientHandler(client);
				new Thread(clientSock).start();
			} // while
		} // try
	} // main()

	/**
	 * @author brandonesarey Regulates interaction between the client and the server
	 *         classes.
	 */
	private static class ClientHandler extends Thread {

		private final Socket clientSocket;

		public ClientHandler(Socket socket) {
			this.clientSocket = socket;
		}// ClientHandler

		@Override
		public void run() {
			try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
				SEMAPHORE.acquire();
				if (remainingTickets <= 0) {
					out.println(
							"Sorry. All tickets are sold out for this showing. Please type \"exit\" to exit program.");
					SEMAPHORE.release();
					return;
				} // if
				out.println("How many tickets would you like to purchase to see Dune(not Doom)? " + remainingTickets
						+ " tickets left. (\"exit\" to quit)");
				String clientInput;
				while ((clientInput = in.readLine()) != null) {
					try {
						if ("exit".equalsIgnoreCase(clientInput)) {
							out.println("Goodbye!");
							SEMAPHORE.release();
							break;
						} // if
						int ticketsRequested = Integer.parseInt(clientInput);
						if (ticketsRequested <= 0 || ticketsRequested > remainingTickets) {
							out.println("Invalid request or insufficient tickets. Please try again.");
							continue;
						} // if
						try {
							remainingTickets = remainingTickets - ticketsRequested;
							out.println("Purchase complete!");
							out.println(remainingTickets + " tickets are left.");
							out.println("Goodbye!");
							break;
						} finally {
							SEMAPHORE.release();
						} // finally
					} catch (NumberFormatException e) {
						out.println("Invalid input. Please try again.");
					} // try/catch #1
				} // while
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			} finally {
				try {
					clientSocket.close();
				} catch (IOException e) {// try/catch #2
					e.printStackTrace();
				} // try/catch #3
			} // finally #2
		}// run()
	}// ClientHandler()
}// Server