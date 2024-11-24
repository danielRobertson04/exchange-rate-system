import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final ServerLogic serverLogic;
    private final RmiMethodsInterface rmiServer;

    public ClientHandler(Socket clientSocket, ServerLogic serverLogic, RmiMethodsInterface rmiServer) {
        this.clientSocket = clientSocket;
        this.serverLogic = serverLogic;
        this.rmiServer = rmiServer;
    }

    @Override
    public void run() {
        try {
            // Notify the server about the new connection
            System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());

            // Launch the Login GUI
            LoginGUI loginGUI = new LoginGUI(rmiServer);
            loginGUI.setVisible(true);

            // Wait for the session to start and keep active
            while (true) {
                if (!loginGUI.isSessionActive()) {
                    System.out.println("Client disconnected: " + clientSocket.getRemoteSocketAddress());
                    break;
                }
                // Monitor for backend updates or handle additional server-side tasks if needed
                handleBackendUpdates();
            }

        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Connection closed for client: " + clientSocket.getRemoteSocketAddress());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method can be used to handle backend-triggered updates.
     * For example, notifying the GUI about balance changes or exchange rate updates.
     */
    private void handleBackendUpdates() {
        // Example: Future logic for server-to-GUI notifications
        // This could be implemented using listeners or periodic checks
    }
}
