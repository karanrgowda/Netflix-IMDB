import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
//import java.sql.Date;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
/**
 * Servlet implementation class CheckoutServlet
 */
@WebServlet(name = "CheckoutServlet", urlPatterns = "/api/checkout")
public class CheckoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
        String sessionId = session.getId();
        Long lastAccessTime = session.getLastAccessedTime();
        System.out.println("POST: CheckoutServlet");
        
        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("sessionID", sessionId);
        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());
        
        //get the previous items in a ArrayList
        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");
        JsonArray movieArray = new JsonArray();

        //Insert into sales table
        try {
        		// 1 Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();
    			// Iterate through Cart and insert each into sales table
            for (String item : previousItems) {
	            	if(item != null) {
	            		// ============ QUERY FOR SALE ID ============
	            		//2 Construct Query
	            		String idQuery = "select max(id) from sales;";
		    			String saleID = "";
		    			Statement IDstatement = dbcon.createStatement();
		    			ResultSet resultSet = IDstatement.executeQuery(idQuery);
		    			while(resultSet.next())
		    			{
		    				saleID = resultSet.getString("max(id)");
		    			}
		    			saleID = Integer.toString(Integer.parseInt(saleID) + 1);
		    			IDstatement.close();
		            
		    			// ============ QUERY FOR CUSTOMER ID ============
		            User customer = (User) request.getSession().getAttribute("user");
		            String uname = customer.getUsername();
		    			String cidQuery = "SELECT id from customers WHERE email = '" + uname + "';";
		    			String customerID = "";
		    			Statement CIDstatement = dbcon.createStatement();
		    			ResultSet resultSetcid = CIDstatement.executeQuery(cidQuery);
		    			while(resultSetcid.next())
		    			{
		    				customerID = resultSetcid.getString("id");
		    			}
		    			CIDstatement.close();
		    			
		    			// ============ INSERT INTO SALES TABLE ============
			        // 2 Construct Query
			        Date date = new Date();
			        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			        int year=localDate.getYear(), month=localDate.getMonthValue(), day=localDate.getDayOfMonth();
			        String today = year + "-" + month + "-" + day;
		            String query = "INSERT INTO sales VALUES(" + saleID + ", " + customerID + ", '" + item + "', '" + today + "');";	
		        		// 3 Declare our statement
		            Statement statement= dbcon.prepareStatement(query);
		 			// 4 Perform the query
		 			statement.executeUpdate(query);		 			
		 			//5 Close Resource
		 			statement.close();
		 			
		 			
		 			// ============ CART RESPONSE (SALEID, id, MOVIE TITLE, QUANTITY) for conf page ============
		            // 2 Construct Query
		            String movieQuery = "SELECT * FROM movies WHERE id='" + item + "';";
		        		// 3 Declare our statement
		            PreparedStatement movieStatement= dbcon.prepareStatement(movieQuery);
		 			// 4 Perform the query
		 			ResultSet rs = movieStatement.executeQuery();			
		            // 5 Iterate through each row of rs
		            while (rs.next()) {
						String title = rs.getString("title");
						String movie_id = rs.getString("id");
						int occurrences = Collections.frequency(previousItems, movie_id);
						// Create a JsonObject based on the data we retrieve from rs
		                JsonObject jsonObject = new JsonObject();
		                jsonObject.addProperty("id", movie_id);
		                jsonObject.addProperty("title", title);
		                jsonObject.addProperty("quantity", occurrences);
		                jsonObject.addProperty("saleID", saleID);
		                movieArray.add(jsonObject);
		            }
		                
		            movieStatement.close();
	            }
	            	
            }
            
             // write all the movie data into the jsonObject
		    response.getWriter().write(movieArray.toString());
	        
            //Empty Cart
            previousItems.clear();
            
            response.setStatus(200);
            dbcon.close();
        } catch (Exception e) {
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
	        response.getWriter().write(jsonObject.toString());

			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);

        }

	}
}
