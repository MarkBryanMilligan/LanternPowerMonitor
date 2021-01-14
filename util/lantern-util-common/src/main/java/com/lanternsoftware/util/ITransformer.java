package com.lanternsoftware.util;

public interface ITransformer<T, V> {
    V transform(T _t);
}
