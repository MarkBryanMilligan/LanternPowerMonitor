package com.lanternsoftware.pigpio;

import com.lanternsoftware.util.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PIGPIO {
    protected static final Logger LOG = LoggerFactory.getLogger(PIGPIO.class);

	private PIGPIO() {
	}

	static {
		try {
			String osArch = System.getProperty("os.arch").toLowerCase();
			if (osArch.equals("arm"))
				osArch = "armhf";
			String path = "/lib/" + osArch + "/lantern-pigpio.so";
			byte[] file = ResourceLoader.getByteArrayResource(PIGPIO.class, path);
			LOG.info("library size: {}", file.length);
			String libPath = "/opt/currentmonitor/lantern-pigpio.so";
			ResourceLoader.writeFile(libPath, file);
			System.load(libPath);
		} catch (Exception _e) {
            LOG.error("Failed to load lantern-pigpio.so from resource", _e);
		}
	}

	public static native int gpioInitialise();

	public static native void gpioTerminate();

	public static native int gpioSetMode(int gpio, int mode);

	public static native int gpioGetMode(int gpio);

	public static native int gpioSetPullUpDown(int gpio, int pud);

	public static native int gpioRead(int gpio);

	public static native int gpioWrite(int gpio, int level);

	public static native int spiOpen(int spiChan, int baud, int spiFlags);

	public static native int spiClose(int handle);

	public static native int spiRead(int handle, byte[] buf, int offset, int count);

	public static native int spiWrite(int handle, byte[] buf, int offset, int count);

	public static native int spiXfer(int handle, byte[] txBuf, int txOffset, byte[] rxBuf, int rxOffset, int count);
}
