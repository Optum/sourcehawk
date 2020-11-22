ARG FROM=centos:7
FROM ${FROM}

# Install rpm-build for builing RPM package
RUN yum install -y rpm-build

# Build Arguments
ARG RPM_BUILD_DIRECTORY
ARG RPM_BUILD_SPEC_FILE
ARG RPM_FILE
ARG RPM_ARCH

# Change the working directory to the rpm build root
WORKDIR /rpmbuild

# Make the build directory structure
RUN mkdir -p RPMS/${RPM_ARCH} SOURCES SPECS SRPMS

# Copy required build files into image
COPY ${RPM_BUILD_DIRECTORY} /tmp/rpmbuild
COPY ${RPM_BUILD_SPEC_FILE} /tmp/rpmbuild.spec

# Create the rpm package
RUN rpmbuild --target ${RPM_ARCH} -bb /tmp/rpmbuild.spec

# Move the RPM to the destination
RUN mv /root/rpmbuild/RPMS/${RPM_ARCH}/*.rpm ${RPM_FILE}