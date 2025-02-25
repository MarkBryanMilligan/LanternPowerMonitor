package com.lanternsoftware.util;

import java.util.Collection;

public interface IAggregator<T, V> {
    Collection<V> aggregate(T _t);
}
