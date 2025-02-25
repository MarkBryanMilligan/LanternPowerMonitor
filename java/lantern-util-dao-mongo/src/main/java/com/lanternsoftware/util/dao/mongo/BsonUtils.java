package com.lanternsoftware.util.dao.mongo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lanternsoftware.util.CollectionUtils;

public class BsonUtils {
    private static final Logger LOG = LoggerFactory.getLogger(BsonUtils.class);

    public static Document parse(String _json)
    {
        try
        {
            return Document.parse(_json);
        }
        catch (Exception _e)
        {
            LOG.error("Failed to parse json", _e);
            return null;
        }
    }

    public static String toJson(Document _d)
    {
        try
        {
            if (_d != null)
                return _d.toJson();
        }
        catch (Exception _e)
        {
            LOG.error("Failed to convert bson document to json", _e);
        }
        return null;
    }

    public static String toJson(Collection<Document> _collDocs)
    {
        if (CollectionUtils.isEmpty(_collDocs))
            return "";
        StringBuilder b = null;
        for (Document d : _collDocs)
        {
            if (b == null)
                b = new StringBuilder("[");
            else
                b.append(",");
            b.append(toJson(d));
        }
        b.append("]");
        return b.toString();
    }

    public static byte[] toByteArray(Document _d)
    {
        BsonBinaryWriter writer = null;
        try
        {
            BasicOutputBuffer buffer = new BasicOutputBuffer();
            writer = new BsonBinaryWriter(buffer);
            new DocumentCodec().encode(writer, _d, EncoderContext.builder().build());
            return buffer.toByteArray();
        }
        catch (Throwable _t)
        {
            LOG.error("Failed to convert bson document to a byte array", _t);
            return null;
        }
        finally
        {
            IOUtils.closeQuietly(writer);
        }
    }

    public static Document fromByteArray(byte[] _data)
    {
        if (_data == null)
            return null;
        BsonBinaryReader reader = null;
        try
        {
            reader = new BsonBinaryReader(ByteBuffer.wrap(_data).order(ByteOrder.LITTLE_ENDIAN));
            return new DocumentCodec().decode(reader, DecoderContext.builder().build());
        }
        catch (Throwable _t)
        {
            LOG.error("Failed to convert byte array into bson document", _t);
            return null;
        }
        finally
        {
            IOUtils.closeQuietly(reader);
        }
    }
}
