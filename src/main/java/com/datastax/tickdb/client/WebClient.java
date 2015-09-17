package com.datastax.tickdb.client;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.datastax.demo.service.TickDBService;
import com.datastax.timeseries.model.CandleStickSeries;
import com.datastax.timeseries.model.Periodicity;
import com.datastax.timeseries.model.TimeSeries;
import com.datastax.timeseries.utils.PeriodicityProcessor;

@Path("/tickdb/")
public class WebClient {

	private TickDBService tickDBService = new TickDBService();
	
	private DateTimeFormatter parser = DateTimeFormat.forPattern("yyyyMMddHHmmss");
	
	@GET
	@Path("/get/bydatetime/{exchange}/{symbol}/{fromdate}/{todate}")
	@Produces(MediaType.APPLICATION_JSON)
	public TimeSeries getTimeSeries(@PathParam("exchange") String exchange, @PathParam("symbol") String symbol,
			@PathParam("fromdate") String fromDateString, @PathParam("todate") String toDateString){
			
		DateTime fromDate = parser.parseDateTime(fromDateString);
		DateTime toDate = parser.parseDateTime(toDateString);
					
		return tickDBService.getTimeSeries(exchange, symbol, fromDate, toDate);
	}

	@GET
	@Path("/get/bydatetime/{exchange}/{symbol}/{fromdate}/{todate}/{periodicity}")
	@Produces(MediaType.APPLICATION_JSON)
	public TimeSeries getTimeSeriesPeriodicity(@PathParam("exchange") String exchange, @PathParam("symbol") String symbol,
			@PathParam("fromdate") String fromDateString, @PathParam("todate") String toDateString,
			@PathParam("periodicity") String periodicityString){
			
		DateTime fromDate = parser.parseDateTime(fromDateString);
		DateTime toDate = parser.parseDateTime(toDateString);
					
		TimeSeries timeSeries = tickDBService.getTimeSeries(exchange, symbol, fromDate, toDate);
		
		timeSeries.reverse();
		
		if (periodicityString != null){
			timeSeries = PeriodicityProcessor.getTimeSeriesByPeriod(timeSeries, Periodicity.valueOf(periodicityString), fromDate);
		}
		
		return timeSeries;
	}

	@GET
	@Path("/get/candlesticks/{exchange}/{symbol}/{fromdate}/{todate}/{periodicity}")
	@Produces(MediaType.APPLICATION_JSON)
	public CandleStickSeries getCandleStickSeries(@PathParam("exchange") String exchange, @PathParam("symbol") String symbol,
			@PathParam("fromdate") String fromDateString, @PathParam("todate") String toDateString,
			@PathParam("periodicity") String periodicityString){

		DateTime fromDate = parser.parseDateTime(fromDateString);
		DateTime toDate = parser.parseDateTime(toDateString);
			
		Periodicity periodicity = Periodicity.valueOf(periodicityString);
		
		CandleStickSeries candleStickSeries = tickDBService.getCandleStickSeries(exchange, symbol, fromDate, toDate, periodicity);
		
		return candleStickSeries;
	}
}
