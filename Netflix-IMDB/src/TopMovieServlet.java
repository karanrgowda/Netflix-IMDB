import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Servlet implementation class TopMovieServlet
 */
@WebServlet(name = "TopMovieServlet", urlPatterns = "/api/top20")
public class TopMovieServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	// Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Declare our statement
            Statement statement = dbcon.createStatement();
            String query = "SELECT id, title, director, year, rating from ratings r, movies m WHERE m.Id = r.movieId ORDER BY rating desc LIMIT 20;";

            // Perform the query
            ResultSet rs = statement.executeQuery(query);
            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
				String title = rs.getString("title");
				String director = rs.getString("director");
				String year = rs.getString("year");
				String rating = rs.getString("rating");
				String movie_id = rs.getString("id");
				
				// ===== For Genre =====
	            // Declare our statement
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
                		

				// ===== For Stars =====
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
}
