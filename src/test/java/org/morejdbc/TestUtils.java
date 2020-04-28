package org.morejdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.SmartDataSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.sql.Connection;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

class TestUtils {

    static Properties propertiesFromString(String str) {
        Properties properties = new Properties();
        if (str != null && !str.isEmpty()) {
            try {
                properties.load(new StringReader(str));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return properties;
    }

    static <K, V> Map.Entry<K, V> immutableEntry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    static void closeQuietly(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }
    }

    static byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int pos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, pos, array.length);
            pos += array.length;
        }
        return result;
    }

    static byte[] readBytes(ClassLoader classLoader, String resource) {
        URL url = classLoader.getResource(resource);
        if (url == null) {
            throw new IllegalStateException("Missing resource [" + resource + "]");
        }
        InputStream in = null;
        try {
            return toByteArray(in = url.openStream());
        } catch (IOException e) {
            throw new IllegalStateException("Error while reading resource [" + resource + "]");
        } finally {
            closeQuietly(in);
        }
    }

    static byte[] readBytes(String resource) {
        return readBytes(TestUtils.class.getClassLoader(), resource);
    }

    static String readString(String resource) {
        return new String(readBytes(resource), UTF_8);
    }

    private static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    static JdbcTemplate jdbc(Connection connection) {
        SmartDataSource ds = smartDataSource(connection);
        return new JdbcTemplate(ds);
    }

    static SmartDataSource smartDataSource(Connection connection) {
        return new SingleConnectionDataSource(connection, false);
    }
}
