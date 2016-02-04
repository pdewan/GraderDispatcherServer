package edu.unc.cs.niograderserver.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigReader implements IConfigReader {

    private static final Logger LOG = Logger.getLogger(ConfigReader.class.getName());

    private HashMap<String, String> config;

    public ConfigReader(Path filePath) throws FileNotFoundException, IOException {
        Objects.requireNonNull(filePath, "The requested config file path cannot be null.");
        config = new HashMap<>(10);
        Files.lines(filePath).filter((line) -> !(line.startsWith("#") || line.isEmpty()))
                .distinct()
                .forEach((line) -> {
                    line = line.trim();
                    line = line.split("#")[0].trim();
                    if (!line.isEmpty()) {
                        String[] property = line.split("[ ]*=[ ]*");
                        config.put(property[0], property[1]);
                    }
                });

        /*try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
         while (br.ready()) {
         String line = br.readLine();
         line = line.trim();
         line = line.split("#")[0];
         if (!line.isEmpty()) {
         String[] property = line.split("[ ]*=[ ]*");
         config.put(property[0], property[1]);
         }
         }
         }*/
    }

    public ConfigReader(String path) throws FileNotFoundException, IOException {
        this(Paths.get(path));
    }

    @Override
    public Optional<String> getString(String key) {
        Objects.requireNonNull(key, "The requested config key cannot be null.");

        String str = config.get(key);
        if (str != null) {
            return Optional.of(str);
        } else {
            LOG.log(Level.INFO, "Invalid request for config value with key ''{0}''", key);
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getString(String key, String defaultVal) {
        Objects.requireNonNull(key, "The requested config key cannot be null.");

        String str = config.get(key);
        if (str != null) {
            return Optional.of(str);
        } else {
            return Optional.ofNullable(defaultVal);
        }
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        Objects.requireNonNull(key, "The requested config key cannot be null.");

        Optional<String> str = getString(key);
        Optional<Boolean> bool = Optional.empty();
        if (str.isPresent()) {
            String val = str.get();
            if (val.equalsIgnoreCase("true")) {
                bool = Optional.of(Boolean.TRUE);
            } else if (val.equalsIgnoreCase("false")) {
                bool = Optional.of(Boolean.FALSE);
            } else {
                LOG.log(Level.INFO, "Invalid request for config value with key ''{0}''", key);
            }
        }

        return bool;
    }

    @Override
    public Optional<Boolean> getBoolean(String key, boolean defaultVal) {
        Objects.requireNonNull(key, "The requested config key cannot be null.");

        Optional<String> str = getString(key);
        Optional<Boolean> bool = Optional.of(defaultVal);
        if (str.isPresent()) {
            String val = str.get();
            if (val.equalsIgnoreCase("true")) {
                bool = Optional.of(Boolean.TRUE);
            } else if (val.equalsIgnoreCase("false")) {
                bool = Optional.of(Boolean.FALSE);
            } else {
                LOG.log(Level.INFO, "Invalid request for config value with key ''{0}''", key);
            }
        }

        return bool;
    }

    @Override
    public OptionalInt getInt(String key) {
        Objects.requireNonNull(key, "The requested config key cannot be null.");

        Optional<String> str = getString(key);

        if (str.isPresent()) {
            try {
                int num = Integer.parseInt(str.get());
                return OptionalInt.of(num);
            } catch (NumberFormatException e) {
                LOG.log(Level.INFO, "Invalid request for config value with key ''{0}''", key);
            }
        }
        return OptionalInt.empty();
    }

    @Override
    public OptionalInt getInt(String key, int defaultVal) {
        Objects.requireNonNull(key, "The requested config key cannot be null.");

        Optional<String> str = getString(key);

        if (str.isPresent()) {
            try {
                int num = Integer.parseInt(str.get());
                return OptionalInt.of(num);
            } catch (NumberFormatException e) {
                LOG.log(Level.INFO, "Invalid request for config value with key ''{0}''", key);
            }
        }
        return OptionalInt.empty();
    }

    @Override
    public OptionalDouble getDouble(String key) {
        Objects.requireNonNull(key, "The requested config key cannot be null.");

        Optional<String> str = getString(key);

        if (str.isPresent()) {
            try {
                double num = Double.parseDouble(str.get());
                return OptionalDouble.of(num);
            } catch (NumberFormatException e) {
                LOG.log(Level.INFO, "Invalid request for config value with key ''{0}''", key);
            }
        }
        return OptionalDouble.empty();
    }

    @Override
    public OptionalDouble getDouble(String key, double defaultVal) {
        Objects.requireNonNull(key, "The requested config key cannot be null.");

        Optional<String> str = getString(key);

        if (str.isPresent()) {
            try {
                double num = Double.parseDouble(str.get());
                return OptionalDouble.of(num);
            } catch (NumberFormatException e) {
                LOG.log(Level.INFO, "Invalid request for config value with key ''{0}''", key);
            }
        }
        return OptionalDouble.empty();
    }

    @Override
    public Optional<Float> getFloat(String key) {
        Objects.requireNonNull(key, "The requested config key cannot be null.");

        Optional<String> str = getString(key);

        if (str.isPresent()) {
            try {
                float num = Float.parseFloat(str.get());
                return Optional.of(num);
            } catch (NumberFormatException e) {
                LOG.log(Level.INFO, "Invalid request for config value with key ''{0}''", key);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Float> getFloat(String key, float defaultVal) {
        Objects.requireNonNull(key, "The requested config key cannot be null.");

        Optional<String> str = getString(key);

        if (str.isPresent()) {
            try {
                float num = Float.parseFloat(str.get());
                return Optional.of(num);
            } catch (NumberFormatException e) {
                LOG.log(Level.INFO, "Invalid request for config value with key ''{0}''", key);
            }
        }
        return Optional.empty();
    }

    @Override
    public OptionalLong getLong(String key) {
        Objects.requireNonNull(key, "The requested config key cannot be null.");

        Optional<String> str = getString(key);

        if (str.isPresent()) {
            try {
                long num = Long.parseLong(str.get());
                return OptionalLong.of(num);
            } catch (NumberFormatException e) {
                LOG.log(Level.INFO, "Invalid request for config value with key ''{0}''", key);
            }
        }
        return OptionalLong.empty();
    }

    @Override
    public OptionalLong getLong(String key, long defaultVal) {
        Objects.requireNonNull(key, "The requested config key cannot be null.");

        Optional<String> str = getString(key);

        if (str.isPresent()) {
            try {
                long num = Long.parseLong(str.get());
                return OptionalLong.of(num);
            } catch (NumberFormatException e) {
                LOG.log(Level.INFO, "Invalid request for config value with key ''{0}''", key);
            }
        }
        return OptionalLong.empty();
    }

    @Override
    public Optional<Byte> getByte(String key) {
        Objects.requireNonNull(key, "The requested config key cannot be null.");

        Optional<String> str = getString(key);

        if (str.isPresent()) {
            try {
                byte num = Byte.parseByte(str.get());
                return Optional.of(num);
            } catch (NumberFormatException e) {
                LOG.log(Level.INFO, "Invalid request for config value with key ''{0}''", key);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Byte> getByte(String key, byte defaultVal) {
        Objects.requireNonNull(key, "The requested config key cannot be null.");

        Optional<String> str = getString(key);

        if (str.isPresent()) {
            try {
                byte num = Byte.parseByte(str.get());
                return Optional.of(num);
            } catch (NumberFormatException e) {
                LOG.log(Level.INFO, "Invalid request for config value with key ''{0}''", key);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Character> getChar(String key) {
        Objects.requireNonNull(key, "The requested config key cannot be null.");

        Optional<String> str = getString(key);

        if (str.isPresent()) {
            try {
                char c = str.get().charAt(0);
                return Optional.of(c);
            } catch (NumberFormatException e) {
                LOG.log(Level.INFO, "Invalid request for config value with key ''{0}''", key);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Character> getChar(String key, char defaultVal) {
        Objects.requireNonNull(key, "The requested config key cannot be null.");

        Optional<String> str = getString(key);

        if (str.isPresent()) {
            try {
                char c = str.get().charAt(0);
                return Optional.of(c);
            } catch (NumberFormatException e) {
                LOG.log(Level.INFO, "Invalid request for config value with key ''{0}''", key);
            }
        }
        return Optional.empty();
    }
}
