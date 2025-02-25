package com.lanternsoftware.util.dao;

import java.io.File;
import java.io.FileInputStream;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;

import com.lanternsoftware.util.NullUtils;

public abstract class AnnotationFinder {
    public static Map<String, String> findAnnotatedClasses(String _codePath, Class<? extends Annotation> _annotationClass) {
        String annotationName = "@" + _annotationClass.getSimpleName();
        Map<String, String> mapClasses = new TreeMap<>();
        searchFile(new File(_codePath), mapClasses, annotationName);
        Iterator<String> classIter = mapClasses.keySet().iterator();
        while (classIter.hasNext()) {
            String className = classIter.next();
            try {
                Class<?> clazz = Class.forName(className);
                if (!clazz.isAnnotationPresent(_annotationClass))
                    classIter.remove();
            } catch (ClassNotFoundException _e) {
                //ignore
            }
        }
        return mapClasses;
    }

    public static Map<String, String> findSubclasses(String _codePath, Class<?> _superclass) {
        Map<String, String> mapClasses = new TreeMap<>();
        searchFile(new File(_codePath), mapClasses, _superclass.getSimpleName());
        Iterator<String> classIter = mapClasses.keySet().iterator();
        while (classIter.hasNext()) {
            String className = classIter.next();
            Class<?> clazz = NullUtils.getClass(className, _superclass);
            if (clazz == null)
                classIter.remove();
        }
        return mapClasses;
    }

    private static void searchFile(File _f, Map<String, String> _mapClasses, String _searchString) {
        if (_f == null) {
            return;
        }
        if (_f.isDirectory()) {
            for (File child : _f.listFiles()) {
                searchFile(child, _mapClasses, _searchString);
            }
        }
        else if (_f.getName().endsWith(".java")) {
            try {
                String source = IOUtils.toString(new FileInputStream(_f), StandardCharsets.UTF_8);
                if (!source.contains(_searchString)) {
                    return;
                }
                int packagePos = source.indexOf("package ");
                int packageEnd = source.indexOf(";", packagePos);
                String packageName = source.substring(packagePos + 8, packageEnd);
                int classPos = source.indexOf("public class") + 12;
                while (source.charAt(classPos) == ' ')
                    classPos++;
                int newLineN = source.indexOf("\n", classPos);
                int newLineR = source.indexOf("\r", classPos);
                int space = source.indexOf(" ", classPos);
                int classEnd = NullUtils.min((newLineN == -1) ? Integer.MAX_VALUE : newLineN, (newLineR == -1) ? Integer.MAX_VALUE : newLineR, (space == -1) ? Integer.MAX_VALUE : space);
                String className = source.substring(classPos, classEnd);
                _mapClasses.put(packageName + "." + className, _f.getParent());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
