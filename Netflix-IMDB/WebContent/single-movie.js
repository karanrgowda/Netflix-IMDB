/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */

/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    let movieInfoElement = jQuery("#movie_info");
    var movieInfoHTML ="", genreList = "", starsHTML ="";
    for (let i = 0; i < resultData["genres"].length; i++) {
		seperator = (i == (resultData["genres"].length - 1) || resultData["genres"].length == 1) ? "": "/";
		genreList += resultData["genres"][i]["name"] + seperator;
    }
    
    movieInfoHTML = "<h2>" + resultData["title"] + " (" + resultData["year"] + ")</h2>" +
    '<p>' + genreList + '</p>' +
    '<p> Directed by ' +  resultData["director"] + '</p>' +
    '<p>Rating: ' + resultData["rating"] + '/10 (' + resultData["numVotes"] + ' votes)</p>';
    movieInfoElement.append(movieInfoHTML);
  
	let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let j = 0; j < resultData["stars"].length; j++) {
    		// Add a link to single-star.html with id passed with GET url parameter
		var rowOpen = (j%2 == 0) ? "<tr>" : "";
    		var rowClose = (j%2 != 0) ? "</tr>" : "";
    		starsHTML+= rowOpen + '<th><p><a href="single-star.html?id=' + resultData['stars'][j]["id"] + '">'
        + resultData['stars'][j]["name"] +     // display star_name for the link text
        '</a></p></th>' + rowClose;
    }
    
    // Append the row created to the table body, which will refresh the page
    movieTableBodyElement.append(starsHTML);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});