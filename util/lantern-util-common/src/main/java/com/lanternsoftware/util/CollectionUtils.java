package com.lanternsoftware.util;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.RandomAccess;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CollectionUtils {
    public static <T> T getFirst(Collection<T> _collObjects) {
        if ((_collObjects != null) && !_collObjects.isEmpty())
            return _collObjects.iterator().next();
        return null;
    }

    public static <T> T removeFirst(Collection<T> _objects) {
        if (_objects == null)
            return null;
        Iterator<T> iter = _objects.iterator();
        if (iter.hasNext()) {
            T t = iter.next();
            iter.remove();
            return t;
        }
        return null;
    }

    public static <T> T removeOne(Collection<T> _coll, IQualifier<T> _qualifier) {
        if ((_coll == null) || (_qualifier == null))
            return null;
        Iterator<T> iter = _coll.iterator();
        while (iter.hasNext()) {
            T t = iter.next();
            if (_qualifier.qualifies(t)) {
                iter.remove();
                return t;
            }
        }
        return null;
    }

    public static <T> List<T> removeAll(Collection<T> _coll, IQualifier<T> _qualifier) {
        if ((_coll == null) || (_qualifier == null))
            return null;
        List<T> ret = new ArrayList<>();
        Iterator<T> iter = _coll.iterator();
        while (iter.hasNext()) {
            T t = iter.next();
            if (_qualifier.qualifies(t)) {
                iter.remove();
                ret.add(t);
            }
        }
        return ret;
    }

    public static <T> T getLast(Collection<T> _collObjects) {
        if ((_collObjects == null) || _collObjects.isEmpty())
            return null;
        if (_collObjects instanceof RandomAccess) {
            List<T> listObjects = (List<T>) _collObjects;
            return listObjects.get(listObjects.size() - 1);
        }
        if (_collObjects instanceof Deque) {
            Deque<T> listObjects = (Deque<T>) _collObjects;
            return listObjects.getLast();
        }
        T t = null;
        Iterator<T> iter = _collObjects.iterator();
        while (iter.hasNext()) {
            t = iter.next();
        }
        return t;
    }

    public static <T> T getFirst(T[] _arrObjects) {
        if (size(_arrObjects) > 0)
            return _arrObjects[0];
        return null;
    }

    public static <T> boolean isEmpty(Collection<T> _collObjects) {
        return (_collObjects == null) || _collObjects.isEmpty();
    }

    public static <T> boolean isNotEmpty(Collection<T> _collObjects) {
        return (_collObjects != null) && !_collObjects.isEmpty();
    }

    public static <T,V> boolean isEmpty(Map<T,V> _map) {
        return (_map == null) || _map.isEmpty();
    }

    public static <T,V> boolean isNotEmpty(Map<T,V> _map) {
        return (_map != null) && !_map.isEmpty();
    }

    public static <T> Collection<T> makeNotNull(Collection<T> _collObjects) {
        if (_collObjects != null)
            return _collObjects;
        return new ArrayList<T>();
    }

    public static <T> List<T> makeNotNull(List<T> _listObjects) {
        if (_listObjects != null)
            return _listObjects;
        return new ArrayList<T>();
    }

    public static <T, V> Map<T, V> makeNotNull(Map<T, V> _mapObjects) {
        if (_mapObjects != null)
            return _mapObjects;
        return new HashMap<T, V>();
    }

    public static int size(Collection<?> _collObjects) {
        if (_collObjects == null)
            return 0;
        return _collObjects.size();
    }

    public static <T,V> int size(Map<T, V> _collObjects) {
        if (_collObjects == null)
            return 0;
        return _collObjects.size();
    }

    public static <T> int size(T[] _arr) {
        if (_arr == null)
            return 0;
        return _arr.length;
    }

    public static <T> T get(List<T> _list, int _idx) {
        if (_list == null)
            return null;
        if ((_idx < 0) || (_idx >= _list.size()))
            return null;
        return _list.get(_idx);
    }

    public static <T> T get(T[] _t, int _idx) {
        if (_t == null)
            return null;
        if ((_idx < 0) || (_idx >= _t.length))
            return null;
        return _t[_idx];
    }

    public static <T, V> List<V> get(Map<T, V> _map, Collection<T> _keys) {
        if (_keys == null)
            return null;
        List<V> ret = new ArrayList<>();
        for (T t : _keys) {
            ret.add(_map.get(t));
        }
        return ret;
    }

    public static <T> T last(T[] _arr) {
        if (_arr == null || _arr.length == 0)
            return null;
        return _arr[_arr.length - 1];
    }

    public static <T> boolean isEqual(Collection<T> _l1, Collection<T> _l2) {
        if (size(_l1) != size(_l2))
            return false;
        Iterator<T> i1 = _l1.iterator();
        Iterator<T> i2 = _l2.iterator();
        while (i1.hasNext()) {
            if (NullUtils.isNotEqual(i1.next(), i2.next()))
                return false;
        }
        return true;
    }

    public static <T extends IIdentical<T>> boolean isIdentical(Collection<T> _l1, Collection<T> _l2) {
        if (size(_l1) != size(_l2))
            return false;
        Iterator<T> i1 = _l1.iterator();
        Iterator<T> i2 = _l2.iterator();
        while (i1.hasNext()) {
            if (NullUtils.isNotIdentical(i1.next(), i2.next()))
                return false;
        }
        return true;
    }

    public static <T> boolean contains(Collection<T> _coll, T _t) {
        if (_coll == null)
            return false;
        return _coll.contains(_t);
    }

    public static <T> boolean containsAny(Collection<T> _coll, T... _t) {
        return containsAny(_coll, asArrayList(_t));
    }

    public static <T> boolean containsAny(Collection<T> _coll, Collection<T> _values) {
        if (size(_values) == 0)
            return false;
        for (T t : _values) {
            if (contains(_coll, t))
                return true;
        }
        return false;
    }

    public static <T> boolean containsAll(Collection<T> _coll, T... _t) {
        return containsAll(_coll, asArrayList(_t));
    }

    public static <T> boolean containsAll(Collection<T> _coll, Collection<T> _values) {
        if (size(_values) == 0)
            return true;
        for (T t : _values) {
            if (!contains(_coll, t))
                return false;
        }
        return true;
    }

    public static <T> boolean containsNone(Collection<T> _coll, T... _t) {
        return containsNone(_coll, asArrayList(_t));
    }

    public static <T> boolean containsNone(Collection<T> _coll, Collection<T> _values) {
        if (size(_values) == 0)
            return true;
        for (T t : _values) {
            if (contains(_coll, t))
                return false;
        }
        return true;
    }

    public static <T> List<T> merge(Collection<List<T>> _colls) {
        List<T> list = new ArrayList<>();
        for (List<? extends T> coll : makeNotNull(_colls)) {
            list.addAll(coll);
        }
        return list;
    }

    public static <T> List<T> merge(Collection<? extends T> _coll1, Collection<? extends T> _coll2) {
        List<T> list = new ArrayList<>(makeNotNull(_coll1));
        list.addAll(makeNotNull(_coll2));
        return list;
    }

    public static <T, V> List<V> aggregate(Collection<T> _coll, IAggregator<T, V> _aggregator) {
        List<V> list = new ArrayList<>();
        for (T t : makeNotNull(_coll)) {
            List<V> vs = _aggregator.aggregate(t);
            if (vs != null)
                list.addAll(vs);
        }
        return list;
    }

    public static <T, V, U> Map<U, V> aggregateToMap(Collection<T> _coll, IAggregator<T, V> _aggregator, ITransformer<V, U> _keyTransformer) {
        return transformToMap(aggregate(_coll, _aggregator), _keyTransformer);
    }

    public static byte[] merge(byte[]... _arrs) {
        int iSize = 0;
        for (byte[] curArr : _arrs) {
            if (curArr != null)
                iSize += curArr.length;
        }
        byte[] arr = new byte[iSize];
        int offset = 0;
        for (byte[] curArr : _arrs) {
            if (curArr == null)
                continue;
            System.arraycopy(curArr, 0, arr, offset, curArr.length);
            offset += curArr.length;
        }
        return arr;
    }

    public static <T, V> List<V> getMultiMapList(T _key, Map<T, List<V>> _map) {
        List<V> list = _map.get(_key);
        if (list == null) {
            list = new ArrayList<>();
            _map.put(_key, list);
        }
        return list;
    }

    public static <T, V> Set<V> getMultiMapSet(T _key, Map<T, Set<V>> _map) {
        Set<V> set = _map.get(_key);
        if (set == null) {
            set = new HashSet<>();
            _map.put(_key, set);
        }
        return set;
    }

    public static <T, V> List<V> addToMultiMap(T _key, V _value, Map<T, List<V>> _map) {
        List<V> list = getMultiMapList(_key, _map);
        list.add(_value);
        return list;
    }

    public static <T, V> Set<V> addToMultiMapSet(T _key, V _value, Map<T, Set<V>> _map) {
        Set<V> set = getMultiMapSet(_key, _map);
        set.add(_value);
        return set;
    }

    public static int sumIntegers(Collection<Integer> _coll) {
        int sum = 0;
        for (Integer val : makeNotNull(_coll)) {
            if (val != null)
                sum += val;
        }
        return sum;
    }

    public static Double sum(Collection<Double> _coll) {
        double sum = 0.0;
        for (Double val : makeNotNull(_coll)) {
            if (val != null)
                sum += val;
        }
        return sum;
    }

    public static Double mean(Collection<Double> _coll) {
        int cnt = 0;
        double total = 0.0;
        for (Double val : makeNotNull(_coll)) {
            if (val != null) {
                cnt++;
                total += val;
            }
        }
        if (cnt == 0)
            return 0.0;
        return total / cnt;
    }

    public static Double variance(Collection<Double> _coll) {
        double mean = mean(_coll);
        int cnt = 0;
        double total = 0.0;
        for (Double val : makeNotNull(_coll)) {
            if (val != null) {
                cnt++;
                total += (val - mean) * (val - mean);
            }
        }
        if (cnt == 0)
            return 0.0;
        return total / cnt;
    }

    public static Double standardDeviation(Collection<Double> _coll) {
        return Math.sqrt(variance(_coll));
    }

    public static <T> List<List<T>> split(List<T> _list, int _size) {
        if (_list == null)
            return Collections.emptyList();
        int iPieces = (_list.size() / _size) + 1;
        List<List<T>> list = new ArrayList<>(iPieces);
        for (int i = 0; i < iPieces; i++) {
            list.add(_list.subList(i * _size, Math.min(_list.size(), (i + 1) * _size)));
        }
        return list;
    }

    public static <T> List<List<T>> splitEvenly(List<T> _list, int _maxSize) {
        if (isEmpty(_list))
            return Collections.emptyList();
        return splitIntoPieces(_list, ((_list.size()-1) / _maxSize) + 1);
    }

    public static <T> List<List<T>> splitIntoPieces(List<T> _list, int _pieces) {
        return splitIntoPieces(_list, _pieces, false);
    }

    public static <T> List<List<T>> splitIntoPieces(List<T> _list, int _pieces, boolean _createNewLists) {
        if (isEmpty(_list))
            return Collections.emptyList();
        if (_list.size() < _pieces)
            return Collections.singletonList(_list);
        int size = (int)Math.ceil(((double)_list.size())/_pieces);
        List<List<T>> list = new ArrayList<>(_pieces);
        int offset = 0;
        while (offset < _list.size()) {
            List<T> subList = _list.subList(offset, Math.min(_list.size(), offset+size));
            list.add(_createNewLists?new ArrayList<>(subList):subList);
            offset += size;
        }
        return list;
    }

    public static <T> ArrayList<T> asArrayList(T... _values) {
        if (_values == null)
            return new ArrayList<>(0);
        ArrayList<T> list = new ArrayList<>(_values.length);
        for (T t : _values)
            list.add(t);
        return list;
    }

    public static <T> HashSet<T> asHashSet(T... _values) {
        HashSet<T> setValues = new HashSet<>();
        if (_values == null)
            return setValues;
        for (T t : _values)
            setValues.add(t);
        return setValues;
    }

    public static <K, V> HashMap<K, V> asHashMap(K _key, V _value) {
        HashMap<K, V> map = new HashMap<>();
        map.put(_key, _value);
        return map;
    }

    public static <T> ArrayList<T> asArrayList(Iterable<T> _iterable) {
        if (_iterable == null)
            return new ArrayList<T>(0);
        ArrayList<T> list = new ArrayList<>();
        for (T t : _iterable)
            list.add(t);
        return list;
    }

    public static <T> ArrayList<T> asArrayList(Iterator<T> _iter) {
        if (_iter == null)
            return new ArrayList<T>(0);
        ArrayList<T> list = new ArrayList<>();
        while (_iter.hasNext())
            list.add(_iter.next());
        return list;
    }

    public static <T> HashSet<T> asHashSet(Iterable<T> _iterable) {
        HashSet<T> setValues = new HashSet<>();
        if (_iterable == null)
            return setValues;
        for (T t : _iterable)
            setValues.add(t);
        return setValues;
    }

    public static <T> HashSet<T> asHashSet(Iterator<T> _iter) {
        HashSet<T> setValues = new HashSet<>();
        if (_iter == null)
            return setValues;
        while (_iter.hasNext())
            setValues.add(_iter.next());
        return setValues;
    }

    public static <T> boolean allQualify(Collection<T> _coll, IQualifier<T> _qualifier) {
        if ((_coll == null) || (_qualifier == null))
            return false;
        for (T t : _coll) {
            if ((t == null) || !_qualifier.qualifies(t))
                return false;
        }
        return true;
    }

    public static <T> boolean anyQualify(Collection<T> _coll, IQualifier<T> _qualifier) {
        if ((_coll == null) || (_qualifier == null))
            return false;
        for (T t : _coll) {
            if ((t != null) && _qualifier.qualifies(t))
                return true;
        }
        return false;
    }

    public static <T> boolean noneQualify(Collection<T> _coll, IQualifier<T> _qualifier) {
        if ((_coll == null) || (_qualifier == null))
            return true;
        for (T t : _coll) {
            if ((t != null) && _qualifier.qualifies(t))
                return false;
        }
        return true;
    }

    public static <T> List<T> filter(Collection<? extends T> _coll, IFilter<T> _filter) {
        if ((_coll == null) || (_filter == null))
            return new ArrayList<>();
        List<T> listValues = new ArrayList<>();
        for (T t : _coll) {
            if (_filter.isFiltered(t))
                listValues.add(t);
        }
        return listValues;
    }

    public static <T> T filterOne(Collection<? extends T> _coll, IFilter<T> _filter) {
        if ((_coll == null) || (_filter == null))
            return null;
        for (T t : _coll) {
            if (_filter.isFiltered(t))
                return t;
        }
        return null;
    }

    public static <T> int indexOf(List<? extends T> _list, IQualifier<T> _qual) {
        if ((_list == null) || (_qual == null))
            return -1;
        int i = 0;
        for (T t : _list) {
            if (_qual.qualifies(t))
                return i;
            i++;
        }
        return -1;
    }

    public static <T> void filterMod(Iterable<? extends T> _iterable, IFilter<T> _filter) {
        if ((_iterable == null) || (_filter == null))
            return;
        Iterator<? extends T> iter = _iterable.iterator();
        while (iter.hasNext()) {
            if (!_filter.isFiltered(iter.next()))
                iter.remove();
        }
    }
    
    public static <T, V> List<V> filterToType(Iterable<? extends T> _iterable, Class<V> _class) {
        List<V> list = new ArrayList<>();
        if (_iterable == null)
            return list;
        for (T t : _iterable) {
            if (_class.isInstance(t))
                list.add(_class.cast(t));
        }
        return list;
    }

    public static <T> void edit(Iterable<T> _coll, IEditor<T> _editor) {
        if ((_coll == null) || (_editor == null))
            return;
        for (T t : _coll) {
            _editor.edit(t);
        }
    }

    public static <T, V> List<V> transform(Collection<T> _coll, ITransformer<? super T, V> _transformer) {
        return transform(_coll, _transformer, false);
    }

    public static <T, V> List<V> transform(Iterable<T> _iter, ITransformer<? super T, V> _transformer) {
        return transform(_iter, _transformer, false);
    }

    public static <T, V> List<V> transform(Iterable<T> _iter, ITransformer<? super T, V> _transformer, boolean _excludeNulls) {
        if ((_iter == null) || (_transformer == null))
            return new ArrayList<>();
        List<V> listValues = new ArrayList<>();
        for (T t : _iter) {
            if (_excludeNulls && (t == null))
                continue;
            V v = _transformer.transform(t);
            if (!_excludeNulls || (v != null))
                listValues.add(v);
        }
        return listValues;
    }

    public static <T, V> List<V> transform(Collection<T> _coll, ITransformer<? super T, V> _transformer, boolean _excludeNulls) {
        if ((_coll == null) || (_transformer == null))
            return new ArrayList<>();
        List<V> listValues = new ArrayList<>(_coll.size());
        for (T t : _coll) {
            V v = _transformer.transform(t);
            if (!_excludeNulls || (v != null))
                listValues.add(v);
        }
        return listValues;
    }

    @SuppressWarnings("unchecked")
    public static <T, V> V[] transform(T[] _coll, ITransformer<? super T, V> _transformer, Class<V> _destType) {
        V[] ret = (V[])Array.newInstance(_destType, size(_coll));
        for (int i=0; i < size(_coll); i++) {
            ret[i] = _transformer.transform(_coll[i]);
        }
        return ret;
    }

    public static <T, V> Map<V, T> transformToMap(Collection<T> _coll, ITransformer<? super T, V> _transformer) {
        Map<V, T> mapValues = new HashMap<>();
        if ((_coll == null) || (_transformer == null))
            return mapValues;
        for (T t : _coll) {
            V v = _transformer.transform(t);
            if (v != null)
                mapValues.put(v, t);
        }
        return mapValues;
    }

    public static <T, V, U> Map<V, U> transformToMap(Collection<T> _coll, ITransformer<? super T, V> _keyTrans, ITransformer<? super T, U> _valTrans) {
        Map<V, U> mapValues = new HashMap<>();
        if ((_coll == null) || (_keyTrans == null) || (_valTrans == null))
            return mapValues;
        for (T t : _coll) {
            V v = _keyTrans.transform(t);
            U u = _valTrans.transform(t);
            if ((v != null) && (u != null))
                mapValues.put(v, u);
        }
        return mapValues;
    }

    public static <T, V> Map<V, List<T>> transformToMultiMap(Collection<T> _coll, ITransformer<? super T, V> _transformer) {
        Map<V, List<T>> mapValues = new HashMap<>();
        if ((_coll == null) || (_transformer == null))
            return mapValues;
        for (T t : _coll) {
            V v = _transformer.transform(t);
            if (v != null)
                addToMultiMap(v, t, mapValues);
        }
        return mapValues;
    }

    public static <T, V, U> Map<V, List<U>> transformToMultiMap(Collection<T> _coll, ITransformer<? super T, V> _keyTrans, ITransformer<? super T, U> _valTrans) {
        Map<V, List<U>> mapValues = new HashMap<>();
        if ((_coll == null) || (_keyTrans == null) || (_valTrans == null))
            return mapValues;
        for (T t : _coll) {
            V v = _keyTrans.transform(t);
            U u = _valTrans.transform(t);
            if ((v != null) && (u != null))
                addToMultiMap(v, u, mapValues);
        }
        return mapValues;
    }

    public static <T,V> void addAllToMap(Collection<T> _coll, ITransformer<? super T, V> _keyTrans, Map<V, T> _map) {
        for (T t : makeNotNull(_coll)) {
            V v = _keyTrans.transform(t);
            if (v != null)
                _map.put(v, t);
        }
    }

    public static <T,V> void addAllToMultiMap(Collection<T> _coll, ITransformer<? super T, V> _keyTrans, Map<V, List<T>> _map) {
        for (T t : makeNotNull(_coll)) {
            V v = _keyTrans.transform(t);
            if (v != null)
                addToMultiMap(v, t, _map);
        }
    }

    public static <T> String transformToCommaSeparated(Collection<T> _coll, ITransformer<? super T, String> _transformer) {
        return transformToCommaSeparated(_coll, _transformer, false);
    }

    public static <T> String transformToCommaSeparated(Collection<T> _coll, ITransformer<? super T, String> _transformer, boolean _spaceAfterComma) {
        if (_transformer == null)
            return null;
        return commaSeparated(transform(_coll, _transformer), _spaceAfterComma);
    }

    public static <T> String transformAndDelimit(Collection<T> _coll, ITransformer<? super T, String> _transformer, String _delimiter) {
        return transformAndDelimit(_coll, _transformer, _delimiter, false);
    }

    public static <T> String transformAndDelimit(Collection<T> _coll, ITransformer<? super T, String> _transformer, String _delimiter, boolean _discardEmptyValues) {
        if (_transformer == null)
            return null;
        return delimit(transform(_coll, _transformer), _delimiter, _discardEmptyValues);
    }

    public static String commaSeparated(Collection<String> _values) {
        return commaSeparated(_values, false);
    }

    public static String commaSeparated(Collection<String> _values, boolean _spaceAfterComma) {
        return delimit(_values, _spaceAfterComma ? ", " : ",");
    }

    public static String delimit(Collection<String> _values, String _delimiter) {
        return delimit(_values, _delimiter, false);
    }

    public static String delimit(Collection<String> _values, String _delimiter, boolean _discardEmptyValues) {
        StringBuilder builder = null;
        for (String value : makeNotNull(_values)) {
            if (_discardEmptyValues && NullUtils.isEmpty(value))
                continue;
            if (builder == null)
                builder = new StringBuilder();
            else
                builder.append(_delimiter);
            builder.append(value);
        }
        if (builder != null)
            return builder.toString();
        return null;
    }

    public static List<String> undelimit(String _value, String _delimiter) {
        return undelimit(_value, _delimiter, true);
    }

    public static List<String> undelimit(String _value, String _delimiter, boolean _discardEmptyValues) {
        if (_value == null)
            return new ArrayList<>();
        return asArrayList(_discardEmptyValues?NullUtils.cleanSplit(_value, _delimiter): _value.split(_delimiter));
    }

    public static <T, V> Set<V> transformToSet(Collection<T> _coll, ITransformer<T, V> _transformer) {
        Set<V> setValues = new HashSet<V>();
        if ((_coll == null) || (_transformer == null))
            return setValues;
        for (T t : _coll) {
            if (t != null) {
                V v = _transformer.transform(t);
                if (v != null)
                    setValues.add(v);
            }
        }
        return setValues;
    }

    public static <T extends Comparable<T>> T getSmallest(Collection<T> _collObjects)
    {
        return getSmallest(_collObjects, new Comparator<T>()
        {
            @Override
            public int compare(T o1, T o2)
            {
                return NullUtils.compare(o1, o2);
            }
        });
    }

    public static <T> T getSmallest(Collection<T> _objects, Comparator<T> _comparator)
    {
        if (_objects == null)
            return null;
        T ret = null;
        for (T t : _objects)
        {
            if (t == null)
                continue;
            if ((ret == null) || (_comparator.compare(t, ret) < 0))
                ret = t;
        }
        return ret;
    }

    public static <T extends Comparable<T>> List<T> getAllSmallest(Collection<T> _collObjects)
    {
        return getAllSmallest(_collObjects, new Comparator<T>()
        {
            @Override
            public int compare(T o1, T o2)
            {
                return NullUtils.compare(o1, o2);
            }
        });
    }

    public static <T> List<T> getAllSmallest(Collection<T> _objects, Comparator<T> _comparator)
    {
        final List<T> ret = new ArrayList<>();
        if (_objects == null)
            return ret;
        for (T t : _objects) {
            if (t == null)
                continue;
            if (ret.isEmpty())
                ret.add(t);
            else {
                int comp = _comparator.compare(t, CollectionUtils.getFirst(ret));
                if (comp == 0)
                    ret.add(t);
                else if (comp < 0) {
                    ret.clear();
                    ret.add(t);
                }
            }
        }
        return ret;
    }

    public static <T extends Comparable<T>> T getLargest(Collection<T> _collObjects)
    {
        return getLargest(_collObjects, new Comparator<T>()
        {
            @Override
            public int compare(T o1, T o2)
            {
                return NullUtils.compare(o1, o2);
            }
        });
    }

    public static <T> T getLargest(Collection<T> _objects, Comparator<T> _comparator)
    {
        if (_objects == null)
            return null;
        T ret = null;
        for (T t : _objects)
        {
            if (t == null)
                continue;
            if ((ret == null) || (_comparator.compare(t, ret) > 0))
                ret = t;
        }
        return ret;
    }

    public static <T> List<T> getSmallest(Collection<T> _objects, Comparator<T> _comparator, int _count)
    {
        return getSmallest(_objects, _comparator, _count, null);
    }

    public static <T> List<T> getSmallest(Collection<T> _objects, Comparator<T> _comparator, int _count, IFilter<T> _filter)
    {
        if (_objects == null)
            return null;
        if (_count * 4 > _objects.size())
        {
            List<T> items = new ArrayList<T>();
            for (T t : _objects)
            {
                if ((_filter == null) || !_filter.isFiltered(t))
                    items.add(t);
            }
            Collections.sort(items, _comparator);
            return subList(items, 0, _count);
        }
        TreeMap<T, List<T>> mapReturn = new TreeMap<>(_comparator);
        for (T t : _objects)
        {
            if ((t == null) || ((_filter != null) && _filter.isFiltered(t)))
                continue;
            if (mapReturn.size() < _count)
                addToMultiMap(t, t, mapReturn);
            else
            {
                Iterator<T> iter = mapReturn.descendingKeySet().iterator();
                if (_comparator.compare(t, iter.next()) < 0)
                {
                    iter.remove();
                    addToMultiMap(t, t, mapReturn);
                }
            }
        }
        List<T> items = new ArrayList<T>(_count);
        for (List<T> list : mapReturn.values())
        {
            items.addAll(list);
        }
        return subList(items, 0, _count);
    }

    public static <T> List<T> subList(List<T> _list, int _fromIndex, int _toIndex) {
        if ((_list == null) || (_fromIndex > _list.size() - 1))
            return new ArrayList<T>();
        return _list.subList(_fromIndex, Math.min(_toIndex, _list.size()));
    }

    public static <T extends Comparable<T>> T mostCommon(Collection<T> _collObjects) {
        return mostCommon(_collObjects, new Comparator<T>() {
            @Override
            public int compare(T _o1, T _o2) {
                return NullUtils.compare(_o1, _o2);
            }
        });
    }

    public static <T> T mostCommon(Collection<T> _collObjects, Comparator<? super T> _comparator) {
        int iMax = 0;
        Map<T, AtomicInteger> mapCounts = new TreeMap<T, AtomicInteger>(_comparator);
        for (T t : makeNotNull(_collObjects)) {
            AtomicInteger i = mapCounts.get(t);
            if (i == null) {
                mapCounts.put(t, new AtomicInteger(1));
                if (iMax == 0)
                    iMax = 1;
            } else {
                if (i.incrementAndGet() > iMax)
                    iMax = i.intValue();
            }
        }
        for (Entry<T, AtomicInteger> e : mapCounts.entrySet()) {
            if (e.getValue().intValue() == iMax)
                return e.getKey();
        }
        return null;
    }

    public static <T,V> List<V> getAll(Map<T, V> _map, Collection<T> _keys) {
        List<V> ret = new ArrayList<>();
        if (_map == null)
            return ret;
        for (T t : makeNotNull(_keys)) {
            V v = _map.get(t);
            if (v != null)
                ret.add(v);
        }
        return ret;
    }

    public static byte[] toByteArray(Collection<Integer> _integers) {
        if (isEmpty(_integers))
            return null;
        ByteBuffer bb = ByteBuffer.allocate(_integers.size() * 4);
        for (Integer i : _integers) {
            bb.putInt(i);
        }
        return bb.array();
    }

    public static List<Integer> fromByteArrayOfIntegers(byte[] _btIntegers) {
        if (length(_btIntegers) > 0) {
            List<Integer> auxIds = new ArrayList<>(_btIntegers.length / 4);
            ByteBuffer bb = ByteBuffer.wrap(_btIntegers);
            while (bb.hasRemaining()) {
                auxIds.add(bb.getInt());
            }
            return auxIds;
        }
        return new ArrayList<>();
    }

    public static int length(byte[] _arr)
    {
        if (_arr == null)
            return 0;
        return _arr.length;
    }
}
