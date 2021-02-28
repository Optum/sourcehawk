package com.optum.sourcehawk.enforcer.file.maven;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.optum.sourcehawk.core.utils.CollectionUtils;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.maven.utils.MavenPomParser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * An enforcer which enforces that the coordinates of the maven dependencies are as expected
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = MavenDependencies.Builder.class)
@AllArgsConstructor(staticName = "coordinates")
public class MavenDependencies extends AbstractMavenModelEnforcer {

    private static final String DEPENDENCY = "dependency";

    /**
     * The expected maven coordinates ID of expected dependencies.  Maven groupId and artifactId are required
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
                .map(Model::getDependencies)
                .map(this::enforceInternal)
                .orElseGet(() -> EnforcerResult.failed(PARSE_ERROR));
    }

    /**
     * Enforce the dependencies are as expected
     *
     * @param dependencies the dependencies to enforce
     * @return the enforcer result
     */
    private EnforcerResult enforceInternal(final Collection<Dependency> dependencies) {
        if (expectedCoordinates.isEmpty()) {
            return EnforcerResult.passed();
        }
        if (CollectionUtils.isEmpty(dependencies)) {
            return EnforcerResult.failed(String.format(MISSING_DECLARATION_ERROR, getMavenModelType()));
        }
        return expectedCoordinates.stream()
                .map(expectedCoordinate -> enforceDependencyCoordinates(dependencies, expectedCoordinate))
                .reduce(EnforcerResult.passed(), EnforcerResult::reduce);
    }

    /**
     * Enforce that the expected coordinates exist amongst all the dependencies
     *
     * @param dependencies the dependencies to check
     * @param expectedCoordinates the dependency's expected coordinates
     * @return the enforcer result
     */
    private EnforcerResult enforceDependencyCoordinates(final Collection<Dependency> dependencies, final String expectedCoordinates) {
        val expectedCoordinatesArray = expectedCoordinates.split(":");
        if (expectedCoordinatesArray.length < 2) {
            return EnforcerResult.failed(EXPECTED_FORMAT_ERROR);
        }
        return dependencies.stream()
                .filter(dep -> dep.getArtifactId().equalsIgnoreCase(expectedCoordinatesArray[1]) && dep.getGroupId().equalsIgnoreCase(expectedCoordinatesArray[0]))
                .findFirst()
                .map(MavenDependencies::buildModelFromDependency)
                .map(dependencyModel -> enforce(expectedCoordinatesArray, dependencyModel))
                .orElseGet(() -> EnforcerResult.failed(String.format(MISSING_DECLARATION_ERROR, expectedCoordinates)));
    }

    /**
     * Build a {@link Model} from the provided {@link Dependency}
     *
     * @param dependency the dependency to build model for
     * @return the built model
     */
    private static Model buildModelFromDependency(final Dependency dependency) {
        val model = new Model();
        model.setArtifactId(dependency.getArtifactId());
        model.setGroupId(dependency.getGroupId());
        model.setVersion(dependency.getVersion());
        return model;
    }

    /** {@inheritDoc} */
    @Override
    protected String getMavenModelType() {
        return DEPENDENCY;
    }

}
