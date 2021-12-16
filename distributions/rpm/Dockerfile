ARG FROM_TAG=centos7
FROM ghcr.io/optum/sourcehawk-ci/rpmbuild:${FROM_TAG}

# Build Arguments
ARG RPM_BUILD_DIRECTORY
ARG RPM_LICENSE_FILE
ARG RPM_BUILD_SPEC_FILE
ARG RPM_FILE
ARG RPM_ARCH

# Change the working directory to the rpm build root
WORKDIR /rpmbuild

# Make the build directory structure
RUN mkdir -p RPMS/${RPM_ARCH} SOURCES SPECS SRPMS

# Copy required build files into image
COPY ${RPM_BUILD_DIRECTORY} /tmp/rpmbuild
COPY ${RPM_LICENSE_FILE} /tmp/rpmbuild-LICENSE
COPY ${RPM_BUILD_SPEC_FILE} /tmp/rpmbuild.spec

# Create the rpm package
RUN rpmbuild --target ${RPM_ARCH} -bb /tmp/rpmbuild.spec

# Move the RPM to the destination
RUN mv /root/rpmbuild/RPMS/${RPM_ARCH}/*.rpm ${RPM_FILE}