#!/usr/bin/env bash

#TODO: add error handling - currently if anything fails at any point, the script still continues producing more errors

# GET USER INPUT
echo 'ENTER RELEASE VERSION:'
read version
echo 'ENTER GPG PASSPHRASE:'
read -s passphrase

# UPDATE VERSIONS
sed -i "s|^    <version>.*</version>|    <version>${version}</version>|" pom.xml
sed -i "1s|^.*$|##### ${version} (`date +%d/%m/%Y`)|" CHANGELOG.md
sed -i "s|^    <version>.*</version>|    <version>${version}</version>|" README.md
git add pom.xml CHANGELOG.md README.md
git commit -m "prepare release map-to-object-converter-${version}"
git tag "${version}"

# BUILD THE ARTIFACTS
rm -rf target/
mvn package
mvn source:jar
mvn javadoc:jar

# COPY THE ARTIFACTS TO target/release
mkdir target/release
cp pom.xml target/release/map-to-object-converter-${version}.pom
cp target/*.jar target/release

# SIGN THE ARTIFACTS
cd target/release
echo "${passphrase}" | gpg --passphrase-fd 0 -ab map-to-object-converter-${version}.pom
echo "${passphrase}" | gpg --passphrase-fd 0 -ab map-to-object-converter-${version}.jar
echo "${passphrase}" | gpg --passphrase-fd 0 -ab map-to-object-converter-${version}-javadoc.jar
echo "${passphrase}" | gpg --passphrase-fd 0 -ab map-to-object-converter-${version}-sources.jar
cd ../..

# PREPARE FOR NEXT ITERATION
nextVersion=$((${version%%.*} + 1)).0
sed -i "s|^    <version>.*</version>|    <version>${nextVersion}-SNAPSHOT</version>|" pom.xml
sed -i "1s|^|##### ${nextVersion} (not yet released)\n\n|" CHANGELOG.md
git add pom.xml CHANGELOG.md
git commit -m "prepare for next development iteration"

# FINAL NOTE
echo -e "\nMANUAL STEP REQUIRED\n"
echo "Go to 'oss.sonatype.org / Staging Upload' and upload the files from 'target/release'"
echo "Then go to 'Staging Repositories', select repository and press 'Release' at the top"

#TODO: optional push (or maybe that should be manual so the verification is possible?)
# OPTIONAL PUSH
#git push
#git push --tags
