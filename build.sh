# Build the jar file for the project
./gradlew build

# Build the docker image and push it to container registry
docker buildx build \
  --platform linux/amd64,linux/arm64/v8 \
  -t msnanda515/stockflow:latest \
  --push -f buildDockerfile .