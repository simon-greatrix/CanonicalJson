S# Run 'gradlew clean createCentralZip' to create the archive
# Run update.sh to upload it to Maven Central

source ~/.gradle/central.sh

BEARER=`printf "$CENTRAL_USERNAME_TOKEN:$CENTRAL_PASSWORD_TOKEN" | base64`

curl --dry-run \
  --request POST \
  --verbose \
  --header "Authorization: Bearer $BEARER" \
  --form bundle=@build/publications/central.zip \
  https://central.sonatype.com/api/v1/publisher/upload
