import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movielist")
public class MovieListServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json"); // Response mime type
		//Main constraints on browse
		String searchString = request.getParameter("category");
		String searchParam = request.getParameter("parameter");
		//Constraints for filtering/sorting
		String col = request.getParameter("column");
		String sequence = request.getParameter("sequence");
		String listLen = request.getParameter("length");
		String page = request.getParameter("page");
		
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        
        // 2 Construct a query
        String primaryQuery = (searchString.equals("genre")) ?
        			"SELECT m.id, title, year, director, rating "
        			+ "FROM movies m, genres g, ratings r, genres_in_movies gm "
        			+ "WHERE g.name = '" + searchParam + "' AND m.id = gm.movieId AND gm.genreId = g.id AND m.id = r.movieId "
        			:"SELECT m.id, title, year, director, rating FROM movies m, ratings r " + 
            		"WHERE title LIKE '" + searchParam + "%' AND m.id = r.movieId ";
  
		String query = displayConstraints(primaryQuery, col, sequence, listLen, page);
		
		System.out.println(query);

        try {
            // 1 Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();
            
            

            // 3 Declare our statement
            PreparedStatement statement= dbcon.prepareStatement(query);
		
 			// 4 Perform the query
 			ResultSet rs = statement.executeQuery();			
            JsonArray jsonArray = new JsonArray();

            // 5 Iterate through each row of rs
            while (rs.next()) {
				String title = rs.getString("title");
				String director = rs.getString("director");
				String year = rs.getString("year");
				String rating = rs.getString("rating");
				String movie_id = rs.getString("id");
				
				// ===== Queries For Genre =====
				JsonArray genre_array = genreArray(dbcon, movie_id);
                		
				// ===== Queries For Stars =====
				JsonArray star_array = starArray(dbcon, movie_id);

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", movie_id);
                jsonObject.addProperty("title", title);
                jsonObject.addProperty("director", director);
                jsonObject.addProperty("year", year);
                jsonObject.addProperty("rating", rating);
                jsonObject.add("genres", genre_array);
                jsonObject.add("stars", star_array);
                jsonArray.add(jsonObject);
            }
            
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

            rs.close();
            statement.close();
            dbcon.close();
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
	        // Declare our statement
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
		 // Declare our statement
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
	    
	protected String displayConstraints(String primaryQuery, String column, String sequence, String listLength, String page) {
	    	int offset = (Integer.parseInt(page) - 1) * Integer.parseInt(listLength);
			return primaryQuery + "ORDER BY " + column + " " + sequence +" LIMIT " + listLength + 
	" OFFSET " + String.valueOf(offset) + ";";
	 }

}
