# Netflix-IMDB
Commercial Website Under Development

http://ec2-18-223-120-171.us-east-2.compute.amazonaws.com:8080/Fablix

## Problem
Build functionalities such as browsing movies by category, searching movies by condition, adding movies to the shopping cart, etc. 

### Basic project Structure:
- Login: To log into the system, customers need to enter correct email and password. You are required to use HTTP POST instead of HTTP GET so that the username and password will not be displayed on the address bar. When authenticating user input information, columns: email and password in customers table can be used for reference.
- Main page: Customers can browse and search movies on the main page.
- Searching: When customers search for movies by multiple conditions, you should use AND logic to combine conditions. Customers can search for movies by single or multiple conditions:
	- title
	- year
	- director
	- star's name

- Movie list page: Customers can find search results on this page. Search results should be formatted as a table with at least the following columns:
	- id
	- title (hyperlinked to the corresponding single movie page)
	- year
	- director
	- list of genres (each hyperlinked to a page that shows all the movies of this genre)
	- list of stars (each hyperlinked to the corresponding single star page implemented)
	- rating

The following details of functionalities should also be met.
- Substring matching
- Sorting: 
- Previous/Next: 
- Jump Functionality:
- Shopping Cart:
- Checkout: 

Host the web app on an AWS EC2 instance.



