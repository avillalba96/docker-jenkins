pipeline {
    agent any
    stages {
        stage('Obteniendo IMAGETAG del ECR') {
            steps {
                withCredentials([
          [$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_TERRAFORM_TOYOTA', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY_TERRAFORM_TOYOTA', credentialsId: 'toy-chile-usr_terraform']
        ]) {
                    script {
                        // Inicia sesión en ECR
                        sh 'aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin xxxxxx.dkr.ecr.us-east-1.amazonaws.com'
                        // Obtiene el IMAGETAG
                        def IMAGETAG = sh(
              returnStdout: true,
            script: """
                aws ecr describe-images --repository-name toyota-cl-tienda --region us-east-1 --query "sort_by(imageDetails, &imagePushedAt)[-1].imageTags" --output json | jq -r '.[]'
            """
            ).trim()
                        // Puedes almacenar IMAGETAG en una variable de entorno para usarlo en etapas posteriores
                        env.IMAGETAG = IMAGETAG
                    }
        }
            }
        }
        stage('Deploy STAGING') {
            steps {
                withKubeCredentials(kubectlCredentials: [[caCertificate: '''-----BEGIN CERTIFICATE-----
MIIDBTCCAe2gAwIBAgIIFC3o8vArGDswDQYJKoZIhvcNAQELBQAwFTETMBEGA1UE
0cyp7rK/ZtZ2
-----END CERTIFICATE-----''', clusterName: 'toyota-stagingV2', contextName: 'toyota-stagingV2', credentialsId: 'jenkong_staging', namespace: 'staging-tienda', serverUrl: 'https://XXXX.gr7.us-east-1.eks.amazonaws.com']]) {
                    sh '''kubectl get pods
              sed -e "s/IMAGEVERSION/$IMAGETAG/g" /var/jenkins_home/workspace/TOYOTA/EKS/GIT-EKS-PORTALES/portales-project/apps-yaml/toy-tienda/staging/02-deployment.jenkins > /var/jenkins_home/workspace/TOYOTA/EKS/GIT-EKS-PORTALES/portales-project/apps-yaml/toy-tienda/staging/02-deployment.yaml
              #sed -e 's/IMAGEVERSION/\${IMAGETAG}/g' /var/jenkins_home/workspace/TOYOTA/EKS/GIT-EKS-PORTALES/portales-project/apps-yaml/toy-tienda/staging/02-deployment.jenkins > /var/jenkins_home/workspace/TOYOTA/EKS/GIT-EKS-PORTALES/portales-project/apps-yaml/toy-tienda/staging/02-deployment.yaml

              kubectl apply -f /var/jenkins_home/workspace/TOYOTA/EKS/GIT-EKS-PORTALES/portales-project/apps-yaml/toy-tienda/staging/02-deployment.yaml
              echo "$IMAGETAG"'''
}
            }
            post {
                success {
                    slackSend botUser: true, tokenCredentialId: 'slack-jenkins-toyota', channel: '#deployments-toyota', color: 'good', message: "Despliegue exitoso TIENDA-STAGING v-${env.IMAGETAG} [#${env.BUILD_NUMBER}] - ${env.BUILD_URL}"
                }
                failure {
                    slackSend botUser: true, tokenCredentialId: 'slack-jenkins-toyota', channel: '#deployments-toyota', color: 'danger', message: "Despliegue fallido TIENDA-STAGING v-${env.IMAGETAG} [#${env.BUILD_NUMBER}] - ${env.BUILD_URL}"
                }
            }
        }
        stage('Deploy PROD') {
            steps {
                slackSend botUser: true, tokenCredentialId: 'slack-jenkins-toyota', channel: '#deployments-toyota', color: '#FF05FF', message: "Despliegue a TIENDA-PRODUCCION v-${env.IMAGETAG} [#${env.BUILD_NUMBER}] requiere aprobacion - ${env.BUILD_URL}pipeline-console/"
                timeout(time: 720, unit: 'MINUTES') {
                    input id: '49567799525369572', message: 'Aprueba el pasaje a PROD?'
                }
                withKubeCredentials(kubectlCredentials: [[caCertificate: '''-----BEGIN CERTIFICATE-----
MIIC/jCCAeagAwIBAgIBADANBgkqhkiG9w0BAQsFADAVMRMwEQYDVQQDEwprdWJl
-----END CERTIFICATE-----''', clusterName: 'eks-portales', contextName: 'eks-portales', credentialsId: 'jenkons', namespace: 'prod-tienda', serverUrl: 'https://XXXX.gr7.us-east-1.eks.amazonaws.com']]) {
                    sh '''kubectl get pods
              sed -e "s/IMAGEVERSION/$IMAGETAG/g" /var/jenkins_home/workspace/TOYOTA/EKS/GIT-EKS-PORTALES/portales-project/apps-yaml/toy-tienda/prod/02-deployment.jenkins > /var/jenkins_home/workspace/TOYOTA/EKS/GIT-EKS-PORTALES/portales-project/apps-yaml/toy-tienda/prod/02-deployment.yaml
              kubectl apply -f /var/jenkins_home/workspace/TOYOTA/EKS/GIT-EKS-PORTALES/portales-project/apps-yaml/toy-tienda/prod/02-deployment.yaml
              echo "$IMAGETAG"'''
}
            }
            post {
                success {
                    slackSend botUser: true, tokenCredentialId: 'slack-jenkins-toyota', channel: '#deployments-toyota', color: 'good', message: "Despliegue exitoso TIENDA-PRODUCCION v-${env.IMAGETAG} [#${env.BUILD_NUMBER}] - ${env.BUILD_URL}"
                }
                failure {
                    slackSend botUser: true, tokenCredentialId: 'slack-jenkins-toyota', channel: '#deployments-toyota', color: 'danger', message: "Despliegue fallido TIENDA-PRODUCCION v-${env.IMAGETAG} [#${env.BUILD_NUMBER}] - ${env.BUILD_URL}"
                }
                aborted {
                    slackSend botUser: true, tokenCredentialId: 'slack-jenkins-toyota', channel: '#deployments-toyota', color: 'warning', message: "Despliegue abortado TIENDA-PRODUCCION v-${env.IMAGETAG} [#${env.BUILD_NUMBER}] - ${env.BUILD_URL}"
                }
            }
        }
    }
}
