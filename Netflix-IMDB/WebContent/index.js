/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleStarResult(resultData) {
    console.log("handleTopMovieResults: populating top movie table from resultData");
    
    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let movieTable = jQuery("#top_movie_table_body");

    // Iterate through resultData
    for (let i = 0; i < resultData.length; i++) {
    		var starsString = "", genresString = "";
    		//generate stars list inner HTML
        for (let j = 0; j < Math.min(5, resultData[i]['stars'].length); j++) {
	        	// Add a link to single-star.html with id passed with GET url parameter
    			seperator = (j == Math.min(4, resultData[i]['stars'].length - 1) || resultData[i]['stars'].length == 1) ? "": ", ";
	    		starsString+='<a href="single-star.html?id=' + resultData[i]['stars'][j]["id"] + '">' +  // display star_name for the link text
	        resultData[i]['stars'][j]["name"] + '</a>' + seperator;
        }
    		//generate genre list inner HTML
        for (let j = 0; j < resultData[i]['genres'].length; j++) {
			seperator = (j == (resultData[i]['genres'].length - 1) || resultData[i]['genres'].length == 1) ? "": "/";
        		genresString += resultData[i]['genres'][j]["name"] + seperator;
        }
    	
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = 	'<div class="card" style="margin-top: 5px;">' +
        	'<div class="card-body">' +
		    '<div class="container">'+
		    		'<div class="row">' + 
				    '<div class="col-6"><h5 class="card-title">' + '<a href="single-movie.html?id=' + resultData[i]["id"] + '">'+ 
				    resultData[i]["title"] + '</a></h5></div>' +   // display star_name for the link text
				    '<div class="col-6"><h6 class="text-muted" style="text-align: right;">' + resultData[i]["year"] +'</h6></div>' + 
				'</div>' +
				'<div class="row">' + 
					'<div class="col-12">' +
					    '<p class="card-text">' + genresString + '</p>' +
					    '<p class="card-text">Directed by ' + resultData[i]["director"] +'</p>' +
					    '<p class="card-text">Cast: ' + starsString + '</p>' +
					    '<p class="card-text mb-2 text-muted">Rating: ' + resultData[i]['rating'] + '</p>' +
				    '</div>' +
			    '</div>' +
		  '</div>' +
		  '</div>';
        
        // Append the row created to the table body, which will refresh the page
        movieTable.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/top20", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});