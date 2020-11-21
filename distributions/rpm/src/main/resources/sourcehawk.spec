%define __jar_repack 0
Name: ${rpm.package}
Version: ${rpm.package.version}
Release: ${rpm.package.release}
Summary: ${rpm.package.description}
License: ${project.license}
Vendor: ${project.organization.name}
URL: ${project.url}
Group: Development/Tools
Packager: ${project.organization.name}
autoprov: yes
autoreq: yes
BuildRoot: /rpmbuild/

%description
${rpm.package.description}

%prep
echo "BUILDROOT = $RPM_BUILD_ROOT"
mkdir -p $RPM_BUILD_ROOT
cp -r /tmp/rpmbuild/* $RPM_BUILD_ROOT/
exit

%files
%defattr(-,sourcehawk,sourcehawk,-)
%attr(0755, sourcehawk, sourcehawk) /usr/local/bin/*
%attr(0644, sourcehawk, sourcehawk) /usr/local/share/man/man1/*
%attr(0755, sourcehawk, sourcehawk) /usr/share/bash-completion/completions/*

%post
ln -s /usr/local/bin/sourcehawk /usr/local/bin/shawk

%postun
rm /usr/local/bin/shawk
