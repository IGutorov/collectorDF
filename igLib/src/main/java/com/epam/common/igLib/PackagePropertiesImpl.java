package com.epam.common.igLib;

import static com.epam.common.igLib.LibFiles.*;
import static com.epam.common.igLib.LibFormats.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

class PackagePropertiesImpl {

    private static final Logger logger = CustomLogger.getDefaultLogger();
    
    // static map
    private static Map<String, PackagePropertiesInner> propsMap = new HashMap<String, PackagePropertiesInner>();

    private static class PackagePropertiesInner {

        private final String        path;
        private Properties          properties; // upload by perforce

        PackagePropertiesInner(String path) {
            this.path = path;
        }

        String getProperty(String param) {
            if (param == null)
                return EMPTY_STRING;

            if (properties == null) {
                try {
                    properties = getResourceProperties(path);
                } catch (IOException e) {
                    logger.error("Not readed file. Path = " + path, e);
                }
            }
            if (properties == null)
                properties = new Properties();

            String result = properties.getProperty(param);
            return result == null ? EMPTY_STRING : result;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PackagePropertiesInner other = (PackagePropertiesInner) obj;
            if (path == null) {
                if (other.path != null)
                    return false;
            } else if (!path.equals(other.path))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "PackagePropertiesInner [path=" + path + ", properties=" + properties + "]";
        }
    }

    protected PackagePropertiesImpl() throws Exception {
        throw new Exception("no instance");
    }

    protected static String getProperty(String path, String param) {
        if (path == null) {
            logger.warn("Error. path = null");
            return EMPTY_STRING;
        }
        PackagePropertiesInner curr = propsMap.get(path);
        if (curr == null) {
            curr = new PackagePropertiesInner(path);
            propsMap.put(path, curr);
        }
        return curr.getProperty(param);
    }

    protected static void reloadProps() {
        for(String key : propsMap.keySet())
            propsMap.put(key, new PackagePropertiesInner(key));
    } 

    protected static String getStats(String path) {
        PackagePropertiesInner curr = propsMap.get(path);
        return (curr != null) ? curr.toString() : null;
    }
}
