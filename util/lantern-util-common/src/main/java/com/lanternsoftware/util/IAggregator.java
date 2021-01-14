package com.lanternsoftware.util;

import java.util.List;

public interface IAggregator<T, V> {
    List<V> aggregate(T _t);
}
