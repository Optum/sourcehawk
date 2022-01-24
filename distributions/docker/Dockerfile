FROM scratch

ARG NAME="sourcehawk"

# Copy Group and User Files
COPY "/etc" "/etc"

# Copy the native image executable into the image
COPY --chown="${NAME}":"${NAME}" "target/native-image" "/entrypoint"

# Set the user and working directory
USER "${NAME}"
WORKDIR "/work"

# Set the native image as the entrypoint
CMD ["--help"]
ENTRYPOINT ["/entrypoint"]