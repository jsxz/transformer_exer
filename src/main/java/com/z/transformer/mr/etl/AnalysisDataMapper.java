package com.z.transformer.mr.etl;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class AnalysisDataMapper extends Mapper<Object, Text, NullWritable, Put>{}
