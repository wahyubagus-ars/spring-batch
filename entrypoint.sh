#!/bin/sh

# Default job name if not provided
JOB_NAME=${JOB_NAME:-report}

# Run the application with the specified job name
exec java -jar app.jar --job.name="$JOB_NAME" "$@"