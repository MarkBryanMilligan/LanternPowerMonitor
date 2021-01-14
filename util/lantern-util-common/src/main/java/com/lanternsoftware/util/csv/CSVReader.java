package com.lanternsoftware.util.csv;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class CSVReader
{
	public static CSV loadCSVFromFile(String _filename)
	{
		return loadCSVFromFile(_filename, false);
	}
	
	public static CSV loadCSVFromFile(String _filename, boolean _firstRowIsHeader)
	{
		FileInputStream is = null;
		try
		{
			int iMaxColumns = 0;
			List<List<String>> listLines = new LinkedList<List<String>>();
			is = new FileInputStream(_filename);
			BufferedReader r = new BufferedReader(new InputStreamReader(is));
			List<String> listHeader = null;
			List<String> listCurLine = new ArrayList<String>();
			String sRemainder = null;
			String sLine = r.readLine();
			while (sLine != null)
			{
				if (sRemainder == null)
					sRemainder = parseLine(sLine, listCurLine);
				else
					sRemainder = parseLine(sRemainder+sLine, listCurLine);
				if (sRemainder == null)
				{
					if (_firstRowIsHeader && (listHeader == null))
						listHeader = listCurLine;
					else
						listLines.add(listCurLine);
					iMaxColumns = Math.max(iMaxColumns, listCurLine.size());
					listCurLine = new ArrayList<String>();
				}
				sLine = r.readLine();
			}
			return new CSV(listHeader, listLines, iMaxColumns);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			return null;
		}
		finally
		{
			IOUtils.closeQuietly(is);
		}
	}
	
	public static CSV parseCSV(String _csv)
	{
		return parseCSV(_csv, false);
	}
	
	public static CSV parseCSV(String _csv, boolean _bFirstRowIsHeader)
	{
		_csv = _csv.replace("\r","");
		int iMaxColumns = 0;
		List<List<String>> listLines = new LinkedList<List<String>>();
		List<String> listHeader = null;
		List<String> listCurLine = new ArrayList<String>();
		String sRemainder = null;
		for (String sLine : _csv.split("\n"))
		{
			if (sRemainder == null)
				sRemainder = parseLine(sLine, listCurLine);
			else
				sRemainder = parseLine(sRemainder+sLine, listCurLine);
			if (sRemainder == null)
			{
				if (_bFirstRowIsHeader && (listHeader == null))
					listHeader = listCurLine;
				else
					listLines.add(listCurLine);
				iMaxColumns = Math.max(iMaxColumns, listCurLine.size());
				listCurLine = new ArrayList<String>();
			}
		}
		return new CSV(listHeader, listLines, iMaxColumns);
	}
	
	public static String parseLine(String _sLine, List<String> _listCurLine)
	{
		int i=0;
		while (i < _sLine.length())
		{
			if (_sLine.charAt(i) == '"')
			{
				int iPos = _sLine.indexOf("\",",i+1);
 				if (iPos < 0)
				{
					if (!_sLine.endsWith("\""))
						return _sLine.substring(i);
					else
					{
						_listCurLine.add(_sLine.substring(i+1, _sLine.length()-1));
						return null;
					}
				}
				else
				{
					_listCurLine.add(_sLine.substring(i+1, iPos));
					i = iPos+2;
				}
			}
			else
			{
				int iPos = _sLine.indexOf(",",i);
				if (iPos < 0)
				{
					_listCurLine.add(_sLine.substring(i));
					return null;
				}
				_listCurLine.add(_sLine.substring(i, iPos));
				i = iPos+1;
			}
		}
		return null;
	}
}
