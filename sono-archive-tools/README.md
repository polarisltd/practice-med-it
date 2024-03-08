## sbt project compiled with Scala 3

### Usage

Endpoint examples

access swagger docs  http://localhost:8084/api-docs/swagger.json


curl http://localhost:8084/dicom/canon?rootPath=x


curl -X POST http://localhost:8084/dicom/canon \
-H "Content-Type: application/json" \
-d '{"path":"xxx","locationType":"Voluson"}'