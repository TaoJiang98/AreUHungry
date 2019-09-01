/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pizzaorder;

import static com.sun.org.apache.xalan.internal.lib.ExsltDatetime.date;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.HashMap;
import java.util.TreeSet;
import javax.annotation.Resource;
import javax.annotation.sql.DataSourceDefinition; 
import javax.inject.Named;
import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
// define the data source
@DataSourceDefinition(
   name = "java:global/jdbc/pizzadb",
   className = "org.apache.derby.jdbc.ClientDataSource",
   url = "jdbc:derby://localhost:1527/pizzadb",
   databaseName = "pizzadb",
   user = "app",
   password = "app")

@Named("PizzaBean")

@javax.faces.view.ViewScoped

/**
 *
 * @author tbj5108
 */
public class PizzaBean implements Serializable
{
    // instance variables
    private static String pizzaQuantity;
    private static String sidesQuantity;
    private static String drinkQuantity;
    private String phone;
    private int orderid;
    private int amount;
    private String email;
    private String firstName;
    private String lastName;

    // allow the server to inject the DataSource
    @Resource(lookup="java:global/jdbc/pizzadb")
    DataSource dataSource;
       
    //get the phonenumber
    public String getPhone()
    {
        return phone;
    }
    //set the phonenumber
    public void setPhone(String phone)
    {
        this.phone = phone;
    }
    
