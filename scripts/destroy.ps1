Param(
  [string]$Region = "us-east-1",
  [string]$BackStack = "btg-backend",
  [string]$FrontStack = "btg-frontend"
)

$ErrorActionPreference = "Stop"

Write-Host "🗑️ Eliminando stacks CloudFormation..."
aws cloudformation delete-stack --region $Region --stack-name $FrontStack
aws cloudformation delete-stack --region $Region --stack-name $BackStack

Write-Host "⏳ Esperando eliminación..."
try { aws cloudformation wait stack-delete-complete --region $Region --stack-name $FrontStack } catch {}
try { aws cloudformation wait stack-delete-complete --region $Region --stack-name $BackStack } catch {}

Write-Host "✅ Stacks eliminados."
Write-Host "Si creaste buckets de artefactos o sitio y quieres borrarlos, hacelo con:"
Write-Host "  aws s3 rm s3://<bucket> --recursive; aws s3 rb s3://<bucket>"
