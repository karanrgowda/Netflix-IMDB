CREATE TABLE movies(
	id varchar(10) PRIMARY KEY,
	title varchar(100) NOT NULL, 
	year integer NOT NULL,
	director varchar(100) NOT NULL
);

CREATE TABLE stars(
	id varchar(10) primary key,
	name varchar(100) NOT NULL,
	birthYear integer 
);

 CREATE TABLE stars_in_movies(
	starId varchar(10),
	movieId varchar(10), 
    FOREIGN KEY(starId) REFERENCES stars(id),
    FOREIGN KEY(movieId) REFERENCES movies(id)
);

CREATE TABLE genres(
	id integer PRIMARY KEY auto_increment,
    name varchar(32) NOT NULL
);

CREATE TABLE genres_in_movies(
	genreId integer NOT NULL,
    movieId varchar(10) NOT NULL,
    PRIMARY KEY(genreId, movieId),
    FOREIGN KEY(genreId) references genres(id),
    FOREIGN KEY(movieId) references movies(id)
);
 
CREATE TABLE creditcards(
	id varchar(20) PRIMARY KEY,
    firstName varchar(50) NOT NULL,
    lastName varchar(50) NOT NULL,
    expiration date NOT NULL
);

CREATE TABLE customers(
	id integer PRIMARY KEY,
    firstName varchar(50) NOT NULL,
    lastName varchar(50) NOT NULL,
    ccId varchar(20) NOT NULL,
    address varchar(200) NOT NULL,
    email varchar(50) NOT NULL,
    password varchar(20) NOT NULL,
    FOREIGN KEY(ccId) references creditcards(id)
);

CREATE TABLE sales(
	id integer PRIMARY KEY auto_increment,
	customerId integer NOT NULL,
    movieId varchar(10) NOT NULL,
    saleDate DATE NOT NULL,
    FOREIGN KEY(customerId) references customers(id),
    FOREIGN KEY(movieId) references movies(id)
);

CREATE TABLE ratings(
	movieId varchar(10) PRIMARY KEY,
    rating float NOT NULL,
    numVotes integer NOT NULL
);