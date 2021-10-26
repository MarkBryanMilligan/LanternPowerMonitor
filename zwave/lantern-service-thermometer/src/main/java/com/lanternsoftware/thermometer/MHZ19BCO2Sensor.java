package com.lanternsoftware.thermometer;

import com.lanternsoftware.util.concurrency.ConcurrencyUtils;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

public class MHZ19BCO2Sensor implements ICO2Sensor {
	private static final Logger LOG = LoggerFactory.getLogger(MHZ19BCO2Sensor.class);

	private static final int DEFAULT_TIMEOUT = 1000;

	private static final byte[] CMD_GAS_CONCENTRATION = {(byte)0xff, 0x01, (byte)0x86, 0x00, 0x00, 0x00, 0x00, 0x00, 0x79};
	private static final byte[] CMD_CALIBRATE_ZERO_POINT = {(byte)0xff, 0x01, (byte)0x87, 0x00, 0x00, 0x00, 0x00, 0x00, 0x78};
	private static final byte[] CMD_AUTO_CALIBRATION_ON_WITHOUT_CHECKSUM = {(byte)0xff, 0x01, (byte)0x79, (byte)0xa0, 0x00, 0x00, 0x00, 0x00};
	private static final byte[] CMD_AUTO_CALIBRATION_OFF_WITHOUT_CHECKSUM = {(byte)0xff, 0x01, (byte)0x79, 0x00, 0x00, 0x00, 0x00, 0x00};

	private static final int CALIBRATE_SPAN_POINT_MIN = 1000;

	private SerialPort serialPort;
	private InputStream is;
	private OutputStream os;

	private MHZ19BCO2Sensor(String _port) {
		this(_port, DEFAULT_TIMEOUT);
	}

	private MHZ19BCO2Sensor(String _port, int _timeout) {
		try {
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(_port);
			serialPort = portIdentifier.open("co2port", 2000);
			serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			serialPort.enableReceiveTimeout(_timeout);
			serialPort.enableReceiveThreshold(9);
			is = serialPort.getInputStream();
			os = serialPort.getOutputStream();
		} catch (Exception _e) {
			if (serialPort != null) {
				serialPort.close();
				serialPort = null;
			}
			LOG.error("Exception while starting MHZ19BCO2Sensor", _e);
		}
	}

	public void shutdown() {
		IOUtils.closeQuietly(is);
		IOUtils.closeQuietly(os);
		if (serialPort != null)
			serialPort.close();
	}

	private void write(byte[] out) {
		try {
			int length = is.available();
			if (length > 0) {
				byte[] unread = new byte[length];
				int read = is.read(unread, 0, length);
				LOG.debug("deleted unread buffer length:{}", read);
			}
			os.write(out, 0, out.length);
		}
		catch (Exception _e) {
			LOG.error("Exception while writing to MHZ19B", _e);
		}
	}

	private byte getCheckSum(byte[] data) {
		int ret = 0;
		for (int i = 1; i <= 7; i++) {
			ret += data[i];
		}
		return (byte)(~(byte)(ret & 0x000000ff) + 1);
	}

	private byte[] getCommandWithCheckSum(byte[] baseCommand) {
		byte[] checkSum = {getCheckSum(baseCommand)};
		byte[] data = new byte[baseCommand.length + 1];
		System.arraycopy(baseCommand, 0, data, 0, baseCommand.length);
		System.arraycopy(checkSum, 0, data, baseCommand.length, 1);
		return data;
	}

	@Override
	public int getPPM() {
		write(CMD_GAS_CONCENTRATION);
		try {
			ByteBuffer buf = ByteBuffer.allocate(2);
			byte[] data = new byte[9];
			if (is.read(data, 0, 9) < 9)
				return 0;
			buf.put(data[2]);
			buf.put(data[3]);
			return buf.getShort(0);
		}
		catch (Exception _e) {
			LOG.error("Could not read value from MHZ19B", _e);
			return 0;
		}
	}

	public void setCalibrateZeroPoint() {
		write(CMD_CALIBRATE_ZERO_POINT);
	}

	public void setCalibrateSpanPoint(int point) {
		if (point < CALIBRATE_SPAN_POINT_MIN) {
			LOG.info("since span needs at least {} ppm, set it to {} ppm.", CALIBRATE_SPAN_POINT_MIN, CALIBRATE_SPAN_POINT_MIN);
			point = CALIBRATE_SPAN_POINT_MIN;
		}

		byte high = (byte)((point / 256) & 0x000000ff);
		byte low = (byte)((point % 256) & 0x000000ff);
		byte[] CMD_CALIBRATE_SPAN_POINT = {(byte)0xff, 0x01, (byte)0x88, high, low, 0x00, 0x00, 0x00};

		write(getCommandWithCheckSum(CMD_CALIBRATE_SPAN_POINT));
		LOG.info("set the calibration span point to {} ppm.", point);
	}

	public void setAutoCalibration(boolean set) {
		if (set) {
			write(getCommandWithCheckSum(CMD_AUTO_CALIBRATION_ON_WITHOUT_CHECKSUM));
			LOG.info("set auto calibration to ON.");
		} else {
			write(getCommandWithCheckSum(CMD_AUTO_CALIBRATION_OFF_WITHOUT_CHECKSUM));
			LOG.info("set auto calibration to OFF.");
		}
	}

	private void setDetectionRange(int range) {
		byte high = (byte)((range / 256) & 0x000000ff);
		byte low = (byte)((range % 256) & 0x000000ff);
		byte[] CMD_DETECTION_RANGE = {(byte)0xff, 0x01, (byte)0x99, high, low, 0x00, 0x00, 0x00};

		write(getCommandWithCheckSum(CMD_DETECTION_RANGE));
		LOG.info("set the detection range to {} ppm.", range);
	}

	public void setDetectionRange2000() {
		setDetectionRange(2000);
	}

	public void setDetectionRange5000() {
		setDetectionRange(5000);
	}

	public static void main(String[] args) {
		MHZ19BCO2Sensor mhz19b = new MHZ19BCO2Sensor("/dev/ttyAMA0");
		mhz19b.setDetectionRange5000();
		mhz19b.setAutoCalibration(false);
		AtomicInteger i = new AtomicInteger(0);
		while (i.incrementAndGet() < 2000) {
			LOG.debug("co2: {}PPM", mhz19b.getPPM());
			ConcurrencyUtils.sleep(5000);
		}
		Runtime.getRuntime().addShutdownHook(new Thread(mhz19b::shutdown, "Shutdown"));
	}
}
