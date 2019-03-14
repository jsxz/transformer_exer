package com.z.transformer.mr;

import java.io.IOException;

import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.z.transformer.dimension.key.BaseDimension;
import com.z.transformer.dimension.value.BaseStatsValueWritable;

public class TransformerMySQLOutputFormat extends OutputFormat<BaseDimension, BaseStatsValueWritable> {

	@Override
	public RecordWriter<BaseDimension, BaseStatsValueWritable> getRecordWriter(TaskAttemptContext context)
			throws IOException, InterruptedException {
		return null;
	}

	@Override
	public void checkOutputSpecs(JobContext context) throws IOException, InterruptedException {
		
	}

	@Override
	public OutputCommitter getOutputCommitter(TaskAttemptContext context) throws IOException, InterruptedException {
		return null;
	}}
