import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

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
 * Servlet implementation class IndexServlet
 */
@WebServlet(name = "IndexServlet", urlPatterns = "/api/index")
public class IndexServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

	 /**
     * handles POST requests to store session information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        Long lastAccessTime = session.getLastAccessedTime();
        
        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("sessionID", sessionId);
        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    		response.setContentType("application/json"); // Response mime type
        String item = request.getParameter("item");
        HttpSession session = request.getSession();
        
        //get the previous items in a ArrayList
        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");
        
        //CART OPERATIONS
        if(item != null && previousItems != null) {
	        	if(item.equals("_DEC_ITEM_")){
	    			String id = request.getParameter("id");
	    			if (previousItems != null && id != null) {
	    				synchronized (previousItems) {
	    					previousItems.remove(id);
	    				}
	    			}	
	        	}else if (item != null && item.equals("_INC_ITEM_")){
				String id = request.getParameter("id");
				if (previousItems != null && id != null) {
					synchronized (previousItems) {
						previousItems.add(id);
					}
				}	
	        } else if(item != null && item.equals("_DELETE_ALL_ITEM_")){
				String id = request.getParameter("id");
				if (previousItems != null) {
					synchronized (previousItems) {
						previousItems.removeAll(Collections.singleton(id));
					}
				}	
	        }
	        	else {
	        		synchronized (previousItems) {
	        			previousItems.add(item);
	        			System.out.println(previousItems);
		        	}
	        	}
        } 
        else if  (previousItems == null) {
            previousItems = new ArrayList<>();
            previousItems.add(item);
            session.setAttribute("previousItems", previousItems);
        }
        
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        
        try {
            // 1 Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();
            
            JsonArray movieArray = new JsonArray();
            
            // 2 Construct Query
            String query = "SELECT * FROM movies WHERE ";
	    		for(int i = 0; i < previousItems.size(); i++) {
	    			String sep = (i == previousItems.size() - 1) ? "": " OR ";
	    			query += "id ='" + previousItems.get(i) + "'" + sep;
	    		}
	    		query += ";";

        		// 3 Declare our statement
            PreparedStatement statement= dbcon.prepareStatement(query);
		
 			// 4 Perform the query
 			ResultSet rs = statement.executeQuery();			

            // 5 Iterate through each row of rs
            while (rs.next()) {
				String title = rs.getString("title");
				String director = rs.getString("director");
				String year = rs.getString("year");
				String movie_id = rs.getString("id");
				int occurrences = Collections.frequency(previousItems, movie_id);
								
				// ===== Queries For Genre =====
				JsonArray genre_array = genreArray(dbcon, movie_id);
                		
				// ===== Queries For Stars =====
				JsonArray star_array = starArray(dbcon, movie_id);

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", movie_id);
                jsonObject.addProperty("title", title);
                jsonObject.addProperty("director", director);
                jsonObject.addProperty("quantity", occurrences);
                jsonObject.addProperty("year", year);
                jsonObject.add("genres", genre_array);
                jsonObject.add("stars", star_array);
                movieArray.add(jsonObject);
            }
                
            // write JSON string of all movies in cartto output
            out.write(movieArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
            dbcon.close();
            rs.close();
            statement.close();
        } catch (Exception e) {
        	
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);

        }
        out.close();
    }
    
    protected JsonArray genreArray(Connection dbcon, String movie_id) {
		try {
	        Statement genre_statement = dbcon.createStatement();
	        String genre_query = "SELECT g.id, name from movies m, genres g, genres_in_movies gm WHERE "
	        		+ "m.id='"+movie_id+"' AND m.Id=gm.movieId AND gm.genreId = g.id;";
	        // Perform the query
	        ResultSet genre_rs = genre_statement.executeQuery(genre_query);
	        JsonArray genre_array = new JsonArray();
	        // Iterate through each row of genre_rs
	        while (genre_rs.next()) {
	        		String genre_id = genre_rs.getString("id");
	        		String genre_name = genre_rs.getString("name");
	        		
	        		JsonObject genre_jsonObject = new JsonObject();
	        		genre_jsonObject.addProperty("id", genre_id);
	        		genre_jsonObject.addProperty("name", genre_name);
	        		genre_array.add(genre_jsonObject);
	        }
	        
	        return genre_array;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
    			e.printStackTrace();
    			return null;
    		}
    }
    
	protected JsonArray starArray(Connection dbcon, String movie_id) {
		try {
			// Declare our statement
			Statement star_statement = dbcon.createStatement();
			String star_query = "SELECT s.id, s.name from movies m, stars s, stars_in_movies sm WHERE "
					+ "m.id='"+movie_id+"' AND m.Id=sm.movieId AND sm.starId = s.id;";
			// Perform the query
			ResultSet star_rs = star_statement.executeQuery(star_query);
			JsonArray star_array = new JsonArray();
			// Iterate through each row of genre_rs
			while (star_rs.next()) {
					String star_id = star_rs.getString("id");
					String star_name = star_rs.getString("name");
					
					JsonObject star_jsonObject = new JsonObject();
					star_jsonObject.addProperty("id", star_id);
					star_jsonObject.addProperty("name", star_name);
					star_array.add(star_jsonObject);
			}
			return star_array;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
		}
	 }
}
