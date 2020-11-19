package com.optum.sourcehawk.enforcer.file.maven.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * Utility class for parsing maven pom.xml file into {@link Model}
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MavenPomParser {

    /**
     * Internal reader to read the pom.xml contents
     */
    private static final MavenXpp3Reader POM_READER = new MavenXpp3Reader();

    /**
     * Read the maven model from the provided pom.xml {@link InputStream}
     *
     * @param pomXmlInputStream the pom.xml input stream
     * @return the model if parsed correctly, otherwise {@link Optional#empty()}
     */
    public static Optional<Model> parse(final InputStream pomXmlInputStream) {
        try (val pomXmlInputStreamReader = new InputStreamReader(pomXmlInputStream)) {
            return Optional.ofNullable(POM_READER.read(pomXmlInputStreamReader));
        } catch (final XmlPullParserException | IOException e) {
            return Optional.empty();
        }
    }

}
