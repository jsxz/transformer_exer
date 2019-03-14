package com.z.transformer.mr.statistics;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

import com.z.transformer.common.DateEnum;
import com.z.transformer.common.EventLogConstants;
import com.z.transformer.common.GlobalConstants;
import com.z.transformer.common.KpiType;
import com.z.transformer.dimension.key.base.BrowserDimension;
import com.z.transformer.dimension.key.base.DateDimension;
import com.z.transformer.dimension.key.base.KpiDimension;
import com.z.transformer.dimension.key.base.PlatformDimension;
import com.z.transformer.dimension.key.stats.StatsCommonDimension;
import com.z.transformer.dimension.key.stats.StatsUserDimension;
import com.z.transformer.util.TimeUtil;

/**
 * 思路：HBase读取数据 --> HBaseInPutFormat --> Mapper --> Reducer --> DBOutPutFormat
 * --> 直接写入到Mysql中
 */
public class NewInstallUsersMapper extends TableMapper<StatsUserDimension, Text> {
	private static final Logger logger = Logger.getLogger(NewInstallUsersMapper.class);

	private byte[] family = EventLogConstants.BYTES_EVENT_LOGS_FAMILY_NAME;
	private StatsUserDimension outputKey = new StatsUserDimension();
	private StatsCommonDimension statsCommonDimension = new StatsCommonDimension();

	private Text outputValue = new Text();

	private KpiDimension newInstallUserDimension = new KpiDimension(KpiType.NEW_INSTALL_USER.name);
	private KpiDimension browserNewInstallUserDimension = new KpiDimension(KpiType.BROWSER_NEW_INSTALL_USER.name);

	private long startdateL, endOfDateL;
	private long firstDateOfThisWeek, endDateOfThisWeek;
	private long firstDateOfThisMonth, endDateOfThisMonth;

	private BrowserDimension browserDimension = new BrowserDimension("", "");

	@Override
	protected void setup(Mapper<ImmutableBytesWritable, Result, StatsUserDimension, Text>.Context context)
			throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		String date = conf.get(GlobalConstants.RUNNING_DATE_PARAMES);
		startdateL = TimeUtil.parseNginxServerTime2Long(date);
		endOfDateL = startdateL + GlobalConstants.DAY_OF_MILLISECONDS;

		firstDateOfThisWeek = TimeUtil.getFirstDayOfThisWeek(startdateL);
		endDateOfThisWeek = TimeUtil.getFirstDayOfNextWeek(startdateL);

		firstDateOfThisMonth = TimeUtil.getFirstDayOfThisMonth(startdateL);
		endDateOfThisMonth = TimeUtil.getFirstDayOfNextMonth(startdateL);

	}

	@Override
	protected void map(ImmutableBytesWritable key, Result value,
			Mapper<ImmutableBytesWritable, Result, StatsUserDimension, Text>.Context context)
			throws IOException, InterruptedException {
		String browser_name = Bytes
				.toString(value.getValue(family, Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_BROWSER_NAME)));
		String browser_version = Bytes
				.toString(value.getValue(family, Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_BROWSER_VERSION)));
		String uuid = Bytes.toString(value.getValue(family, Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_UUID)));
		String platform_name = Bytes
				.toString(value.getValue(family, Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_PLATFORM)));
		String platform_version = Bytes
				.toString(value.getValue(family, Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_VERSION)));
		String server_time = Bytes
				.toString(value.getValue(family, Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_SERVER_TIME)));

		if (StringUtils.isBlank(uuid) || StringUtils.isBlank(platform_name)) {
			logger.debug("异常" + platform_name + "," + uuid);
		}

		long longOfServerTime = 0L;
		try {
			longOfServerTime = Long.valueOf(server_time);
		} catch (NumberFormatException e) {
			logger.debug("时间格式不正确" + server_time);
			return;
		}
		this.outputValue.set(uuid);

		//
		DateDimension dayOfDimension = DateDimension.buildDate(longOfServerTime, DateEnum.DAY);
		DateDimension weekOfDimension = DateDimension.buildDate(longOfServerTime, DateEnum.WEEK);
		DateDimension monthOfDimension = DateDimension.buildDate(longOfServerTime, DateEnum.MONTH);

		// 构建platform唯度
		List<PlatformDimension> platforms = PlatformDimension.buildList(platform_name, platform_version);
		// 构建browser唯度

		List<BrowserDimension> browsers = BrowserDimension.buildList(browser_name, browser_version);

		for (PlatformDimension pd : platforms) {
			this.outputKey.setBrowser(browserDimension);
			this.statsCommonDimension.setPlatform(pd);
			this.statsCommonDimension.setKpi(this.newInstallUserDimension);
			if (longOfServerTime >= startdateL && longOfServerTime <= endOfDateL) {
				this.statsCommonDimension.setDate(dayOfDimension);
				context.write(outputKey, outputValue);
			}
			if (longOfServerTime >= firstDateOfThisWeek && longOfServerTime <= endDateOfThisWeek) {
				this.statsCommonDimension.setDate(weekOfDimension);
				context.write(outputKey, outputValue);

			}
			if (longOfServerTime >= firstDateOfThisMonth && longOfServerTime <= endDateOfThisMonth) {
				this.statsCommonDimension.setDate(monthOfDimension);
				context.write(outputKey, outputValue);
			}

			this.statsCommonDimension.setKpi(this.browserNewInstallUserDimension);
			for (BrowserDimension bd : browsers) {
				this.outputKey.setBrowser(bd);
				context.write(outputKey, outputValue);
			}
		}

	}

}
