package com.lanternsoftware.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ResourceLoader {
    protected static final Logger LOG = LoggerFactory.getLogger(ResourceLoader.class);

    public static String getStringResource(Class clazz, String _sResourceFileName) {
        String sReply = null;
        InputStream stream = null;
        try {
            stream = clazz.getResourceAsStream(_sResourceFileName);
            if (stream != null)
                sReply = IOUtils.toString(stream);
        }
        catch (Exception e) {
            LOG.error("Failed to load resource: " + _sResourceFileName, e);
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
        return sReply == null ? "" : sReply;
    }
    
    public static byte[] getByteArrayResource(Class clazz, String _sResourceFileName) {
        byte[] btReply = null;
        InputStream stream = null;
        try {
            stream = clazz.getResourceAsStream(_sResourceFileName);
            if (stream != null)
                btReply = IOUtils.toByteArray(stream);
        }
        catch (IOException e) {
            LOG.error("Failed to load resource: " + _sResourceFileName, e);
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
        return btReply;
    }

    public static String loadFileAsString(String _fileName) {
        return loadFileAsString(new File(_fileName));
    }

    public static String loadFileAsString(File _file) {
        return NullUtils.toString(loadFile(_file));
    }

    public static List<String> loadFileLines(String _fileName) {
        return loadFileLines(new File(_fileName));
    }

    public static List<String> loadFileLines(File _file) {
        if ((_file == null) || !_file.exists())
            return null;
        FileReader is = null;
        try {
            is = new FileReader(_file);
            BufferedReader reader = new BufferedReader(is);
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null)
            {
                lines.add(line);
            }
            return lines;
        }
        catch (Throwable t) {
            LOG.error("Failed to load file: " + _file.getAbsolutePath(), t);
            return Collections.emptyList();
        }
        finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static byte[] loadFile(String _fileName) {
        return loadFile(new File(_fileName));
    }

    public static byte[] loadFile(File _file) {
        if ((_file == null) || !_file.exists())
            return null;
        InputStream is = null;
        try {
            is = new FileInputStream(_file);
            return IOUtils.toByteArray(is);
        }
        catch (Throwable t) {
            LOG.error("Failed to load file: " + _file.getAbsolutePath(), t);
            return null;
        }
        finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static void writeFile(String _sFile, String _data) {
        writeFile(_sFile, NullUtils.toByteArray(_data));
    }

    public static void writeFile(String _sFile, byte[] _btData) {
        FileOutputStream os = null;
        try {
            if (File.separator.equals("/"))
                _sFile = _sFile.replace("\\", File.separator);
            else
                _sFile = _sFile.replace("/", File.separator);
            int idx = _sFile.lastIndexOf(File.separator);
            new File((idx > 0)?_sFile.substring(0, idx):_sFile).mkdirs();
            os = new FileOutputStream(_sFile, false);
            os.write(_btData);
            os.flush();
            os.getFD().sync();
        }
        catch (Throwable t) {
            LOG.error("Failed to write file: " + _sFile, t);
        }
        finally {
            IOUtils.closeQuietly(os);
        }
    }

    public static void writeFileLines(String _sFile, List<String> _lines) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(_sFile, false);
            for (String line : CollectionUtils.makeNotNull(_lines)) {
                os.write(NullUtils.toByteArray(line));
                os.write((char)10);
            }
        }
        catch (Throwable t) {
            LOG.error("Failed to write file: " + _sFile, t);
        }
        finally {
            IOUtils.closeQuietly(os);
        }
    }

}
