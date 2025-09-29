# S3 CRUD API

A minimal Spring Boot REST API for managing files in Amazon S3. It supports:

- Upload a file to S3
- Download a file from S3
- Generate presigned URLs for a file
- List files in a bucket
- Delete a file
- OpenAPI/Swagger UI documentation

## Tech stack
- Java 21
- Spring Boot 3.5.x (Web)
- AWS SDK for Java v2 (S3)
- Lombok
- springdoc-openapi
- Maven

## Prerequisites
- Java 21
- Maven 3.9+
- An AWS account with an S3 bucket
- AWS credentials with permissions for s3:PutObject, s3:GetObject, s3:ListBucket, s3:DeleteObject

## Configuration
This app reads its configuration from `src/main/resources/application.yml` (or environment variables overriding those values).

Do NOT commit real AWS secrets. Use placeholders or environment variables. Below is a safe template you can copy to your `application.yml`:

```yaml
server:
  port: 8080

spring:
  application:
    name: s3-crud-api
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

aws:
  access-key-id: ${AWS_ACCESS_KEY_ID:YOUR_ACCESS_KEY_ID}
  secret-access-key: ${AWS_SECRET_ACCESS_KEY:YOUR_SECRET_ACCESS_KEY}
  region: ${AWS_REGION:eu-central-1}
  s3:
    bucket-name: ${AWS_S3_BUCKET:your-bucket-name}

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

### Suggested placeholders and descriptions
- AWS_ACCESS_KEY_ID: IAM user or role access key id
- AWS_SECRET_ACCESS_KEY: IAM user or role secret
- AWS_REGION: e.g., eu-central-1, us-east-1
- AWS_S3_BUCKET: name of your target S3 bucket

You can set these via environment variables instead of hardcoding values in `application.yml`:

```bash
export AWS_ACCESS_KEY_ID=AKIA... \
       AWS_SECRET_ACCESS_KEY=... \
       AWS_REGION=eu-central-1 \
       AWS_S3_BUCKET=my-bucket
```

## Build and Run

- Build: `./mvnw clean package`
- Run: `./mvnw spring-boot:run`

Or run the built jar:

```bash
java -jar target/s3-crud-api-0.0.1-SNAPSHOT.jar
```

The app will start on `http://localhost:8080` by default.

## API Endpoints
Base path: `/api/v1/files`

- POST `/upload` (multipart/form-data)
  - Request: form-data key `file` with the file to upload
  - Response: JSON with fileKey, presignedUrl, message

- GET `/download/{fileName}`
  - Downloads the raw file bytes

- GET `/presigned-url/{fileName}`
  - Returns a presigned URL valid for 1 hour

- GET `/list`
  - Returns an array of objects: fileKey, fileName, size, lastModified

- DELETE `/delete/{fileName}`
  - Deletes the file

### Try it in Swagger UI
Once the app is running, open:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/api-docs`

## Notes on credentials and security
- Prefer environment variables or AWS default providers (e.g., instance role) over hardcoding secrets.
- For production, consider using AWS IAM Roles, AWS Secrets Manager, or Parameter Store.
- Restrict bucket policies and IAM permissions to the minimum required.

## Troubleshooting
- AccessDenied: Verify IAM permissions for your user/role.
- NoSuchBucket: Ensure `aws.s3.bucket-name` exists and is in the configured region.
- Region mismatch: The client region must match the bucket region.
- 413 Payload Too Large: Increase `spring.servlet.multipart.max-file-size` and `max-request-size`.

## License
This project is provided as-is under the MIT license (or your preferred license).
