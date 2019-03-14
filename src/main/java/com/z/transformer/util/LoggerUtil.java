package com.z.transformer.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.z.transformer.common.EventLogConstants;

public class LoggerUtil {
	public static final Logger logger = Logger.getLogger(LoggerUtil.class);
	
	
	
	public static Map<String,String> processLog(String log){
		HashMap<String,String> map = new HashMap<String,String>();
		if(StringUtils.isNotBlank(log)){
			String[] splits = log.split(EventLogConstants.LOG_SEPARTIOR);
			if(splits.length == 3){
				String ip = splits[0];			
				map.put(EventLogConstants.LOG_COLUMN_NAME_IP, ip);
				String timeStamp = splits[1];
				long serverTimeL = TimeUtil.parseNginxServerTime2Long(timeStamp);
				String serverTime = String.valueOf(serverTimeL);
				map.put(EventLogConstants.LOG_COLUMN_NAME_SERVER_TIME, serverTime);
				
				String uri = splits[2].trim();
				int index = uri.indexOf("?");
			}
		}
		return map;
	}
	private static void processUri(){
		
	}
}
