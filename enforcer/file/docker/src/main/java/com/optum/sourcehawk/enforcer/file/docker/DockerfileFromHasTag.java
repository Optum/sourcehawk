package com.optum.sourcehawk.enforcer.file.docker;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.optum.sourcehawk.core.utils.StringUtils;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.docker.utils.Dockerfile;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Enforce that the Dockerfile has a tag in the FROM line
 *
 * @author Brian Wyka
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = DockerfileFromHasTag.Builder.class)
@AllArgsConstructor(staticName = "allowLatest")
public class DockerfileFromHasTag extends AbstractDockerfileFromTokenEnforcer {

    private static final String MISSING_TAG_MESSAGE = "Dockerfile FROM [%s] is missing tag";
    private static final String LATEST_TAG_MESSAGE = "Dockerfile FROM [%s] has 'latest' tag";
    private static final String TAG_LATEST = "latest";

    /**
     * Whether or not to allow the "latest" tag
     */
    private final boolean allowLatest;

    /**
     * Enforce the FROM tag is as expected
     *
     * @param fromToken the FROM token
     * @return the enforcer result
     */
    @Override
    protected EnforcerResult enforceFromToken(final Dockerfile.FromToken fromToken) {
        if (fromToken.getTag() == null) {
            return EnforcerResult.failed(String.format(MISSING_TAG_MESSAGE, fromToken.getRawValue()));
        }
        if (StringUtils.equals(fromToken.getTag(), TAG_LATEST) && !allowLatest) {
            return EnforcerResult.failed(String.format(LATEST_TAG_MESSAGE, fromToken.getRawValue()));
        }
        return EnforcerResult.passed();
    }

}
