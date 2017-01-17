#!/usr/bin/env bash

#TODO: add error handling - currently if anything fails at any point, the script still continues producing more errors

# GET USER INPUT
echo 'ENTER RELEASE VERSION:'
read version
echo 'ENTER GPG PASSPHRASE:'
read passphrase
#TODO: passphrase is visible on the console and then in the history, 'reset ; history -c' followed by what version is being released?

# UPDATE VERSION IN POM FILE
sed "s;^    <version>.*</version>;    <version>${version}</version>;" pom.xml > pom.xml2
rm pom.xml
mv pom.xml2 pom.xml
git add pom.xml

#TODO: update changelog with version and date (something smart checking the top line?)
#TODO: update quick-start in readme with new version (should it be here before actual release?)

# COMMIT, TAG AND PUSH
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

#TODO: update pom file with new snapshot version
#TODO: add new header for changelog
#TODO: git commit and push

# FINAL NOTE
echo "MANUAL STEP REQUIRED"
echo "Go to 'oss.sonatype.org / Staging Upload' and upload the files from 'target/release'"
echo "Then go to 'Staging Repositories', select repository and press 'Release' at the top"

#TODO: optional push (or maybe that should be manual so the verification is possible?)
# OPTIONAL PUSH
#git push
#git push --tags
