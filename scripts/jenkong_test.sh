#!/bin/bash

export CLUSTER_ENDPOINT="https://XXXX.gr7.us-east-1.eks.amazonaws.com"
export CA_CERTIFICATE_BASE64="XXXX"
export TOKEN="XXXX"
export NAMESPACE="staging-autonauta"

# Decodificar el certificado CA de base64 y escribirlo en un archivo temporal
echo "$CA_CERTIFICATE_BASE64" | base64 -d > /tmp/ca.crt

# Ejecutar el comando kubectl con el certificado CA decodificado desde el archivo temporal
kubectl get pods --server="$CLUSTER_ENDPOINT" --certificate-authority=/tmp/ca.crt --token="$TOKEN" --namespace="$NAMESPACE"

# Limpiar el archivo temporal después de usarlo
rm /tmp/ca.crt
