pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                script {
                    // Agregar la línea para permitir el directorio en Git
                    sh 'git config --global --add safe.directory /var/jenkins_home/workspace/TOYOTA/EKS/TOY-TIENDA/BUILD-TO-ECR'

                    git credentialsId: 'bitbucket.tienda', url: 'https://bitbucket.org/xxxxxx/toyota.cl.git'
                    env.gitCommit = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
                    sh 'echo $gitCommit'
                }
            }
        }

        stage('Build') {
            steps {
                sh "docker build -t xxxxxx.dkr.ecr.us-east-1.amazonaws.com/toyota-cl-tienda:${env.gitCommit} ."
            }
        }

        stage('Push') {
            steps {
                script {
                        withCredentials([
                            [$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_TERRAFORM_TOYOTA', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY_TERRAFORM_TOYOTA', credentialsId: 'toy-chile-usr_terraform']
                        ]) {
                            // Inicia sesión en ECR
                            sh 'aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin xxxxxx.dkr.ecr.us-east-1.amazonaws.com'
                            // Push to ECR
                            sh "docker push xxxxxx.dkr.ecr.us-east-1.amazonaws.com/toyota-cl-tienda:${env.gitCommit}"
                            // docker image prune
                            //sh 'yes | docker container prune && yes | docker volume prune && yes | docker system prune -a && yes | docker image prune && yes | docker network prune'
                        }
                }
            }
        }
    }
}
