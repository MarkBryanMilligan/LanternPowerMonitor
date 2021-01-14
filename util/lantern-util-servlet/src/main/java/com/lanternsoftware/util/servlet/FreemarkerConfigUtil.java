package com.lanternsoftware.util.servlet;

import java.io.File;
import java.io.IOException;

import freemarker.cache.MruCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.TemplateExceptionHandler;

public abstract class FreemarkerConfigUtil {
    public static final Configuration createConfig(Class<?> _templateClassLoader, String _templatePath, int _cacheMaxTemplateCount) {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setClassForTemplateLoading(_templateClassLoader, _templatePath);
        cfg.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_23).build());
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);
        cfg.setCacheStorage(new MruCacheStorage(_cacheMaxTemplateCount, Math.max(1, _cacheMaxTemplateCount/2)));
        return cfg;
    }

    public static final Configuration createFileSystemConfig(String _templatePath, int _cacheMaxTemplateCount) {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
            cfg.setDirectoryForTemplateLoading(new File(_templatePath));
            cfg.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_23).build());
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);
            cfg.setCacheStorage(new MruCacheStorage(_cacheMaxTemplateCount, Math.max(1, _cacheMaxTemplateCount/2)));
            return cfg;
        }
        catch (IOException _e) {
            return null;
        }
    }
}
