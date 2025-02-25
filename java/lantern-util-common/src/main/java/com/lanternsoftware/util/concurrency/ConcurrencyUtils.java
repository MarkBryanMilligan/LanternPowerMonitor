package com.lanternsoftware.util.concurrency;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public abstract class ConcurrencyUtils
{
	private static final Object m_mutex = new Object();
	private static Map<String, String> mapMutexes = null;
	
	public static void sleep(long _lDuration)
	{
		try
		{
			Thread.sleep(_lDuration);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void wait(Object _object)
	{
		try
		{
			synchronized(_object)
			{
				_object.wait();
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void wait(Object _object, int _iTimeout)
	{
		try
		{
			synchronized(_object)
			{
				_object.wait(_iTimeout);
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void notify(Object _object)
	{
		try
		{
			synchronized(_object)
			{
				_object.notify();
			}
		}
		catch (IllegalMonitorStateException e)
		{
			e.printStackTrace();
		}
	}

	public static void notifyAll(Object _object)
	{
		try
		{
			synchronized(_object)
			{
				_object.notifyAll();
			}
		}
		catch (IllegalMonitorStateException e)
		{
			e.printStackTrace();
		}
	}
	
	public static String getMutex(String _sKey)
	{
		synchronized (m_mutex)
		{
			if (mapMutexes == null)
				mapMutexes = new HashMap<String, String>();
			String sMutex = mapMutexes.get(_sKey);
			if (sMutex != null)
				return sMutex;
			mapMutexes.put(_sKey, _sKey);
			return _sKey;
		}
	}
	
	public static void destroy()
	{
		if (mapMutexes != null)
		{
			mapMutexes.clear();
			mapMutexes = null;
		}
	}
	
	public static void getAll(Future<?>... _futures)
	{
		if (_futures == null)
			return;
		getAll(Arrays.asList(_futures));
	}
	
	public static void getAll(Collection<Future<?>> _collFutures)
	{
		if (_collFutures == null)
			return;
		for (Future<?> f : _collFutures)
		{
			try
			{
				f.get();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
