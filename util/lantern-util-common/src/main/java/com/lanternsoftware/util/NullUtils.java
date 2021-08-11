package com.lanternsoftware.util;

import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NullUtils {
    public static boolean isNotEqual(Object a, Object b) {
        return !isEqual(a, b);
    }

    public static boolean isEqual(Object a, Object b) {
        if (a != null)
            return (b != null) && a.equals(b);
        return (b == null);
    }

    public static <T extends IIdentical<T>> boolean isNotIdentical(T a, T b) {
        return !isIdentical(a, b);
    }

    public static <T extends IIdentical<T>> boolean isIdentical(T a, T b) {
        if (a != null)
            return (b != null) && a.isIdentical(b);
        return (b == null);
    }

    public static <T> boolean isNotEqual(T a, T b, IEquals<T> _equals) {
        return !isEqual(a, b, _equals);
    }

    public static <T> boolean isEqual(T a, T b, IEquals<T> _equals) {
        if (a != null)
            return (b != null) && _equals.equals(a, b);
        return (b == null);
    }

    public static boolean equalsIgnoreCase(String a, String b) {
        if (a != null)
            return a.equalsIgnoreCase(b);
        return (b == null);
    }

    public static int length(String _val) {
        if (_val == null)
            return 0;
        return _val.length();
    }

    public static boolean isEmpty(String _sVal) {
        return (_sVal == null) || (_sVal.length() == 0);
    }

    public static boolean isAnyEmpty(String... _vals) {
        if (_vals == null)
            return true;
        for (String val : _vals) {
            if (isEmpty(val))
                return true;
        }
        return false;
    }

    public static boolean isNotEmpty(String _sVal) {
        return !isEmpty(_sVal);
    }

    public static boolean isAnyNotEmpty(String... _vals) {
        if (_vals == null)
            return false;
        for (String val : _vals) {
            if (isNotEmpty(val))
                return true;
        }
        return false;
    }

    public static boolean isAnyNull(Object... _o) {
        if ((_o == null) || (_o.length == 0))
            return false;
        for (Object o : _o) {
            if (o == null)
                return true;
        }
        return false;
    }

    public static boolean isOneOf(Object _o, Object... _values) {
        if ((_o == null) || (_values == null) || (_values.length == 0))
            return false;
        for (Object o : _values) {
            if (_o.equals(o))
                return true;
        }
        return false;
    }

    public static String trim(String _val) {
        if (_val == null)
            return null;
        return _val.trim();
    }

    public static String toString(byte[] _arrBytes) {
        if (_arrBytes == null)
            return null;
        try {
            return new String(_arrBytes, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static byte[] toByteArray(String _value) {
        if (_value == null)
            return null;
        try {
            return _value.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static int toInteger(String _value) {
        try {
            return Integer.valueOf(makeNotNull(_value));
        }
        catch (NumberFormatException _e) {
            return 0;
        }
    }

    public static long toLong(String _value) {
        try {
            return Long.valueOf(makeNotNull(_value));
        }
        catch (NumberFormatException _e) {
            return 0;
        }
    }

    public static double toDouble(String _value) {
        try {
            return Double.valueOf(makeNotNull(_value));
        }
        catch (NumberFormatException _e) {
            return 0.0;
        }
    }

    public static float toFloat(String _value) {
        try {
            return Float.valueOf(makeNotNull(_value));
        }
        catch (NumberFormatException _e) {
            return 0f;
        }
    }

    public static String urlEncode(String _url) {
        try {
            return URLEncoder.encode(makeNotNull(_url), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            return _url;
        }
    }

    public static String urlDecode(String _url) {
        try {
            return URLDecoder.decode(makeNotNull(_url), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            return _url;
        }
    }

    public static String makeNotNull(String _value) {
        if (_value != null)
            return _value;
        return "";
    }

    public static String after(String _value, String _search) {
        if (_value == null)
            return "";
        int iPos = _value.lastIndexOf(_search);
        if (iPos < 0)
            return "";
        return iPos < _value.length() - _search.length() ? _value.substring(iPos + _search.length()) : "";
    }

    public static <T extends Enum<T>> T toEnum(Class<T> _enumType, String _sValue) {
        return toEnum(_enumType, _sValue, null);
    }

    public static <T extends Enum<T>> T toEnum(Class<T> _enumType, String _sValue, T _default) {
        T e = null;
        try {
            e = Enum.valueOf(_enumType, _sValue);
        }
        catch (Throwable t) {
            return _default;
        }
        return e;
    }

    public static <T extends Enum<T>> List<T> toEnums(Class<T> _enumType, Collection<String> _values) {
        List<T> listEnums = new ArrayList<T>();
        for (String value : CollectionUtils.makeNotNull(_values)) {
            T e = toEnum(_enumType, value, null);
            if (e != null)
                listEnums.add(e);
        }
        return listEnums;
    }

    public static <T extends Comparable<T>> int compare(T a, T b) {
        return compare(a, b, true);
    }

    public static <T extends Comparable<T>> int compare(T a, T b, boolean _bNullsFirst) {
        if (a != null) {
            if (b != null)
                return a.compareTo(b);
            else
                return _bNullsFirst ? 1 : -1;
        }
        if (b != null)
            return _bNullsFirst ? -1 : 1;
        return 0;
    }

    public static int min(int... values) {
        int iMin = Integer.MAX_VALUE;
        for (int value : values) {
            if (value < iMin)
                iMin = value;
        }
        return iMin;
    }

    public static String[] cleanSplit(String _sValue, String _sRegex) {
        if (_sValue == null)
            return new String[0];
        return removeEmpties(_sValue.split(_sRegex));
    }

    public static String[] removeEmpties(String[] _arr) {
        if (_arr == null)
            return new String[0];
        int valid = 0;
        for (String s : _arr) {
            if (NullUtils.isNotEmpty(s))
                valid++;
        }
        if (valid == _arr.length)
            return _arr;
        String[] ret = new String[valid];
        valid = 0;
        for (String s : _arr) {
            if (NullUtils.isNotEmpty(s))
                ret[valid++] = s;
        }
        return ret;
    }

    public static String wrap(String _input, int _lineLength) {
        return wrap(_input, _lineLength, false);
    }

    public static String wrap(String _input, int _lineLength, boolean carriageReturn) {
        if (_input == null)
            return null;
        StringBuilder output = new StringBuilder();
        int i = 0;
        while (i < _input.length()) {
            if ((i + _lineLength) > _input.length())
                output.append(_input.substring(i, _input.length()));
            else {
                output.append(_input.substring(i, i + _lineLength));
                if (carriageReturn)
                    output.append("\r");
                output.append("\n");
            }
            i += _lineLength;
        }
        return output.toString();
    }

    public static <T> Class<? extends T> getClass(String _className, Class<T> _superClass) {
        try {
            return Class.forName(_className).asSubclass(_superClass);
        }
        catch (ClassNotFoundException _e) {
            return null;
        }
    }

    public static String terminateWith(String _value, String _suffix) {
        if (_value == null)
            return _suffix;
        if (_value.endsWith(_suffix))
            return _value;
        return _value + _suffix;
    }

    public static String toUpperCase(String _value) {
        if (_value == null)
            return null;
        return _value.toUpperCase();
    }

    public static String toLowerCase(String _value) {
        if (_value == null)
            return null;
        return _value.toLowerCase();
    }

    public static Map<String, List<String>> parseQueryParams(String _queryString) {
        Map<String, List<String>> queryParameters = new HashMap<>();
        if (isEmpty(_queryString)) {
            return queryParameters;
        }
        String[] parameters = _queryString.split("&");
        for (String parameter : parameters) {
            String[] keyValuePair = parameter.split("=");
            if (keyValuePair.length > 1)
                CollectionUtils.addToMultiMap(keyValuePair[0], keyValuePair[1], queryParameters);
        }
        return queryParameters;
    }

    public static String toQueryString(Map<String, List<String>> _queryParameters) {
        StringBuilder queryString = null;
        for (Entry<String, List<String>> entry : CollectionUtils.makeNotNull(_queryParameters).entrySet()) {
            for (String param : CollectionUtils.makeNotNull(entry.getValue())) {
                if (NullUtils.isEmpty(param))
                    continue;
                if (queryString == null)
                    queryString = new StringBuilder();
                else
                    queryString.append("&");
                queryString.append(entry.getKey());
                queryString.append("=");
                queryString.append(param);
            }
        }
        return queryString == null?"":queryString.toString();
    }

    public static String toHex(String _sValue)
    {
        return toHex(toByteArray(_sValue));
    }

    public static String toHexBytes(byte[] _btData)
    {
        List<String> bytes = new ArrayList<>(_btData.length);
        for (byte b : _btData) {
            bytes.add(String.format("%02X ", b));
        }
        return CollectionUtils.delimit(bytes, " ");
    }

    public static String toHex(byte[] _btData)
    {
        try
        {
            return new String(Hex.encodeHex(_btData));
        }
        catch (Exception e)
        {
            return "";
        }
    }

    public static byte[] fromHex(String _sValue)
    {
        try
        {
            return Hex.decodeHex(makeNotNull(_sValue).toCharArray());
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static int bound(int _value, int _min, int _max) {
        if (_value < _min)
            return _min;
        if (_value > _max)
            return _max;
        return _value;
    }
}
