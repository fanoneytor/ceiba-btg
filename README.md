# Fullstack Demo - Angular + Spring Boot + AWS

Este proyecto es una aplicación **Fullstack** compuesta por:

- **Frontend**: Angular, desplegado en **S3 + CloudFront**
- **Backend**: Spring Boot (Java 17), desplegado en **EC2 con Nginx como reverse proxy**
- **Base de Datos**: MongoDB Atlas (externo)

---

## 1. Requisitos Previos

Antes de ejecutar local o desplegar necesitas:

- [Node.js](https://nodejs.org/) (v18+ recomendado)
- [Angular CLI](https://angular.dev/cli)  
  ```bash
  npm install -g @angular/cli
  ```
- [Java 17](https://adoptium.net/) (Amazon Corretto o OpenJDK)
- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) configurado
- MongoDB Atlas cluster (tendrás tu propia URI)

---

## 2. Ejecutar en Local

### 2.1 Backend (Spring Boot)

```bash
# Entrar al directorio backend
cd backend

# Construir el jar
.\mvnw.cmd -DskipTests package

# Ejecutar localmente
java -jar target/app-1.0.0.jar   --server.port=8080   --spring.data.mongodb.uri="mongodb+srv://<USER>:<PASSWORD>@<CLUSTER>/?retryWrites=true&w=majority"
```

Endpoints disponibles:

- `http://localhost:8080/api/clients`
- `http://localhost:8080/api/funds`
- `http://localhost:8080/api/transactions/subscribe`
- `http://localhost:8080/api/transactions/cancel`
- `http://localhost:8080/api/transactions/history/{clientId}`

---

### 2.2 Frontend (Angular)

```bash
# Entrar al directorio frontend
cd frontend

# Instalar dependencias
npm install

# Ejecutar en local
ng serve --open
```

La app estará en `http://localhost:4200` y consumirá la API local (`http://localhost:8080/api`).

---

## 3. Despliegue en AWS

### 3.2 Desplegar con CloudFormation

El template (`infra/fullstack.yml`) crea:

- Buckets S3 para frontend y backend
- EC2 con Nginx + Spring Boot (systemd)
- CloudFront con dos orígenes: S3 (frontend) y EC2 (backend)
- Roles e Instance Profile para EC2

Comando de despliegue:

```bash
aws cloudformation deploy   --stack-name fullstack-demo   --template-file infra/fullstack.yml   --capabilities CAPABILITY_NAMED_IAM   --parameter-overrides       ProjectName=demo-fullstack       BackendJarS3Key=backend/app-1.0.0.jar       ArtifactVersion=v1       MongoUri="mongodb+srv://<USER>:<PASSWORD>@<CLUSTER>/?retryWrites=true&w=majority"       BackendPort=8080       InstanceType=t3.small       KeyName=<TU-KEY-PAIR>       DefaultVpcId=<VPC-ID>       DefaultSubnetId=<SUBNET-ID>
```

⚠️ **Nota**: Reemplaza los parámetros `<...>` con tus valores, pero nunca los subas a un repo público.

---

### 3.1 Construir Artefactos

- **Backend**:  
  ```bash
  cd backend
  .\mvnw.cmd -DskipTests package
  ```
  Copiar el artefacto a un bucket S3 creado por el template:
  ```bash
  aws s3 cp target/app-1.0.0.jar s3://demo-fullstack-backend-fullstack-demo/backend/app-1.0.0.jar
  ```

- **Frontend**:  
  ```bash
  cd frontend
  ng build --configuration production
  ```
  Copiar los archivos estáticos al bucket de frontend:
  ```bash
  aws s3 sync dist/frontend s3://demo-fullstack-frontend-fullstack-demo/
  ```
Si los nombres de los bucket por algun motivo ya existe, se deben cambiar en el archivo de CloudFormation
---

## 4. Acceso a la Aplicación

Cuando CloudFormation termine:

- URL de frontend (Angular) estará en el output `CloudFrontURL`
- API accesible en `https://<CloudFrontURL>/api/...`

Ejemplo:

```bash
curl -i https://<CloudFrontURL>/api/clients
```

---

## 5. Notas Importantes

- Si cambias el jar, vuelve a subirlo al bucket S3 y actualiza `ArtifactVersion` para forzar redeploy.
- Nginx ya está configurado como reverse proxy para `/api/*`.
- CloudFront está configurado para cachear solo `GET, HEAD`; los métodos `POST, PUT, DELETE` siempre pasan al backend.
- En local funciona sin CORS, en AWS está habilitado en `@CrossOrigin`.

---
