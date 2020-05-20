pipeline {
    agent any
    tools {
        maven 'Maven latest'
        jdk 'OpenJDK 11'
    }
    stages {
        stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
            }
        }

        stage ('Build') {
            steps {
                sh 'mvn clean install -U javadoc:jar -DadditionalJOption=-Xdoclint:none' 
            }
        }
        stage ('Javadoc') {
            when {
                branch "master"
            }
            steps {
                sh 'mvn javadoc:javadoc -DskipTests -DadditionalJOption=-Xdoclint:none '
                step([$class: 'JavadocArchiver',
                        javadocDir: 'target/site/apidocs',
                        keepAll: false])
            }
        }
        stage ('Deploy') {
            when {
                branch "master"
            }
            steps {
                sh 'mvn javadoc:jar source:jar deploy -DskipTests -DadditionalJOption=-Xdoclint:none '
            }
        }
    }
}