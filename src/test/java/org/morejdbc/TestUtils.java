package org.morejdbc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

class TestUtils {

    @Nonnull
    static Properties propertiesFromString(@Nullable String str) {
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
        return new Map.Entry<K, V>() {
            @Override
            public K getKey() {
                return key;
            }

            @Override
            public V getValue() {
                return value;
            }

            @Override
            public V setValue(V value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof Map.Entry)) {
                    return false;
                }
                Map.Entry<?, ?> that = (Map.Entry<?, ?>) o;
                return Objects.equals(this.getKey(), that.getKey()) &&
                        Objects.equals(this.getValue(), that.getValue());
            }

            @Override
            public int hashCode() {
                return Objects.hash(key, value);
            }

            @Override
            public String toString() {
                return key + "=" + value;
            }
        };
    }

    static void closeQuietly(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }
    }

    static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
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

    @Nonnull
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

    @Nonnull
    static byte[] readBytes(String resource) {
        return readBytes(TestUtils.class.getClassLoader(), resource);
    }

    @Nonnull
    static String readString(String resource) {
        return new String(readBytes(resource), UTF_8);
    }
}
