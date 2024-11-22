import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.ServerLogic;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ServerLogicTest {
    private ServerLogic serverLogic;

    @BeforeEach
    void setUp() {
        serverLogic = new ServerLogic();
    }

    @Test
    void testCreateAccount() {
        String username = "testUser";
        String password = "testPass";

        boolean created = serverLogic.createAccount(username, password);
        assertTrue(created, "Account should be created successfully");

        boolean duplicateCreation = serverLogic.createAccount(username, password);
        assertFalse(duplicateCreation, "Duplicate account creation should not be allowed");
    }

    @Test
    void testVerifyAccount() throws IOException {
        String username = "verifyUser";
        String password = "verifyPass";

        boolean verified = serverLogic.verifyAccount(username, password);
        assertTrue(verified, "New account should be created and verified successfully");

        boolean incorrectPassword = serverLogic.verifyAccount(username, "wrongPass");
        assertFalse(incorrectPassword, "Verification should fail for incorrect password");

        boolean existingAccount = serverLogic.verifyAccount(username, password);
        assertTrue(existingAccount, "Existing account should be verified successfully");
    }

    @Test
    void testGetOutgoingRequests() {
        serverLogic.addTransferRequest("sender1", "recipient1", "USD", 100);
        serverLogic.addTransferRequest("sender2", "recipient2", "GBP", 200);

        List<String> outgoingRequests = serverLogic.getOutgoingRequests();
        assertEquals(2, outgoingRequests.size(), "There should be two outgoing requests");
        assertTrue(outgoingRequests.get(0).contains("USD"), "First request should involve USD");
    }

    @Test
    void testGetIncomingRequests() {
        serverLogic.addTransferRequest("sender1", "recipient1", "USD", 100);
        serverLogic.addTransferRequest("sender2", "recipient2", "GBP", 200);

        List<String> incomingRequests = serverLogic.getIncomingRequests("recipient1");
        assertEquals(1, incomingRequests.size(), "There should be one incoming request for recipient1");
        assertTrue(incomingRequests.get(0).contains("USD"), "Request should involve USD");
    }

    @Test
    void testUpdateExchangeRates() {
        serverLogic.updateExchangeRates();
        Map<String, Double> exchangeRates = serverLogic.getExchangeRates();

        assertNotNull(exchangeRates, "Exchange rates should not be null");
        assertTrue(exchangeRates.containsKey("GBP-USD"), "Exchange rates should include GBP-USD");
        assertTrue(exchangeRates.containsKey("USD-GBP"), "Exchange rates should include USD-GBP");
    }

    @Test
    void testTransferWithinAccount() throws IOException {
        String username = "testUser";
        String password = "testPass";

        serverLogic.createAccount(username, password);
        serverLogic.updateAccountBalance(username, "USD", 100.0);

        boolean success = serverLogic.transferWithinAccount(username, "USD", "GBP", 50);
        assertTrue(success, "Transfer within account should succeed");

        List<String> userInfo = serverLogic.getAllUserInfo(username);
        assertNotNull(userInfo, "User info should not be null");
        assertTrue(userInfo.get(0).contains("GBP"), "Account should now include GBP balance");
    }



    @Test
    void testConcurrency() throws InterruptedException, IOException {
        String username = "concurrentUser";
        String password = "concurrentPass";

        serverLogic.createAccount(username, password);
        serverLogic.updateAccountBalance(username, "USD", 100.0);

        Runnable task = () -> {
            try {
                serverLogic.transferWithinAccount(username, "USD", "GBP", 10);
            } catch (Exception e) {
                fail("Concurrency task failed");
            }
        };

        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        List<String> userInfo = serverLogic.getAllUserInfo(username);
        assertTrue(userInfo.get(0).contains("GBP"), "User should have GBP balance after concurrent transfers");
    }
}
