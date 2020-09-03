/**
 * 
 */
package pricemonitor;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import org.h2.tools.Server;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * @author Chris Boado 905714629
 * @version Mar 26, 2016
 */
public class ConnectToDatabase {
    private int numRan = 0;
    private Server serv = null;
    private Connection con = null;
    private Statement stmt = null;
    private ResultSet rs = null;
    public ConnectToDatabase() throws Exception {
        try {
            System.setProperty("user.timezone", "EST");
            TimeZone.setDefault(null);
            serv = Server.createTcpServer("-tcpPassword", "", "-tcpAllowOthers").start();
            Class.forName("org.h2.Driver");
            con = DriverManager.getConnection("jdbc:h2:tcp://localhost/~/dfopricemonitor",
                    "sa", "");
            System.out.println("Connection Established: " + con.getMetaData().getDatabaseProductName() + "/"
                    + con.getCatalog());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    }
    
    public Server getServer() {
        return serv;
    }
    
    public Connection getConnection() {
        return con;
    }
    
    public Statement getStatement() {
        return stmt;
    }
    
    public ResultSet getResultSet() {
        return rs;
    }
    
    public ObservableList<String> getItemNames() throws SQLException {
        rs = stmt.executeQuery("SELECT * FROM DFOPRICEMONITOR ORDER BY NAME");
        rs.beforeFirst();
        ObservableList<String> items = FXCollections.observableArrayList();
        items.clear();
        while (rs.next()) {
            String name = rs.getString("NAME");
            items.add(name);
        }
        return items;
    }
    
    @SuppressWarnings("unchecked")
    public Item createItem(String itemName) throws Exception {
        if (numRan == 0) {
            numRan = 1;
        }
        else {
            rs = stmt.executeQuery("SELECT * FROM DFOPRICEMONITOR ORDER BY NAME");
        }
        rs.first();
        while (!Objects.equals(rs.getString("NAME"), itemName)) {
            rs.next();
        }
        String priceString = rs.getString("PRICEARRAY");
        String[] priceArray = priceString.split(",");
        ArrayList<Integer> priceList = new ArrayList<Integer>();
        for (int i = 0; i < priceArray.length; i++) {
            priceList.add(Integer.parseInt(priceArray[i]));
        }
        
        String[] recipeArray = rs.getString("RECIPE").split(",");
        int totalPrice = 0;
        if (Objects.equals(recipeArray[0], "N/A")) {
            totalPrice = 0;
        }
        else {
            for (int i = 0; i < recipeArray.length; i = i + 2) {
                totalPrice = totalPrice + Integer.valueOf(recipeArray[i]) * getIngredientPrice(recipeArray[i + 1]);
            }
        }
        
        rs.first();
        while (!Objects.equals(itemName, rs.getString("NAME"))) {
            System.out.println(rs.getString("NAME"));
            rs.next();
        }
        System.out.println(rs.getString("NAME"));
        Item item = new Item(rs.getInt("ID"),
                itemName,
                priceList,
                rs.getInt("VENDOR"),
                recipeArray,
                totalPrice,
                rs.getTimestamp("TIMESTAMP"));
        return item;
    }
    
    private int getIngredientPrice(String ingredient) throws SQLException {
        rs.first();
        while (!Objects.equals(rs.getString("NAME"), ingredient)) {
            rs.next();
        }
        String priceString = rs.getArray("PRICEARRAY").toString();
        priceString = priceString.substring(priceString.indexOf("'") + 1);
        priceString = priceString.substring(0, priceString.indexOf("'"));
        String[] priceArray = priceString.split(",");
        return Integer.valueOf(priceArray[0]);
    }
    
    public void addItem(String itemName, int vendorPrice, int currentPrice, String recipe) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement("INSERT INTO DFOPRICEMONITOR "
                + "(NAME, VENDOR, PRICEARRAY, RECIPE) VALUES (?,?,?,?)");
        pstmt.setString(1, itemName);
        pstmt.setInt(2, vendorPrice);
        pstmt.setString(3, String.valueOf(currentPrice));
        pstmt.setString(4, recipe);
        pstmt.executeUpdate();
    }
    
    public void updateItem(int ID, String itemName, int vendorPrice, String priceArray, String recipe) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement("UPDATE DFOPRICEMONITOR SET NAME = ?, VENDOR = ?, "
                + "PRICEARRAY = ?, RECIPE = ?, TIMESTAMP = ? WHERE ID = " + String.valueOf(ID));
        pstmt.setString(1, itemName);
        pstmt.setInt(2, vendorPrice);
        pstmt.setString(3, priceArray);
        pstmt.setString(4, recipe);
        pstmt.setTimestamp(5, new Timestamp(new Date().getTime()));
        pstmt.executeUpdate();
    }
    
    public void deleteItem(String itemName) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement("DELETE FROM DFOPRICEMONITOR WHERE NAME = ?");
        pstmt.setString(1, itemName);
        pstmt.executeUpdate();
    }
    
    public void disconnect() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    con.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    stmt.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    rs.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                serv.stop();
                System.out.println("Server stopped");
            }
        }));
    }
}
