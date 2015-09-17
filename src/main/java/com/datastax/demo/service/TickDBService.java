package com.datastax.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.demo.utils.Timer;
import com.datastax.tickdata.TickDataBinaryDao;
import com.datastax.tickdata.TickDataDao;
import com.datastax.tickdata.engine.TimeSeriesBinaryReader;
import com.datastax.tickdata.engine.TimeSeriesReader;
import com.datastax.timeseries.model.CandleStickSeries;
import com.datastax.timeseries.model.Periodicity;
import com.datastax.timeseries.model.TimeSeries;
import com.datastax.timeseries.utils.CandleStickProcessor;
import com.datastax.timeseries.utils.DateUtils;
import com.datastax.timeseries.utils.TimeSeriesUtils;

public class TickDBService {
	
	private static Logger logger = LoggerFactory.getLogger(TickDBService.class);
	
	private TickDataDao tickDataDao = new TickDataDao(new String[]{"127.0.0.1"});
	private TickDataBinaryDao tickDataBinaryDao = new TickDataBinaryDao(new String[]{"127.0.0.1"});

	public void convertTickDataToTimeSeries(String exchange, String symbol, DateTime date){
		TimeSeries timeSeries = tickDataDao.getTickData(exchange, symbol, date.withMillisOfDay(0), date);

		try {
			if (timeSeries!=null){
				tickDataBinaryDao.insertTimeSeries(timeSeries);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get times series data between 2 dates. If its historic it will be from the binary store 
	 * and if its current it will be in the tick data store.
	 * @param exchange
	 * @param symbol
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	public TimeSeries getTimeSeries(String exchange, String symbol, DateTime fromDate, DateTime toDate) {
		List<DateTime> dates = DateUtils.getDatesBetween(fromDate, toDate);
		
		List<TimeSeries> timeSeriesDays = getTimeSeriesForDates(exchange, symbol, dates, toDate);
		
		TimeSeries finalTimeSeries = null;
		
		for (TimeSeries timeSeries : timeSeriesDays){
			finalTimeSeries =  TimeSeriesUtils.mergeTimeSeries(finalTimeSeries, timeSeries);					
		}
		return finalTimeSeries;
	}

	private List<TimeSeries> getTimeSeriesForDates(String exchange, String symbol, List<DateTime> dates, DateTime toDate) {
		List<Future<TimeSeries>> results = new ArrayList<Future<TimeSeries>>();
		List<TimeSeries> timeSeriesDays = new ArrayList<TimeSeries>();
		
		DateTime todayMidnight = new DateTime().withMillisOfDay(0);
		
		for (DateTime dateTime : dates){
			
			if (dateTime.isBefore(todayMidnight)){
				String key = generateKey(exchange, symbol, dateTime);			
				results.add(new TimeSeriesBinaryReader(tickDataBinaryDao, key));
			}else{
				results.add(new TimeSeriesReader(tickDataDao, exchange, symbol, todayMidnight, toDate));
			}
		}
		
		for (Future<TimeSeries> future : results) {
			try {
				TimeSeries timeSeries = future.get();
			
				timeSeriesDays.add(timeSeries);
			} catch (InterruptedException | ExecutionException e) {				
				e.printStackTrace();
			}
		}
		return timeSeriesDays;
	}

	public CandleStickSeries getCandleStickSeries(String exchange, String symbol, DateTime fromDate, DateTime toDate, Periodicity periodicity) {
		
		TimeSeries timeSeries = getTimeSeries(exchange, symbol, fromDate, toDate);
		timeSeries.reverse();
		
		return CandleStickProcessor.createCandleStickSeries(timeSeries, periodicity, fromDate);		
	}

	private String generateKey(String exchange, String symbol, DateTime startTime) {
		return exchange.toUpperCase() + "-" + symbol.toUpperCase() + "-" + startTime.getYear() + "-" 
					+ fillNumber(startTime.getMonthOfYear()) + "-" + fillNumber(startTime.getDayOfMonth()); 
	}
	
	private String fillNumber(int num) {
		return num < 10 ? "0" + num : "" + num;
	}
	
	public static void main(String args[]){
		TickDBService service = new TickDBService();
		
		DateTime time = new DateTime();		
		
		Timer t = new Timer();
		TimeSeries result = service.getTimeSeries("NASDAQ", "AAPL", time.minusDays(5), time);
		logger.info(new DateTime(result.lowestDate()).toString());
		logger.info(new DateTime(result.highestDate()).toString());		
		t.end();		
		logger.info("Load took " + t.getTimeTakenMillis() + "ms for " + result.getDates().length + " ticks.");
				
//		Timer t1 = new Timer();		
//		service.convertTickDataToTimeSeries("NASDAQ", "AAPL", DateTime.now());
//		t1.end();
//		logger.info("Convert took " + t1.getTimeTakenMillis() + "ms");
		
		Timer t2 = new Timer();
		CandleStickSeries candles = service.getCandleStickSeries("NASDAQ", "AAPL", time.minusDays(5), time, Periodicity.HOUR);
		t2.end();		
		logger.info("Load of Candles took " + t.getTimeTakenMillis() + "ms for " + candles.getCandleSticks().size() + " ticks.");
		
		logger.info(candles.getCandleSticks().toString());

		System.exit(0);
	}
}
