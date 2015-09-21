# DataStax Tick Data

This is a simple example of using C* as a tick data store for financial market data.

## Running the Demo

* You will need a Java runtime (preferably 7) along with Maven 3 to run this demo.

* Start DSE 3.1.X or a Cassandra 1.2.X instance on your local machine. This demo just runs as a standalone process on the localhost. For more information on launching a development instance of Cassandra on your local machine, please see the DataStax blog post on [Cassandra Cluster Manager](http://www.datastax.com/dev/blog/ccm-a-development-tool-for-creating-local-cassandra-clusters).

* This demo uses quite a lot of memory, so it is worth setting `MAVEN_OPTS` to run Maven with more memory:

        export MAVEN_OPTS=-Xmx512M

* You will need the DataStax Timeseries library JAR in your local Maven repository. Run the Maven install on the [datastax-timeseries-lib](https://github.com/PatrickCallaghan/datastax-timeseries-lib) GitHub project.

## Queries

The queries that we want to be able to run is 
	
1. Get all the tick data for a symbol in an exchange (in a time range)

     select * from tick_data where symbol ='NASDAQ-NFLX-2014-01-31';
     
     select * from tick_data where symbol ='NASDAQ-NFLX-2014-01-31' and date > '2014-01-01 14:45:00' and date < '2014-01-01 15:00:00';

## Data 

The data is generated from a tick generator which uses a csv file to create random values from AMEX, NYSE and NASDAQ.

## Throughput 

To increase the throughput, add nodes to the cluster. Cassandra will scale linearly with the amount of nodes in the cluster.

## Schema Setup
Note : This will drop the keyspace "datastax_tickdata_demo" and create a new one. All existing data will be lost. 

curl -X GET -H "Content-Type: application/json"  

Dates are in format - yyyyMMddHHmmss

Periodicity's are 
MINUTE
MINUTE_5
MINUTE_15
MINUTE_30
HOUR

###Querying.

//Todays data
http://localhost:8080/datastax-tickdb/rest/tickdb/get/NASDAQ/AAPL

//To and From dates
http://localhost:8080/datastax-tickdb/rest/tickdb/get/bydatetime/NASDAQ/AAPL/20150914000000/20150917000000

//To and from dates broken into minute chunks 
http://localhost:8080/datastax-tickdb/rest/tickdb/get/bydatetime/NASDAQ/AAPL/20150914000000/20150917000000/MINUTE

//To and from dates broken into minute chunks and shown as candlesticks 
http://localhost:8080/datastax-tickdb/rest/tickdb/get/candlesticks/NASDAQ/AAPL/20150914000000/20150917000000/MINUTE_5

###Services

//For all exchanges and symbols, run daily conversion of tick data to binary data for long term storage and retrieval 
http://localhost:8080/datastax-tickdb/rest/tickdb/get/rundailyconversion

//For a specific symbol and todays date, run daily conversion of tick data to binary data for long term storage and retrieval
http://localhost:8080/datastax-tickdb/rest/tickdb/get/rundailyconversionbysymbol/NASDAQ/AAPL

//For a specific symbol and date, run daily conversion of tick data to binary data for long term storage and retrieval
http://localhost:8080/datastax-tickdb/rest/tickdb/get/rundailyconversionbysymbolanddate/NASDAQ/AAPL/20150917000000





