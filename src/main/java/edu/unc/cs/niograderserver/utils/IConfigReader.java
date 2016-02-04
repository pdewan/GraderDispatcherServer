package edu.unc.cs.niograderserver.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * @author Andrew Vitkus
 *
 */
public interface IConfigReader {

    public Optional<String> getString(String key);

    public Optional<String> getString(String key, String defaultVal);

    public Optional<Boolean> getBoolean(String key);

    public Optional<Boolean> getBoolean(String key, boolean defaultVal);

    public OptionalInt getInt(String key);

    public OptionalInt getInt(String key, int defaultVal);

    public OptionalDouble getDouble(String key);

    public OptionalDouble getDouble(String key, double defaultVal);

    public Optional<Float> getFloat(String key);

    public Optional<Float> getFloat(String key, float defaultVal);

    public OptionalLong getLong(String key);

    public OptionalLong getLong(String key, long defaultVal);

    public Optional<Byte> getByte(String key);

    public Optional<Byte> getByte(String key, byte defaultVal);

    public Optional<Character> getChar(String key);

    public Optional<Character> getChar(String key, char defaultVal);
}
