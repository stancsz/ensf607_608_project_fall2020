package server.controller;

import java.io.BufferedReader;
import java.sql.*;
import java.util.Arrays;
//TODO :
//Query for Listing all tools
//Query for search by toolName
//Query for search by toolID
//Query for purchasing an item (need to include customer info ???)
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * String path = "localhost"
 * String fileName = "mysql.db"
 * String username = "admin";
 * String password = "passw0rd";
 */
public class JDBC {
    private PreparedStatement preparedStmt;
    /**
     * The Stmt.
     */
    Statement stmt;
    /**
     * The Conn.
     */
    Connection conn;
    /**
     * The Rs.
     */
    ResultSet rs;
    private BufferedReader reader;
    private String json = new String();
    private ResultSetMetaData metaData;

    /**
     * Gets conn.
     *
     * @return the conn
     */
    public Connection getConn() {
        return conn;
    }

    /**
     * Sets conn.
     *
     * @param conn the conn
     */
    public void setConn(Connection conn) {
        this.conn = conn;
    }

    /**
     * Instantiates a new Jdbc.
     */
    public JDBC() {
        connectDB("18.236.191.241:3306", "ToolShop", "testadmin", "passw0rd");
    }

    /**
     * Connect db.
     *
     * @param host     the host
     * @param dbname   the dbname
     * @param username the username
     * @param password the password
     */
    public void connectDB(String host, String dbname, String username, String password) {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        String url = "jdbc:mysql://"+host+"/"+dbname;
        try (Connection conn = DriverManager.getConnection(url,username,password)) {
            setConn(DriverManager.getConnection(url,username,password));
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println(ts+"\nConnected to: "+url);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Query.
     *
     * @param sql the sql
     */
    public void query(String sql){
        try (Statement stmt = conn.createStatement()) {
//            System.out.println("Querying: \n" +
//                    "----------"+sql+"\n----------");
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * D2L provided code
     * close()
     */
    public void close() {
        try {
            stmt.close();
            rs.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Might create a User table that contains login info in Mysql
     * Select user.
     */
    public void selectUser() {
        try {
            stmt = conn.createStatement();
            String query = "SELECT * FROM USER";
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                System.out.println(rs.getString("username") + " " +
                        rs.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * D2L provided code
     * Insert user.
     */
    public void insertUser() {
        try {
            stmt = conn.createStatement();
            String insert = "INSERT INTO USER (username,password) values " +
                    "('newUser','newPass')";
            int rowCount = stmt.executeUpdate(insert);
            System.out.println("row Count = " + rowCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * D2L provided code
     * Delete user.
     */
    public void deleteUser() {
        try {
            stmt = conn.createStatement();
            String delete = "DELETE FROM USER WHERE username = 'newUser'";
            int rowCount = stmt.executeUpdate(delete);
            System.out.println("row Count = " + rowCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * D2L provided code
     * Validate login.
     *
     * @param username the username
     * @param password the password
     * @return string
     */
    public String validateLogin(String username, String password) {
        try {
            stmt = conn.createStatement();
            String query = "SELECT * FROM USER WHERE username = '" + username
                    + "' and password ='" + password + "'";
            rs = stmt.executeQuery(query);
            if (rs.next()) {
                System.out.println("User is logged in");
                return "{\"valid\":\"1\"}"; // {"valid":"1"}
            } else {
                System.out.println("Invalid Username and Password");
                return "{\"valid\":\"0\"}";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "{\"valid\":\"1\", \"error\": \"1\"}"; //  {"valid":"1", "error": "1"}
    }

    /**
     * D2L provided code
     * Select user prepared statement.
     */
    public void selectUserPreparedStatement() {
        try {
            String query = "SELECT * FROM users where username= ? and password =?";
            PreparedStatement pStat = conn.prepareStatement(query);
            pStat.setString(1, "Jackson");
            pStat.setString(2, "123456");
            rs = pStat.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getString("username") + " " +
                        rs.getString("password"));
            }
            pStat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * D2L provided code
     * Insert user prepared statement.
     */
    public void insertUserPreparedStatement() {
        try {
            String query = "INSERT INTO users (ID,username, password) values" +
                    "(?,?,?)";
            PreparedStatement pStat = conn.prepareStatement(query);
            pStat.setInt(1, 1004);
            pStat.setString(2, "newUser");
            pStat.setString(3, "newPass");
            int rowCount = pStat.executeUpdate();
            System.out.println("row Count = " + rowCount);
            pStat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Search general purpose.
     *
     * @param table           the table
     * @param column          the column
     * @param strWithWildcard the str with wildcard
     * @throws JsonProcessingException the json processing exception
     */
    public void searchGeneralPurpose(String table, String column, String strWithWildcard) throws JsonProcessingException{
        try {
            query("use ToolShop;");
            String query= "SELECT T.ToolID,T.Name,T.Type,T.Quantity,T.Price,T.SupplierID,E.PowerType FROM  TOOL AS T \n"+
            "LEFT OUTER JOIN ELECTRICAL AS E ON T.ToolID=E.ToolID\n"+
            "WHERE T."+column+" like ?";
            
            PreparedStatement pStat = conn.prepareStatement(query);
            pStat.setString(1, strWithWildcard);
            rs = pStat.executeQuery();
            metaData=rs.getMetaData();
            toJsonToolList();
            pStat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void displayReturnStatement(String[] columns) throws SQLException {
        for (int i = 0; i < columns.length; i++)
            System.out.print (rs.getString(columns[i])+ "\t");
    }

    /**
     * Search for tool id string.
     *
     * @param toolID the tool id
     * @return the string
     * @throws JsonProcessingException the json processing exception
     */
    public String searchForToolID(int toolID) throws JsonProcessingException{
    		 searchGeneralPurpose("TOOL","ToolID", "%"+toolID+"%");
    		 
    		 return json;
     }

    /**
     * Search for tool name string.
     *
     * @param toolName the tool name
     * @return the string
     * @throws JsonProcessingException the json processing exception
     */
    public String searchForToolName(String toolName) throws JsonProcessingException{
      searchGeneralPurpose("TOOL","Name","%"+toolName+"%");
      return json;
     }

    /**
     * Check inventory.
     */
    public void checkInventory() {
        try {
            String table = "TOOL";
            query("use ToolShop;");
            String query = "SELECT ToolID,Quantity,SupplierID FROM "+table+" WHERE Quantity < ?";
            PreparedStatement pStat = conn.prepareStatement(query);
            pStat.setInt(1, 40);
         
            rs=pStat.executeQuery();

            if (rs.next())
            {
                System.err.println("CREATING NEW ORDER!!");
                int orderID=generateOrderID();
                createOrder(orderID);
                createOrderLine(orderID);
                updateToolTable(rs);
            }
            else
                System.err.println("No New ORDERES CREATED");
            pStat.close();
        }catch (SQLException e) {
            System.out.println(e);
        }
    }
    private void updateToolTable(ResultSet rs2) {
        try {
            System.err.println("UPDATEING TOOL TABLE!! WITH NEW ORDERS");
            String table = "TOOL";
            query("use ToolShop;");
            String query = "UPDATE "+table+" SET Quantity=? WHERE ToolID= ?";
            PreparedStatement pStat = conn.prepareStatement(query);
            while(rs2.next()) {
                pStat.setInt(1, 50);
                pStat.setInt(2, rs.getInt("ToolID"));
            }
            pStat.close();
        }catch (SQLException e) {
            System.err.println(e);
        }
    }

    /**
     * Purchase.
     *
     * @param toolID     the tool id
     * @param quantity   the quantity
     * @param customerID the customer id
     */
    public void purchase (int toolID, int quantity,int customerID) {
        try {
            updatePurchaseTable(toolID, customerID);//customer ID
            String table = "TOOL";
            query("use ToolShop;");
            String query = "UPDATE "+ table +" SET Quantity=Quantity-? WHERE ToolID=?";
            PreparedStatement pStat = conn.prepareStatement(query);
            pStat.setInt(1, quantity);
            pStat.setInt(2, toolID);
            int n=pStat.executeUpdate();
            checkInventory();
            pStat.close();

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void createOrderLine(int orderID) {
        try {
            String table = "ORDERLINE";
            query("use ToolShop;");
            String query1 = "INSERT INTO "+table+" VALUES(?,?,?,?)";
            String query2 = "UPDATE ToolShop.TOOL SET Quantity=? WHERE ToolID=?";
            PreparedStatement  pStat = conn.prepareStatement(query1);
            PreparedStatement  pStat2 = conn.prepareStatement(query2);
            //Cursor is at first ROW ,  COULDNT change position of cursor
            pStat2.setInt(1, 50);
            pStat2.setInt(2, rs.getInt("ToolID"));
            pStat.setInt(1, orderID);
            pStat.setInt(2, rs.getInt("ToolID"));
            pStat.setInt(3, rs.getInt("SupplierID"));
            pStat.setInt(4, (int)(50-rs.getInt("Quantity")));//assuming it creates orderline of 50
            pStat.executeUpdate();
            pStat2.executeUpdate();
            while(rs.next()) {

                pStat2.setInt(1, 50);
                pStat2.setInt(2, rs.getInt("ToolID"));
                pStat.setInt(1, orderID);
                pStat.setInt(2, rs.getInt("ToolID"));
                pStat.setInt(3, rs.getInt("SupplierID"));
                pStat.setInt(4, (int)(50-rs.getInt("Quantity")));//assuming it creates orderline of 50
                pStat.executeUpdate();
                pStat2.executeUpdate();
            }
            pStat.close();
            pStat2.close();

        }catch (SQLException e) {
            System.out.println(e);
        }
    }
    private void createOrder(int orderID) {
        try {
            String table = "ORDER_";
            query("use ToolShop;");
            String query = "INSERT INTO "+table+" VALUES(?,CURRENT_TIMESTAMP)";
            PreparedStatement pStat = conn.prepareStatement(query);
            pStat.setInt(1, orderID);
            pStat.executeUpdate();
            pStat.close();


        }catch (SQLException e) {
            System.out.println("HERE");
        }
    }
    private void updatePurchaseTable(int toolID, int customerID) {
        try {
            String table = "PURCHASE";
            query("use ToolShop;");
            String query = "INSERT INTO "+ table +" VALUES(?,?,CURRENT_TIMESTAMP)";
            PreparedStatement pStat = conn.prepareStatement(query);
            pStat.setInt(1, customerID);
            pStat.setInt(2, toolID);
            int n=pStat.executeUpdate();
            pStat.close();
        } catch (SQLException e) {
            System.out.println(e);
        }

    }

    /**
     * Generate order id int.
     *
     * @return the int
     */
    public int generateOrderID(){
        Random r = new Random( System.currentTimeMillis() );
        return ((1 + r.nextInt(2)) * 10000 + r.nextInt(10000));
    }

    /**
     * Get search result string.
     *
     * @param table           the table
     * @param column          the column
     * @param strWithWildcard the str with wildcard
     * @return the string
     */
    public String getSearchResult(String table, String column, String strWithWildcard){
        try {
            query("use ToolShop;");
            String query ="";
            switch (table){
                case "TOOL":
                    query = "SELECT T.ToolID,T.Name,T.Type,T.Quantity,T.Price,T.SupplierID,E.PowerType FROM ToolShop.TOOL AS T \n" +
                            "LEFT OUTER JOIN ToolShop.ELECTRICAL AS E ON T.ToolID=E.ToolID"
                            + " WHERE T." + column + " LIKE ?; ";
                    break;
                case "SUPPLIER":
                    query = "SELECT S.SupplierID,S.Name,S.Type,S.Address,S.CName,S.Phone,I.ImportTax FROM ToolShop.SUPPLIER AS S\n" +
                            "LEFT OUTER JOIN ToolShop.INTERNATIONAL AS I ON S.SupplierID =I.SupplierID "
                            + " WHERE S." + column + " LIKE ?; ";
                    break;
                case "CLIENT":
                    query = "select * from CLIENT"
                            +" WHERE " + column + " LIKE ?; ";
                    break;
                case "ORDER":
                    query = "SELECT O.OrderID,O.Date,T.Name,S.Name,L.Quantity FROM ToolShop.ORDERLINE AS L ,ToolShop.ORDER_ AS O ,ToolShop.TOOL AS T , ToolShop.SUPPLIER AS S\n"+
                            "WHERE L.OrderID=O.OrderID AND L.ToolID =  T.ToolID AND L.SupplierID=S.SupplierID"
                            + " AND O." + column + " LIKE ?; ";
                case "USER":
                	 query ="SELECT * FROM ToolShop.USER WHERE "+column+"=?";
                    break;
                default:
                    break;
            }


            PreparedStatement pStat = null;
            if (Utils.isInteger(strWithWildcard)||Utils.isNumeric(strWithWildcard)){
                query = query.replace("?",strWithWildcard);
                pStat = conn.prepareStatement(query);
            }
            if (!(Utils.isInteger(strWithWildcard)||Utils.isNumeric(strWithWildcard))){
                pStat = conn.prepareStatement(query);
                pStat.setString(1, strWithWildcard);
            }

            rs = pStat.executeQuery();
            metaData=rs.getMetaData();
            switch (table){
                case "TOOL":
                    toJsonToolList();
                    break;
                case "SUPPLIER":
                    toJsonSupplierList();
                    break;
                case "CLIENT":
                    toJsonCustomerList();
                    break;
                case "ORDER":
                    toJsonOrder();
                case "USER":
                    toJsonUser();
                    break;
                default:
                    break;
            }
            pStat.close();
        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Gets items list.
     *
     * @return the items list
     * @throws JsonProcessingException the json processing exception
     */
    public String getItemsList() throws JsonProcessingException{
        try {
            String query = "SELECT T.ToolID,T.Name,T.Type,T.Quantity,T.Price,T.SupplierID,E.PowerType FROM ToolShop.TOOL AS T \n" +
                    "LEFT OUTER JOIN ToolShop.ELECTRICAL AS E ON T.ToolID=E.ToolID";
            PreparedStatement pStat = conn.prepareStatement(query);
            rs = pStat.executeQuery();
            metaData=rs.getMetaData();
            toJsonToolList();
            pStat.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Gets suppliers list.
     *
     * @return the suppliers list
     * @throws JsonProcessingException the json processing exception
     */
    public String getSuppliersList() throws JsonProcessingException{
        try {
            query("use ToolShop;");
            String query = "SELECT S.SupplierID,S.Name,S.Type,S.Address,S.CName,S.Phone,I.ImportTax FROM ToolShop.SUPPLIER AS S\n" +
                    "LEFT OUTER JOIN ToolShop.INTERNATIONAL AS I ON S.SupplierID =I.SupplierID ";
            PreparedStatement pStat = conn.prepareStatement(query);
            rs = pStat.executeQuery();
            metaData=rs.getMetaData();
            toJsonSupplierList();
            pStat.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Gets customers list.
     *
     * @return the customers list
     * @throws JsonProcessingException the json processing exception
     */
    public String getCustomersList() throws JsonProcessingException {
        try {
            String table = "CLIENT";
            query("use ToolShop;");
            String query = "select * from " +
                    table;
            PreparedStatement pStat = conn.prepareStatement(query);
            rs = pStat.executeQuery();
            metaData=rs.getMetaData();
            toJsonCustomerList();
            pStat.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Gets order list.
     *
     * @return the order list
     * @throws JsonProcessingException the json processing exception
     */
    public String getOrderList() throws JsonProcessingException {
        try {
            query("use ToolShop;");
            String query = "SELECT O.OrderID,O.Date,T.Name,S.Name,L.Quantity FROM ToolShop.ORDERLINE AS L ,ToolShop.ORDER_ AS O ,ToolShop.TOOL AS T , ToolShop.SUPPLIER AS S\n"+
                    "WHERE L.OrderID=O.OrderID AND L.ToolID =  T.ToolID AND L.SupplierID=S.SupplierID";

            PreparedStatement pStat = conn.prepareStatement(query);
            rs=pStat.executeQuery();
            metaData=rs.getMetaData();
            toJsonOrder();
            pStat.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Gets table.
     *
     * @param tableName the table name
     * @return the table
     * @throws JsonProcessingException the json processing exception
     */
    public String getTable(String tableName) throws JsonProcessingException {
        switch (tableName){
            case "TOOL":
                return getItemsList();
            case "SUPPLIER":
                return getSuppliersList();
            case "CLIENT":
                return  getCustomersList();
            case "ORDER":
                return getOrderList();
            default:
                return null;
        }
    }

    /**
     * To json tool list.
     *
     * @throws SQLException            the sql exception
     * @throws JsonProcessingException the json processing exception
     */
    public void toJsonToolList() throws SQLException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();

        while(rs.next()) {
            ObjectNode node = new ObjectMapper().createObjectNode();
            node.put(metaData.getColumnName(1), rs.getInt(1));
            node.put(metaData.getColumnName(2), rs.getString(2));
            node.put(metaData.getColumnName(3), rs.getString(3));
            node.put(metaData.getColumnName(4), rs.getInt(4));
            node.put(metaData.getColumnName(5), rs.getFloat(5));
            node.put(metaData.getColumnName(6), rs.getInt(6));
            node.put(metaData.getColumnName(7), rs.getString(7));
            arrayNode.addAll(Arrays.asList(node));

        }
        json=mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);
    }

    /**
     * To json supplier list.
     *
     * @throws SQLException            the sql exception
     * @throws JsonProcessingException the json processing exception
     */
    public void toJsonSupplierList() throws SQLException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();

        while(rs.next()) {
            ObjectNode node = new ObjectMapper().createObjectNode();
            node.put(metaData.getColumnName(1), rs.getInt(1));
            node.put(metaData.getColumnName(2), rs.getString(2));
            node.put(metaData.getColumnName(3), rs.getString(3));
            node.put(metaData.getColumnName(4), rs.getString(4));
            node.put(metaData.getColumnName(5), rs.getString(5));
            node.put(metaData.getColumnName(6), rs.getString(6));
            node.put(metaData.getColumnName(7), rs.getFloat(7));
            arrayNode.addAll(Arrays.asList(node));

        }
        json=mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);
    }
    private void toJsonUser() throws SQLException, JsonProcessingException {
    	 ObjectNode node = new ObjectMapper().createObjectNode();
         while(rs.next()) {
            
             node.put(metaData.getColumnName(1), rs.getString(1));
             node.put(metaData.getColumnName(2), rs.getString(2));
 	}
         json=new ObjectMapper().writeValueAsString(node);
         
    }

    /**
     * To json customer list.
     *
     * @throws SQLException            the sql exception
     * @throws JsonProcessingException the json processing exception
     */
    public void toJsonCustomerList() throws SQLException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();

        while(rs.next()) {
            ObjectNode node = new ObjectMapper().createObjectNode();
            node.put(metaData.getColumnName(1), rs.getInt(1));
            node.put(metaData.getColumnName(2), rs.getString(2));
            node.put(metaData.getColumnName(3), rs.getString(3));
            node.put(metaData.getColumnName(4), rs.getString(4));
            node.put(metaData.getColumnName(5), rs.getString(5));
            node.put(metaData.getColumnName(6), rs.getString(6));
            node.put(metaData.getColumnName(7), rs.getString(7));
            arrayNode.addAll(Arrays.asList(node));

        }
        json=mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);
    }

    /**
     * To json order.
     *
     * @throws SQLException            the sql exception
     * @throws JsonProcessingException the json processing exception
     */
    public void toJsonOrder() throws SQLException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        
        

        while(rs.next()) {
            ObjectNode node = new ObjectMapper().createObjectNode();
            node.put(metaData.getColumnName(1), rs.getInt(1));
            node.put(metaData.getColumnName(2), rs.getTimestamp(2).toString());
            node.put(metaData.getColumnName(3), rs.getString(3));
            node.put(metaData.getColumnName(4), rs.getString(4));
            node.put(metaData.getColumnName(5), rs.getInt(5));
            
            arrayNode.addAll(Arrays.asList(node));

        }
        json=mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);
    }

    /**
     * Update SQL DB  @param tableName the table name
     *
     * @param row the row
     */
    public void insertIntoTable(String tableName, String[] row){
        StringBuilder sqlSB = new StringBuilder();
        sqlSB.append("INSERT INTO `" + tableName +"` "+"values(");
        for (int i = 0; i< row.length; i++){
            switch (server.controller.Utils.parseColumn(row[i])){
                case "int":
                case "float":
                case "timestamp":
                    sqlSB.append(row[i]);
                    break;
                default:
                    sqlSB.append("'"+row[i]+"'");
                    break;
            }
            if (i+1< row.length)
                sqlSB.append(",");
        }
        sqlSB.append(");");
        String sql = sqlSB.toString();
        System.out.println(sql);
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Insert into tool.
     *
     * @param ToolID     the tool id
     * @param Name       the name
     * @param Type       the type
     * @param Quantity   the quantity
     * @param Price      the price
     * @param SupplierID the supplier id
     */
    public void insertIntoTOOL(String ToolID, String Name, String Type, String Quantity, String Price, String SupplierID){
        String[] row = {ToolID,Name,Type,Quantity,Price,SupplierID};
        insertIntoTable("TOOL", row);
    }

    /**
     * Insert into supplier.
     *
     * @param SupplierID the supplier id
     * @param Name       the name
     * @param Type       the type
     * @param Address    the address
     * @param CName      the c name
     * @param Phone      the phone
     */
    public void insertIntoSUPPLIER (String SupplierID, String Name, String Type, String Address, String CName, String Phone){
        String [] row = {SupplierID, Name, Type, Address, CName, Phone};
        insertIntoTable("SUPPLIER", row);
    }

    /**
     * Insert into purchase.
     *
     * @param ClientID the client id
     * @param ToolID   the tool id
     * @param Data     the data
     */
    public void insertIntoPURCHASE (String ClientID, String ToolID, String Data){
        String [] row = {ClientID, ToolID, Data};
        insertIntoTable("PURCHASE", row);
    }

    /**
     * Insert into orderline.
     *
     * @param OrderID    the order id
     * @param ToolID     the tool id
     * @param SupplierID the supplier id
     * @param Quantity   the quantity
     */
    public void insertIntoORDERLINE (String OrderID, String ToolID, String SupplierID, String Quantity){
        String [] row = {OrderID, ToolID, SupplierID, Quantity};
        insertIntoTable("ORDERLINE", row);
    }

    /**
     * Insert into order.
     *
     * @param OrderID the order id
     * @param Date    the date
     */
    public void insertIntoORDER_ (String OrderID, String Date){
        String [] row = {OrderID, Date};
        insertIntoTable("ORDER_", row);
    }

    /**
     * Insert into international.
     *
     * @param SupplierID the supplier id
     * @param ImportTax  the import tax
     */
    public void insertIntoINTERNATIONAL (String SupplierID, String ImportTax){
        String [] row = {SupplierID, ImportTax};
        insertIntoTable("INTERNATIONAL", row);
    }

    /**
     * Insert into electrical.
     *
     * @param ToolID    the tool id
     * @param PowerType the power type
     */
    public void insertIntoELECTRICAL (String ToolID, String PowerType){
        String [] row = {ToolID, PowerType};
        insertIntoTable("ELECTRICAL", row);
    }

    /**
     * Insert into client.
     *
     * @param ClientID   the client id
     * @param LName      the l name
     * @param FName      the f name
     * @param Type       the type
     * @param PhoneNum   the phone num
     * @param Address    the address
     * @param PostalCode the postal code
     */
    public void insertIntoCLIENT(String ClientID, String LName, String FName, String Type, String PhoneNum, String Address, String PostalCode){
        String [] row = {ClientID, LName, FName, Type, PhoneNum, Address, PostalCode};
        insertIntoTable("CLIENT", row);
    }
 
}


/**
 * The type Utils.
 */
class Utils {
    /**
     * Is integer boolean.
     *
     * @param str the str
     * @return the boolean
     */
    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * Is numeric boolean.
     *
     * @param str the str
     * @return the boolean
     */
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    /**
     * Parse column string.
     *
     * @param str the str
     * @return the string
     */
    public static String parseColumn(String str){
        if (isInteger(str)) return "int";
        if (isNumeric(str)) return "float";
        if (str == "CURRENT_TIMESTAMP" ) return "timestamp";
        return "text";
    }
}