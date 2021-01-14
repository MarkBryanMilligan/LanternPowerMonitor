package com.lanternsoftware.util.dao.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.ITransformer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.IDaoSerializer;

public abstract class CSVStream {
    protected static final Logger LOG = LoggerFactory.getLogger(CSVStream.class);

    public static <T> Iterator<T> parse(InputStream _is, Class<T> _class) {
        return new CSVIterator<T>(_is, DaoSerializer.getSerializer(_class));
    }

    public static <T> Iterator<T> parse(InputStream _is, Class<T> _class, DaoEntity _entity) {
        return new CSVIterator<T>(_is, DaoSerializer.getSerializer(_class), _entity);
    }

    public static <T> Iterator<T> parse(InputStream _is, Class<T> _class, ITransformer<String, String> _headerTransformer) {
        return new CSVIterator<T>(_is, DaoSerializer.getSerializer(_class), _headerTransformer);
    }

    public static <T> Iterator<T> parse(InputStream _is, Class<T> _class, DaoEntity _entity, ITransformer<String, String> _headerTransformer) {
        return new CSVIterator<T>(_is, DaoSerializer.getSerializer(_class), _entity, _headerTransformer);
    }

    public static <T> Iterator<T> parse(InputStream _is, IDaoSerializer<T> _serializer) {
        return new CSVIterator<T>(_is, _serializer);
    }

    public static <T> Iterator<T> parse(InputStream _is, IDaoSerializer<T> _serializer, DaoEntity _entity) {
        return new CSVIterator<T>(_is, _serializer, _entity);
    }

    public static <T> Iterator<T> parse(InputStream _is, IDaoSerializer<T> _serializer, ITransformer<String, String> _headerTransformer) {
        return new CSVIterator<T>(_is, _serializer, _headerTransformer);
    }

    public static <T> Iterator<T> parse(InputStream _is, IDaoSerializer<T> _serializer, DaoEntity _entity, ITransformer<String, String> _headerTransformer) {
        return new CSVIterator<T>(_is, _serializer, _entity, _headerTransformer);
    }

    private static class CSVIterator<T> implements Iterator<T> {
        private final BufferedReader reader;
        private final String[] headers;
        private final IDaoSerializer<T> serializer;
        private final DaoEntity metadata;
        private String[] line = null;

        public CSVIterator(InputStream _is, IDaoSerializer<T> _serializer) {
            this(_is, _serializer, null, null);
        }

        public CSVIterator(InputStream _is, IDaoSerializer<T> _serializer, DaoEntity _metadata) {
            this(_is, _serializer, _metadata, null);
        }

        public CSVIterator(InputStream _is, IDaoSerializer<T> _serializer, ITransformer<String, String> _headerTransformer) {
            this(_is, _serializer, null, _headerTransformer);
        }

        public CSVIterator(InputStream _is, IDaoSerializer<T> _serializer, DaoEntity _metadata, ITransformer<String, String> _headerTransformer){
            reader = new BufferedReader(new InputStreamReader(_is));
            headers = line();
            if ((_headerTransformer != null) && (headers != null)) {
                for (int i=0; i<headers.length; i++) {
                    headers[i] = _headerTransformer.transform(headers[i]);
                }
            }
            line = line();
            serializer = _serializer;
            metadata = _metadata;
        }

        @Override
        public boolean hasNext() {
            if (line == null) {
                IOUtils.closeQuietly(reader);
                return false;
            }
            return true;
        }

        @Override
        public T next() {
            DaoEntity entity = new DaoEntity();
            if(metadata!=null){
                Set<Map.Entry<String, Object>> entryset =  metadata.entrySet();
                for (Map.Entry<String, Object> entry : entryset) {
                    entity.put(entry.getKey(), entry.getValue());
                }
            }
            for (int i = 0; i < headers.length; i++) {
                entity.put(headers[i], CollectionUtils.get(line, i));
            }
            T t;
            try {
                t = serializer.fromDaoEntity(entity);
            }
            catch (RuntimeException e) {
                line = line();
                throw e;
            }
            line = line();
            return t;
        }

        @Override
        public void remove() {
        }

        private String[] line() {
            try {
                String line = reader.readLine();
                if (line == null)
                    return null;
                return line.split("\\s*,\\s*");
            } catch (IOException _e) {
                LOG.error("Failed to parse CSV", _e);
                return null;
            }
        }
    }
}
