package com.optum.sourcehawk.cli;

import com.optum.sourcehawk.core.constants.SourcehawkConstants;
import lombok.val;
import picocli.CommandLine;

import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * The Sourcehawk command (default CLI entry point for all sub-commands)
 *
 * @author Brian Wyka
 * @author Christian Oestreich
 */
@CommandLine.Command(
        name = SourcehawkConstants.NAME_LOWERCASE,
        aliases = "shawk",
        description = "Watch over your source like a hawk",
        mixinStandardHelpOptions = true,
        versionProvider = Sourcehawk.VersionProvider.class,
        subcommands = {
                CommandLine.HelpCommand.class,
                ScanCommand.class,
                ValidateConfigCommand.class,
                FixCommand.class,
                FlattenConfigCommand.class
        }
)
public class Sourcehawk {

    /**
     * Bootstrap the command
     *
     * @param args the command line args
     */
    public static void main(final String[] args) {
        val status = new CommandLine(new Sourcehawk()).execute(args);
        Runtime.getRuntime().halt(status);
    }

    /**
     * Provide the version for CLI usage
     */
    static class VersionProvider implements CommandLine.IVersionProvider {

        public static final String PROPERTIES_LOCATION = "sourcehawk.properties";
        private static final String VERSION_PROPERTY = "version";
        private static final String DEFAULT_VERSION = "0";

        /**
         * Version
         */
        static final String VERSION = loadVersion();

        /**
         * {@inheritDoc}
         */
        @Override
        public String[] getVersion() {
            return new String[]{String.format("%s v%s", SourcehawkConstants.NAME, VERSION)};
        }

        /**
         * Load the version from properties
         *
         * @return the version
         */
        private static String loadVersion() {
            try (val inputStream = VersionProvider.class.getClassLoader().getResourceAsStream(PROPERTIES_LOCATION)) {
                return loadProperties(inputStream)
                        .map(properties -> properties.getProperty(VERSION_PROPERTY))
                        .orElse(DEFAULT_VERSION);
            } catch (final Exception e) {
                return DEFAULT_VERSION;
            }
        }

        /**
         * Load the properties from an input stream
         *
         * @param inputStream the manifest input stream
         * @return the properties
         */
        private static Optional<Properties> loadProperties(final InputStream inputStream) {
            try {
                val properties = new Properties();
                properties.load(inputStream);
                return Optional.of(properties);
            } catch (final Exception e) {
                return Optional.empty();
            }
        }

    }

}
