package com.optum.sourcehawk.enforcer.file.maven;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.optum.sourcehawk.core.utils.CollectionUtils;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.maven.utils.MavenPomParser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * An enforcer which enforces that the coordinates of the maven plugins are as expected
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = MavenPlugins.Builder.class)
@AllArgsConstructor(staticName = "coordinates")
public class MavenPlugins extends AbstractMavenModelEnforcer {

    private static final String PLUGIN = "plugin";

    /**
     * The expected maven coordinates ID of expected plugins.  Maven groupId and artifactId are required
     * and version is optional.  Coordinates should be separated by colon.
     * <p>
     * Examples:
     * <p>
     * - com.optum.sourcehawk:library
     * - com.optum.sourcehawk:library:1.0.0
     */
    @NonNull
    private final List<String> expectedCoordinates;

    /**
     * {@inheritDoc}
     */
    @Override
    protected EnforcerResult enforceInternal(@NonNull final InputStream actualFileInputStream) {
        return MavenPomParser.parse(actualFileInputStream)
                .map(Model::getBuild)
                .map(Build::getPlugins)
                .map(this::enforceInternal)
                .orElseGet(() -> EnforcerResult.failed(PARSE_ERROR));
    }

    /**
     * Enforce the plugins are as expected
     *
     * @param plugins the plugins to enforce
     * @return the enforcer result
     */
    private EnforcerResult enforceInternal(final Collection<Plugin> plugins) {
        if (expectedCoordinates.isEmpty()) {
            return EnforcerResult.passed();
        }
        if (CollectionUtils.isEmpty(plugins)) {
            return EnforcerResult.failed(String.format(MISSING_DECLARATION_ERROR, getMavenModelType()));
        }
        return expectedCoordinates.stream()
                .map(expectedCoordinate -> enforcePluginCoordinates(plugins, expectedCoordinate))
                .reduce(EnforcerResult.passed(), EnforcerResult::reduce);
    }

    /**
     * Enforce that the expected coordinates exist amongst all the plugins
     *
     * @param plugins the plugins to check
     * @param expectedCoordinates the plugin's expected coordinates
     * @return the enforcer result
     */
    private EnforcerResult enforcePluginCoordinates(final Collection<Plugin> plugins, final String expectedCoordinates) {
        val expectedCoordinatesArray = expectedCoordinates.split(":");
        if (expectedCoordinatesArray.length < 2) {
            return EnforcerResult.failed(EXPECTED_FORMAT_ERROR);
        }
        return plugins.stream()
                .filter(plugin -> plugin.getArtifactId().equalsIgnoreCase(expectedCoordinatesArray[1]) && plugin.getGroupId().equalsIgnoreCase(expectedCoordinatesArray[0]))
                .findFirst()
                .map(MavenPlugins::buildModelFromPlugin)
                .map(pluginModel -> enforce(expectedCoordinatesArray, pluginModel))
                .orElseGet(() -> EnforcerResult.failed(String.format(MISSING_DECLARATION_ERROR, expectedCoordinates)));
    }

    /**
     * Build a {@link Model} from the provided {@link Plugin}
     *
     * @param plugin the plugin to build model for
     * @return the built model
     */
    private static Model buildModelFromPlugin(final Plugin plugin) {
        val model = new Model();
        model.setArtifactId(plugin.getArtifactId());
        model.setGroupId(plugin.getGroupId());
        model.setVersion(plugin.getVersion());
        return model;
    }

    /** {@inheritDoc} */
    @Override
    protected String getMavenModelType() {
        return PLUGIN;
    }

    /** {@inheritDoc} */
    @Override
    protected String getArtifactId(final Model model) {
        return Optional.ofNullable(model)
                .map(Model::getArtifactId)
                .orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    protected String getGroupId(final Model model) {
        return Optional.ofNullable(model)
                .map(Model::getGroupId)
                .orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    protected String getVersion(final Model model) {
        return Optional.ofNullable(model)
                .map(Model::getVersion)
                .orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    protected String getId(final Model model) {
        return Optional.ofNullable(model)
                .map(Model::getId)
                .orElse(null);
    }

}
