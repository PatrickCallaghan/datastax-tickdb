package com.datastax.tickdata.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.tickdata.model.TickData;

public class TickGenerator {

	private static Logger logger = LoggerFactory.getLogger(TickGenerator.class);
	private long TOTAL_TICKS = 0;

	private List<TickValue> tickValueList = new ArrayList<TickValue>();

	public TickGenerator(List<String> exchangeSymbols) {

		int count = 1;
		for (String symbol : exchangeSymbols) {

			tickValueList.add(new TickValue(symbol, count++));
		}		
	}

	public long getTicksGenerated() {
		return TOTAL_TICKS;
	}

	public void generatorTicks(BlockingQueue<List<TickData>> queueTickData, long noOfTicks) {

		List<TickData> flusher = new ArrayList<TickData>();

		for (int i = 0; i < noOfTicks; i++) {

			TickValue tickValue = getTickValueRandom();
			flusher.add(new TickData(tickValue.tickSymbol, tickValue.value));

			TOTAL_TICKS++;

			if (i % 20 == 0) {
				try {
					queueTickData.put(new ArrayList<TickData>(flusher));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				flusher.clear();
			}

			if (i % 100 == 0) {
				sleepMillis(1);
			}
		}
		try {
			queueTickData.put(new ArrayList<TickData>(flusher));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		flusher.clear();
	}

	public void generatorTicksFromNow(BlockingQueue<List<TickData>> queueTickData, long noOfTicks) {

		List<TickData> flusher = new ArrayList<TickData>();

		for (int i = 0; i < noOfTicks; i++) {

			DateTime dateTime = DateTime.now();

			TickValue tickValue = getTickValueRandom();
			flusher.add(new TickData(tickValue.tickSymbol, tickValue.value, dateTime));

			TOTAL_TICKS++;

			if (i % 100 == 0) {
				try {
					queueTickData.put(new ArrayList<TickData>(flusher));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				flusher.clear();
			}

			if (i % 30 == 0) {
				sleepMillis(1);
			}
		}
		try {
			queueTickData.put(new ArrayList<TickData>(flusher));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		flusher.clear();
	}

	private void sleepMillis(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class TickValue {

		String tickSymbol;
		double value;

		public TickValue(String tickSymbol, double value) {
			super();
			this.tickSymbol = tickSymbol;
			this.value = value;
		}
	}

	TickValue getTickValueRandom() {

		TickValue tickValue = tickValueList.get((int) (Math.random() * tickValueList.size()));
		tickValue.value = this.createRandomValue(tickValue.value);
		return tickValue;
	}

	private double createRandomValue(double lastValue) {

		double up = Math.random() * 2;
		double percentMove = (Math.random() * 1.0) / 10;

		if (up < 1) {
			lastValue -= percentMove;
		} else {
			lastValue += percentMove;
		}

		return lastValue;
	}
}
