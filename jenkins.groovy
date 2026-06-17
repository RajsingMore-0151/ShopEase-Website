pipeline {
    agent any

    environment {
        GIT_REPO      = 'https://github.com/RajsingMore-0151/ShopEase-Website.git'
        GIT_BRANCH    = 'main'

        TARGET_USER   = 'ubuntu'
        TARGET_IP     = '172.31.20.47'

        TARGET_PATH   = '/var/www/html/'

        SSH_CRED_ID   = 'linux'
    }

    stages {

        stage('Clone Repository') {
            steps {
                git branch: "${GIT_BRANCH}",
                    url: "${GIT_REPO}"

                echo "Repository cloned successfully."
            }
        }

        stage('Copy Files to Target Server') {
            steps {
                sshagent(credentials: ["${SSH_CRED_ID}"]) {
                    sh """
                        scp -o StrictHostKeyChecking=no \
                        -r * \
                        ${TARGET_USER}@${TARGET_IP}:${TARGET_PATH}/
                    """
                }
            }
        }

        stage('Restart Nginx on Target Server') {
            steps {
                sshagent(credentials: ["${SSH_CRED_ID}"]) {
                    sh """
                        ssh -o StrictHostKeyChecking=no \
                        ${TARGET_USER}@${TARGET_IP} \
                        'sudo systemctl restart nginx && sudo systemctl status nginx --no-pager'
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Website deployed successfully."
        }

        failure {
            echo "Deployment failed."
        }
    }
}