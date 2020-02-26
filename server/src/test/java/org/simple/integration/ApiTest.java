package org.simple.integration;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.*;
import org.simple.utils.RunServer;
import org.simple.utils.TestGroupIntegration;
import org.simple.DataBaseUtils;
import org.springframework.boot.web.server.LocalServerPort;

import java.io.IOException;
import java.sql.*;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.AssertionErrors.*;

@RunServer
@TestGroupIntegration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiTest {

    private static int testId;

    @LocalServerPort
    private int randomServerPort;

    private CloseableHttpClient httpClient = HttpClients.createDefault();

    private String getResponse(String uri) throws IOException {
        String result;

        HttpGet request = new HttpGet(String.format("http://localhost:%d%s", randomServerPort, uri));
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            HttpEntity entity = response.getEntity();
            assertNotNull("response is null", entity);
            result = EntityUtils.toString(entity);
        }

        return result;
    }

    @Test
    @Order(1)
    public void helloTest() throws IOException {
        assertEquals("hello", getResponse("/api/hello"));
    }

    @Test
    @Order(2)
    public void listTest() throws IOException {
        assertEquals("[{\"id\":1,\"text\":\"go to settings\"},{\"id\":2,\"text\":\"go to online store\"}," +
                "{\"id\":3,\"text\":\"delete unwanted items from your shopping cart\"}," +
                "{\"id\":4,\"text\":\"change your home address\"}," +
                "{\"id\":5,\"text\":\"save changes\"}," +
                "{\"id\":6,\"text\":\"buy some goods\"}," +
                "{\"id\":7,\"text\":\"go to shopping cart\"}," +
                "{\"id\":8,\"text\":\"check your shopping cart\"}," +
                "{\"id\":9,\"text\":\"finish shopping\"}]", getResponse("/api/list-obj"));
    }

    @Test
    @Order(3)
    public void addToDBTest() throws SQLException, IOException {
        String TEST_VALUE = "Test_Add_To_Db_Value";

        testId = Integer.parseInt(getResponse(String.format("/api/add?text=%s", TEST_VALUE)));

        Connection conn = DataBaseUtils.getConnect();
        PreparedStatement stmt = conn.prepareStatement("SELECT id, text FROM TODO_LIST WHERE id = ?");
        stmt.setInt(1, testId);
        ResultSet rset = stmt.executeQuery();

        assertTrue(String.format("There is no record with id = %d in SQL database", testId), rset.next());
        assertEquals(TEST_VALUE, rset.getString(2));
        assertFalse(String.format("There is more then one record with id = %d in SQL database", testId), rset.next());
    }

    @Test
    @Order(4)
    public void editTest() throws IOException, SQLException {
        String NEW_VALUE = "do_not_look_at_it";

        getResponse(String.format("/api/edit?id=%d&text=%s", testId, NEW_VALUE));

        Connection conn = DataBaseUtils.getConnect();
        PreparedStatement stmt = conn.prepareStatement("SELECT id, text FROM TODO_LIST WHERE id = ?");
        stmt.setInt(1, testId);
        ResultSet rset = stmt.executeQuery();

        assertTrue(String.format("There is no records with id = %d in SQL database", testId), rset.next());
        assertEquals(NEW_VALUE, rset.getString(2));
        assertFalse(String.format("There is more then one record with id = %d in SQL database", testId), rset.next());
    }

    @Test
    @Order(5)
    public void deleteTest() throws IOException, SQLException {
        getResponse(String.format("/api/delete?id=%d", testId));

        Connection conn = DataBaseUtils.getConnect();
        PreparedStatement stmt = conn.prepareStatement("SELECT id FROM TODO_LIST WHERE id = ?");
        stmt.setInt(1, testId);
        ResultSet rset = stmt.executeQuery();

        assertFalse(String.format("There is no record with id = %d in SQL database", testId), rset.next());
    }
}