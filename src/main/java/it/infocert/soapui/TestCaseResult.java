package it.infocert.soapui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.eviware.soapui.model.testsuite.TestRunner;

public class TestCaseResult {

	public static final String SEPARATOR = SoapUiProjectRunner.SEPARATOR;
	public static final DateFormat timeStartFormatter = new SimpleDateFormat("HH:mm:ss.SSS");
	String tCase;
	String tSuite;
	String key;
	String status;
	String reason;
	Long startTime;
	Long elapsed;

	public TestCaseResult(String key, String status) {
		super();
		this.key = key;
		String[] a = key.split(SEPARATOR);
		if (a.length > 1) {
			this.tSuite = a[0];
			this.tCase = a[1];
		} else {
			this.tSuite = a[0];
			this.tCase = "";
		}
		this.status = status;
	}

	public TestCaseResult(String key, String status, Long startTime, Long elapsed) {
		this(key, status);
		this.startTime = startTime;
		this.elapsed = elapsed;
	}

	public TestCaseResult(String key, String status, String reason) {
		this(key, status);
		this.reason = reason;
	}

	public TestCaseResult(String key, String status, Long startTime, Long elapsed, String reason) {
		this(key, status, startTime, elapsed);
		this.reason = reason;
	}
	
	public TestCaseResult(String key, TestRunner runner) {
		this(key, runner.getStatus().toString(), runner.getStartTime(), runner.getTimeTaken(), runner.getReason());
	}
	

	@Override
	public String toString() {
		return String.format("%s / start @ %s / elapsed: %s", getFormattedStatus(), getFormattedStartTime(),
				getFormattedElapsed());
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getReason() {
		return (reason != null ? reason : "");
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getElapsed() {
		return elapsed;
	}

	public void setElapsed(Long elapsed) {
		this.elapsed = elapsed;
	}

	public String getFormattedStatus() {
		if (getReason().isEmpty())
			return status;
		else
			return String.format("%s - %s", status, reason);
	}

	public String getFormattedStartTime() {
		return timeStartFormatter.format(new Date(startTime));
	}

	public String getFormattedElapsed() {
		return String.format("%s ms", elapsed);
	}

}
