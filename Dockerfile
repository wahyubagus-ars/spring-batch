# Build stage
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# Run stage
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy the specific jar file (adjust the name pattern if needed)
COPY --from=build /app/build/libs/batch-*-SNAPSHOT.jar /app/app.jar

# Copy the entrypoint script
COPY entrypoint.sh /app/
RUN chmod +x /app/entrypoint.sh

# Add entrypoint
ENTRYPOINT ["/app/entrypoint.sh"]