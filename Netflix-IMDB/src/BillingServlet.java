import com.google.gson.JsonObject;
import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

//
@WebServlet(name = "BillingServlet", urlPatterns = "/api/billing")
public class BillingServlet extends HttpServlet {
    private static final long serialVersionUID = 5L;
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = request.getParameter("cardid");
        String fname = request.getParameter("firstname");
        String lname = request.getParameter("lastname");
        String date = request.getParameter("edate");
        
        try {
	     	Connection connection = dataSource.getConnection();
	     	// prepare query
	     	String query = "select * from creditcards where id=? and firstName=? and lastName=? and expiration=?";
	     	PreparedStatement statement = connection.prepareStatement(query);
	     	statement.setString(1, id);
	     	statement.setString(2, fname);
	     	statement.setString(3, lname);
	     	statement.setString(4, date);
	     	System.out.println("Billing Servlet");
	     	ResultSet resultSet = statement.executeQuery();
	
	        if (resultSet.next()) {	          
	            request.getSession().setAttribute("billing", new Billing(id,fname,lname,date));            
	            JsonObject responseJsonObject = new JsonObject();
	            responseJsonObject.addProperty("status", "success");
	            responseJsonObject.addProperty("message", "success");	
	            response.getWriter().write(responseJsonObject.toString());
	        } else {
	            JsonObject responseJsonObject = new JsonObject();
	            responseJsonObject.addProperty("status", "fail");
	            responseJsonObject.addProperty("message", "Invalid credit card information! Please enter again!");
	            response.getWriter().write(responseJsonObject.toString());
	        }
	    }
        catch(Exception e) {
			e.printStackTrace();
		}
    }
}