Param(
    [string]$Region = "us-east-1",
    [string]$ArtifactBucket = "",
    [string]$BackStack = "btg-backend",
    [string]$FrontStack = "btg-frontend",
    [string]$SiteBucketName = "",
    [string]$PlatformArn = "",
    [string]$MongoUri = "",
    [string]$InstanceType = "t3.micro"
)

$ErrorActionPreference = "Stop"

function Need($name) {
    if (-not (Get-Command $name -ErrorAction SilentlyContinue)) {
        throw "Falta comando requerido: $name"
    }
}

Need aws
Need Compress-Archive
Need powershell

# Rutas (siempre relativas a la carpeta del script)
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$BackDir = Join-Path $Root "back"
$FrontDir = Join-Path $Root "front"
$InfraDir = Join-Path $Root "infra"
$BuildDir = Join-Path $Root "build"
New-Item -ItemType Directory -Force -Path $BuildDir | Out-Null

$BackTemplate = Join-Path $InfraDir "backend-eb.yml"
$FrontTemplate = Join-Path $InfraDir "frontend-s3.yml"

# Validar mvnw.cmd ya con ruta absoluta
if (-not (Test-Path (Join-Path $BackDir "mvnw.cmd"))) {
    throw "No se encontro mvnw.cmd en $BackDir. Agrega Maven Wrapper o instala Maven global."
}

# Valores por defecto
if ([string]::IsNullOrWhiteSpace($ArtifactBucket)) {
    $ArtifactBucket = "btg-artifacts-$([DateTimeOffset]::Now.ToUnixTimeSeconds())"
}
if ([string]::IsNullOrWhiteSpace($SiteBucketName)) {
    $SiteBucketName = "btg-funds-site-$([DateTimeOffset]::Now.ToUnixTimeSeconds())"
}
if ([string]::IsNullOrWhiteSpace($MongoUri)) {
    throw "Debes pasar -MongoUri 'mongodb+srv://user:pass@cluster/db?...'"
}
if ([string]::IsNullOrWhiteSpace($PlatformArn)) {
    # Copia el ARN exacto desde la consola de EB según tu región (Java 21 / AL2023)
    $PlatformArn = "arn:aws:elasticbeanstalk:$Region::platform/Java 21 running on 64bit Amazon Linux 2023/5.0.0"
}

Write-Host "Region: $Region"
Write-Host "Stacks: back=$BackStack, front=$FrontStack"
Write-Host "Bucket artefactos: $ArtifactBucket"
Write-Host "Bucket sitio: $SiteBucketName"

# 1) Compilar backend
Write-Host "Compilando backend..."
Push-Location $BackDir
# OJO: ya estamos en /back, por eso es .\mvnw.cmd (no .\back\mvnw.cmd)
.\mvnw.cmd -DskipTests package
Pop-Location

# 2) Empaquetar app.zip (app.jar + Procfile)
$AppJar = Join-Path $BuildDir "app.jar"
$Procfile = Join-Path $BuildDir "Procfile"
$AppZip = Join-Path $BuildDir "app.zip"
Copy-Item (Get-ChildItem (Join-Path $BackDir "target\*.jar") | Select-Object -First 1).FullName $AppJar -Force
"web: java -jar app.jar --server.port=`$PORT" | Out-File -Encoding ascii $Procfile -Force
if (Test-Path $AppZip) { Remove-Item $AppZip -Force }
Compress-Archive -Path $AppJar, $Procfile -DestinationPath $AppZip

# 3) Crear bucket de artefactos (respetando región) y subir ZIP
Write-Host "Verificando/creando bucket S3 de artefactos $ArtifactBucket ..."

function Invoke-Aws {
  param([string[]]$Args)
  $psi = @{
    FilePath = "aws"
    ArgumentList = $Args
    NoNewWindow = $true
    PassThru = $true
    Wait = $true
    RedirectStandardError = "$env:TEMP\aws_err.txt"
    RedirectStandardOutput = "$env:TEMP\aws_out.txt"
  }
  $p = Start-Process @psi
  return $p.ExitCode
}

# 3.1 ¿Existe el bucket?
$exit = Invoke-Aws @("s3api","head-bucket","--bucket",$ArtifactBucket)
$bucketExists = ($exit -eq 0)

# 3.2 Si no existe, crearlo (us-east-1 no lleva LocationConstraint)
if (-not $bucketExists) {
  Write-Host "Bucket no existe. Creando $ArtifactBucket en $Region ..."
  if ($Region -eq "us-east-1") {
    $exit = Invoke-Aws @("s3api","create-bucket","--bucket",$ArtifactBucket,"--region",$Region)
  } else {
    $exit = Invoke-Aws @("s3api","create-bucket","--bucket",$ArtifactBucket,"--region",$Region,"--create-bucket-configuration","LocationConstraint=$Region")
  }
  if ($exit -ne 0) {
    Write-Host "ERROR creando bucket. STDERR:"; Get-Content "$env:TEMP\aws_err.txt"
    throw "Fallo create-bucket ($exit)"
  }

  # 3.3 Esperar disponibilidad
  for ($i=1; $i -le 10; $i++) {
    $exit = Invoke-Aws @("s3api","head-bucket","--bucket",$ArtifactBucket)
    if ($exit -eq 0) { break }
    Start-Sleep -Seconds 3
    if ($i -eq 10) { throw "Timeout esperando a que exista el bucket $ArtifactBucket" }
  }
} else {
  Write-Host "Bucket ya existe: $ArtifactBucket"
}