    //get the first name
    public String getFirstName()
    {
        return firstName;
    }
    //set the first name
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }
    //get the last name
    public String getLastName()
    {
        return lastName;
    }
    //set the last name
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }
    //get the email address
    public String getEmail()
    {
        return email;
    }
    //set the email address
    public void setEmail(String email)
    {
        this.email = email;
    }
    
    //return the current pizza quantity
    public String getPizzaQuantity(){
        return pizzaQuantity;
    }
    //store the pizzaQuantity
    public void setPizzaQuantity(String pizzaQuantity){
        this.pizzaQuantity = pizzaQuantity;
    }
    
    //return the current sides quantity
    public String getSidesQuantity(){
        return sidesQuantity;
    }
    //store sides quantity
    public void setSidesQuantity(String sidesQuantity){
        this.sidesQuantity = sidesQuantity;
    }
    
    //return the current drink quantity
    public String getDrinkQuantity(){
        return drinkQuantity;
    }
    //store drink quantity
    public void setDrinkQuantity(String drinkQuantity){
        this.drinkQuantity = drinkQuantity;
    }
    
    // Return a resultset of past pizza orders
    public ResultSet getOrders() throws SQLException{
        // check whether dataSource was injected by the server
        if (dataSource == null){
            throw new SQLException("Unable to obtain DataSource");
        }
        
        //obtain a connection from the connection pool 
        Connection connection = dataSource.getConnection();
        
        //check whether connection was successful
        if (connection == null)
        {
            throw new SQLException("Unable to connect to DataSource");
        }
        //diplay the database table base the the descending order time
        try
        {   
          PreparedStatement getOrders = connection.prepareStatement(
          "SELECT * FROM orders ORDER BY ORDERTIME DESC");
            CachedRowSet rowSet =
                    RowSetProvider.newFactory().createCachedRowSet();
            rowSet.populate(getOrders.executeQuery());
            return rowSet;
        }
        finally
        {
            connection.close(); //return this connection to pool
        }
    }
    
   
    private static String selection; //stores the current selection
    //get the selection on the getpizza page
    public String getSelection()
    {
        return selection;
    }
    //store user's selection
    public void setSelection(String selection){
        this.selection = selection;
    }
    
    private static String selection1; //stores the current selection1
    //get the selection on the getside page
    public String getSelection1()
    {
        return selection1;
    }
    //store user's selection
    public void setSelection1(String selection1){
        this.selection1 = selection1;
    }
    
    private static String selection2; //stores the current selection2
    //get the selection on the getdrink page
    public String getSelection2()
    {
        return selection2;
    }
    //store user's selection
    public void setSelection2(String selection2){
        this.selection2 = selection2;
    }
    
    //insert all the variable needed into customers table and orderitems table and order table
    public String register() throws SQLException
    {
        //check whether dataSource was injected 
        if (dataSource == null)
        {
            throw new SQLException("Unable to obtain DataSource");
        }
        //obtain a connection from the connnection pool
        Connection connection = dataSource.getConnection();
        
        //check whether connection was successful
        if (connection == null)
        {
            throw new SQLException("Unable to connect to DataSource");
        }
        
        //main part of inserting the data 
        try
        {       
            //create a PreparedStatement to insert a new customer 
            PreparedStatement addCustomer = 
                    connection.prepareStatement("INSERT INTO customers" 
                            + "( SELECT ? as PhoneNumber, ? as FirstName, ? as LastName, ? as Email "
                            + " FROM customers WHERE PhoneNumber = ? HAVING count(*)=0 )"); //to specify not inserting same customer again
            
            //specify the PreparedStatement's arguments
            addCustomer.setString(1, getPhone());
            addCustomer.setString(2, getFirstName());
            addCustomer.setString(3, getLastName());
            addCustomer.setString(4, getEmail());
            addCustomer.setString(5, getPhone());
            
            addCustomer.executeUpdate(); // insert the customer
            
            Date orderDate = new Date(); // get current time
            orderid = (int)(orderDate.getTime()&0x0000000000ffffffL); // generate order id
            //set the orderdate in the correct format
            String temp1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(orderDate);
            
            //create a PreparedStatement to insert a new pizza order
            PreparedStatement addOrderitems = 
                    connection.prepareStatement("INSERT INTO ordereditems" +
                            "( OrderId, PizzaId, PizzaQn, SidesId, SidesQn, DrinkId, DrinkQn )" +
                            "VALUES(?, ?, ?, ?, ?, ?, ?)");
            //specify the PreparedStatement's arguments
            addOrderitems.setInt(1, orderid);
            addOrderitems.setString(2, selection);
            addOrderitems.setString(3, getPizzaQuantity());
            addOrderitems.setString(4, selection1);
            addOrderitems.setString(5, getSidesQuantity());
            addOrderitems.setString(6, selection2);
            addOrderitems.setString(7, getDrinkQuantity());
            
            addOrderitems.executeUpdate(); // insert the orderitems
            
            //convert the pizza sides and drink quantity to an integer
            int pizzaqn = Integer.parseInt(pizzaQuantity);
            int sidesqn = Integer.parseInt(sidesQuantity);
            int drinkqn = Integer.parseInt(drinkQuantity);
            //calculate 12 different pairs amount
            if (selection.equals("1")) {
                if (selection1.equals("1")){
                    if (selection2.equals("1")){
                        amount = (8 * pizzaqn + 4 * sidesqn + 2 * drinkqn);
                    }
                    else if (selection2.equals("2")){
                        amount = 8 * pizzaqn + 4 * sidesqn + 1 * drinkqn;
                    }
                }
                else if (selection1.equals("2")){
                    if (selection2.equals("1")){
                        amount = 8 * pizzaqn + 6 * sidesqn + 2 * drinkqn;
                    }
                    else if (selection2.equals("2")){
                        amount = 8 * pizzaqn + 6 * sidesqn + 1 * drinkqn;
                    }
                }
            }
            else if (selection.equals("2")){
                    if (selection1.equals("1")){
                        if (selection2.equals("1")){
                            amount = 9 * pizzaqn + 4 * sidesqn + 2 * drinkqn;
                        }
                        else if (selection2.equals("2")){
                            amount = 9 * pizzaqn + 4 * sidesqn + 1 * drinkqn;
                        }
                    }
                    else if (selection1.equals("2")){
                        if (selection2.equals("1")){
                            amount = 9 * pizzaqn + 6 * sidesqn + 2 * drinkqn;
                        }
                        else if (selection2.equals("2")){
                            amount = 9 * pizzaqn + 6 * sidesqn + 1 * drinkqn;
                        }
                    }
                }
            else if (selection.equals("3")){
                    if (selection1.equals("1")){
                        if (selection2.equals("1")){
                            amount = 6 * pizzaqn + 4 * sidesqn + 2 * drinkqn;
                        }
                        else if (selection2.equals("2")){
                            amount = 6 * pizzaqn + 4 * sidesqn + 1 * drinkqn;
                        }
                    }
                    else if (selection1.equals("2")){
                        if (selection2.equals("1")){
                            amount = 6 * pizzaqn + 6 * sidesqn + 2 * drinkqn;
                        }
                        else if (selection2.equals("2")){
                            amount = 6 * pizzaqn + 6 * sidesqn + 1 * drinkqn;
                    }
                }
            }
        
            
            //create a PreparedStatement to insert a new order
            PreparedStatement addorder = 
                    connection.prepareStatement("INSERT INTO orders" +
                            "( OrderId, PhoneNumber, OrderTime, TotalPrice )" +
                            "VALUES(?, ?, ?, ?)");
            
            //specify the PreparedStatement's arguments
            addorder.setInt(1, orderid);
            addorder.setString(2, getPhone());
            addorder.setString(3, temp1);
            addorder.setInt(4, amount);
            
            addorder.executeUpdate(); // insert the order
            
            return "index"; // go back to index.xhtml page

        }
        finally
        {
            connection.close(); //return this connection to pool
        }
    }
}
