package com.lanternsoftware.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ZipUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ZipUtils.class);
    
    public static byte[] zip(byte[] _btData) {
        if (_btData == null)
            return null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream stream = null;
        try {
            stream = new GZIPOutputStream(out);
            stream.write(_btData);
            IOUtils.closeQuietly(stream);
            return out.toByteArray();
        }
        catch (IOException e) {
            IOUtils.closeQuietly(stream);
            LOG.error("Failed to zip data", e);
            return null;
        }
    }
    
    public static byte[] unzip(byte[] _btData) {
        if ((_btData == null) || (_btData.length == 0))
            return null;
        ByteArrayInputStream in = new ByteArrayInputStream(_btData);
        GZIPInputStream stream = null;
        try {
            stream = new GZIPInputStream(in);
            return IOUtils.toByteArray(stream);
        }
        catch (IOException e) {
            LOG.error("Failed to unzip data", e);
            return null;
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }
}
