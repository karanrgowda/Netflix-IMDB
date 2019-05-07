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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		response.setContentType("application/json"); // Response mime type
		
		String searchString = request.getParameter("string");
		String searchParam = request.getParameter("parameter");
		String t = request.getParameter("title");
		String y = request.getParameter("year");
		String d = request.getParameter("director");
		String s = request.getParameter("star");
		String col = request.getParameter("column");
		String sequence = request.getParameter("sequence");
		String listLen = request.getParameter("length");
		String page = request.getParameter("page");

		// 2 Construct a query
        String primaryQuery = (searchString == null && searchParam == null) ? advancedQuery(t, y, d, s) : simpleQuery(searchString, searchParam);
		String query = displayConstraints(primaryQuery, col, sequence, listLen, page);
        System.out.println(query);
		
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

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
   
    protected String simpleQuery(String searchString, String searchParam) {
		String query;
		if(searchParam.equals("star")) {
			query = "SELECT m.id, title, year, director, rating FROM movies m, ratings r, stars_in_movies sm, stars s " +
            		"WHERE sm.starId = s.id AND m.id = sm.movieId AND m.id = r.movieId AND name LIKE '%"+ searchString + "%' ";
		} else {
			query = "SELECT id, title, year, director, rating from movies m,ratings r WHERE "+ searchParam +
            	" like '%"+ searchString + "%' AND m.id = r.movieId ";
		}
		return query;
    }
    
    protected String advancedQuery(String title, String year, String director, String star) {
    		String query;
    		String and0 = (star != "") ? "AND " : "";
    		String and1 = (title != "") ? "AND ": "";
    		String and2 = (title != "" || year != "") ? "AND ": "";
    		String and3 = (title != "" || year != "" || director != "") ? "AND ": "";
    		String and4 =  (title != "" || year != "" || director != "") ? " AND  ": "";;
    		String tStr = (title != "") ? "title LIKE '%"+ title + "%' ": "";
    		String yStr = (year != "") ? and1 + "year = " + year + " ": "";
		String dStr = (director != "") ? and2 + "director LIKE '%"+ director + "%' ": "";
		String sStr = (star != "") ? and3 + "name LIKE '%"+ star + "%' " : "";
		String conditions = tStr + yStr + dStr + sStr;
    		
    		if(star == "" || star == null) {
    			query = "SELECT DISTINCT id, title, year, director, rating from movies m,ratings r WHERE m.id = r.movieId"+ 
    					and4 + conditions + " ";
    		} else {
    			query = "SELECT DISTINCT m.id, title, year, director, rating FROM movies m, ratings r, stars_in_movies sm, stars s " +
                		"WHERE sm.starId = s.id AND m.id = sm.movieId AND m.id = r.movieId"+ and0 + conditions + " ";
    		}
    		
		return query;
    }
    
    protected String displayConstraints(String primaryQuery, String column, String sequence, String listLength, String page) {
    		int offset = (Integer.parseInt(page) - 1) * Integer.parseInt(listLength);
    		return primaryQuery + "ORDER BY " + column + " " + sequence +" LIMIT " + listLength + 
    				" OFFSET " + String.valueOf(offset) + ";";
    }
}
