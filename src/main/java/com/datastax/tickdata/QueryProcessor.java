package com.datastax.tickdata;

import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.demo.utils.PropertyHelper;
import com.datastax.demo.utils.Timer;
import com.datastax.timeseries.model.CandleStickSeries;
import com.datastax.timeseries.model.Periodicity;
import com.datastax.timeseries.model.TimeSeries;
import com.datastax.timeseries.utils.CandleStickProcessor;
import com.datastax.timeseries.utils.FunctionProcessor;
import com.datastax.timeseries.utils.TechnicalAnalysis;

public class QueryProcessor {

	private static Logger logger = LoggerFactory.getLogger(QueryProcessor.class);

	private DateTimeFormatter parser = DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss");
	private Scanner scanIn = new Scanner(System.in);

	public QueryProcessor() {

		String contactPointsStr = PropertyHelper.getProperty("contactPoints", "localhost");

		TickDataDao dao = new TickDataDao(contactPointsStr.split(","));

		while (true) {

			System.out.print("Enter Symbol : ");
			String key = getInput("NASDAQ-AAPL-2014-03-28");

			System.out.print("Enter startTime : ");
			String startTime = getInput("2014-03-28 10:00:00");

			System.out.print("Enter endTime : ");
			String endTime = getInput("2014-03-28 10:20:00");

			System.out.print("Periodicity (HOUR, MINUTE) etc : ");
			String periodicity = getInput("MINUTE");

			System.out.print("Periodicity start time etc : ");
			String periodicityStartTime = getInput("2014-03-28 10:00:00");

			System.out.print("Moving Average period : ");
			String movingAveragePeriod = getInput("20");

			Timer timer = new Timer();
			timer.start();
			TimeSeries tickData = null;

			tickData = dao.getTickData(key, parser.parseDateTime(startTime), parser.parseDateTime(endTime));
			System.out.println(tickData.toFormatterString());

			// For analytics reverse series
			tickData.reverse();

			TimeSeries timeSeriesByPeriod = FunctionProcessor.getTimeSeriesByPeriod(tickData,
					Periodicity.valueOf(periodicity), parser.parseDateTime(periodicityStartTime));
			System.out.println("By Peridicity - " + periodicity);
			System.out.println(timeSeriesByPeriod.toFormatterString());

			if (!movingAveragePeriod.equals("")) {
				TimeSeries movingAverage = TechnicalAnalysis.calculateMovingAverage(tickData,
						Integer.parseInt(movingAveragePeriod));

				System.out.println("Moving Average - " + movingAveragePeriod);
				System.out.println(movingAverage.toFormatterString());

			}

			CandleStickSeries candleStickSeries;
			candleStickSeries = CandleStickProcessor.createCandleStickSeries(tickData, Periodicity.MINUTE, new DateTime(parser
					.parseDateTime(startTime).getMillis()));

			System.out.println(candleStickSeries);
			timer.end();

			logger.info("Got data and ran all analysis in " + timer.getTimeTakenMillis() + "ms");

		}
	}

	private String getInput(String defaultValue) {
		String value = scanIn.nextLine().trim();

		return value == null || value.equals("") ? defaultValue : value;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new QueryProcessor();
	}
}