# 3.4 Subir ZIP
$ArtifactKey = "btg/app.zip"
Write-Host "Subiendo artefacto a s3://$ArtifactBucket/$ArtifactKey"
$exit = Invoke-Aws @("s3","cp",$AppZip,"s3://$ArtifactBucket/$ArtifactKey")
if ($exit -ne 0) {
  Write-Host "ERROR subiendo artefacto. STDERR:"; Get-Content "$env:TEMP\aws_err.txt"
  throw "Fallo s3 cp ($exit)"
}

# 4) Desplegar backend (CloudFormation / EB)
Write-Host "Desplegando backend (Elastic Beanstalk Single Instance)..."
aws cloudformation deploy `
    --region $Region `
    --stack-name $BackStack `
    --template-file $BackTemplate `
    --capabilities CAPABILITY_NAMED_IAM `
    --parameter-overrides `
    ArtifactBucket=$ArtifactBucket `
    ArtifactKey=$ArtifactKey `
    PlatformArn="$PlatformArn" `
    MongoUri="$MongoUri" `
    AppCorsOrigin="*" `
    InstanceType=$InstanceType | Out-Null

# Obtener EBUrl output
$BackOutputs = aws cloudformation describe-stacks --region $Region --stack-name $BackStack --query "Stacks[0].Outputs" --output json | ConvertFrom-Json
$EBUrl = ($BackOutputs | Where-Object { $_.OutputKey -eq "EBUrl" }).OutputValue
if (-not $EBUrl) { throw "No se pudo obtener EBUrl del backend." }
Write-Host "Backend URL: $EBUrl"

# 5) Preparar y compilar frontend (ajustar environment.prod.ts a EBUrl)
Write-Host "Compilando frontend..."
$EnvProd = Join-Path $FrontDir "src\environments\environment.prod.ts"
if (-not (Test-Path $EnvProd)) {
    "export const environment = { production: true, apiUrl: 'http://$EBUrl/api' };" | Out-File -Encoding utf8 $EnvProd
}
else {
    # Reemplazo liviano de apiUrl
    (Get-Content $EnvProd) `
        -replace "apiUrl:\s*'http.*?'", "apiUrl: 'http://$EBUrl/api'" `
  | Set-Content $EnvProd
}

Push-Location $FrontDir
if (-not (Test-Path "node_modules")) { npm install }
if (-not (Get-Command ng -ErrorAction SilentlyContinue)) {
    npx @angular/cli build --configuration production
}
else {
    ng build --configuration production
}
Pop-Location

# 6) Desplegar frontend (S3 Website)
Write-Host "Desplegando frontend (S3 Website)..."
aws cloudformation deploy `
    --region $Region `
    --stack-name $FrontStack `
    --template-file $FrontTemplate `
    --parameter-overrides SiteBucketName=$SiteBucketName | Out-Null

$FrontOutputs = aws cloudformation describe-stacks --region $Region --stack-name $FrontStack --query "Stacks[0].Outputs" --output json | ConvertFrom-Json
$WebsiteURL = ($FrontOutputs | Where-Object { $_.OutputKey -eq "WebsiteURL" }).OutputValue
$BucketName = ($FrontOutputs | Where-Object { $_.OutputKey -eq "BucketName" }).OutputValue
if (-not $WebsiteURL) { throw "No se pudo obtener WebsiteURL del frontend." }
Write-Host "Frontend Website URL: $WebsiteURL"

# 7) Subir build del front al bucket
$FrontDist = Join-Path $FrontDir "dist\front\browser"
Write-Host "Publicando build en s3://$BucketName/"
aws s3 sync $FrontDist "s3://$BucketName/" --delete | Out-Null

# 8) Actualizar CORS del backend con la URL del front
Write-Host "Actualizando CORS del backend con WebsiteURL..."
aws cloudformation deploy `
    --region $Region `
    --stack-name $BackStack `
    --template-file $BackTemplate `
    --capabilities CAPABILITY_NAMED_IAM `
    --parameter-overrides `
    ArtifactBucket=$ArtifactBucket `
    ArtifactKey=$ArtifactKey `
    PlatformArn="$PlatformArn" `
    MongoUri="$MongoUri" `
    AppCorsOrigin="$WebsiteURL" `
    InstanceType=$InstanceType | Out-Null

Write-Host ""
Write-Host "Despliegue completo"
Write-Host "   Backend:  http://$EBUrl"
Write-Host "   Frontend: $WebsiteURL"
Write-Host ""
Write-Host "Si re-compilas el front:"
Write-Host "  ng build --configuration production"
Write-Host "  aws s3 sync $FrontDist s3://$BucketName/ --delete"
