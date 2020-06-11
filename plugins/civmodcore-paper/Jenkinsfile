@Library('civ_pipeline_lib')_

pipeline {
    agent any
    tools {
        maven 'Maven 3.6.3'
        jdk 'Java 8'
    }
     stages {
        stage ('Build') {
            steps {
                script {
                    if (env.BRANCH_NAME.startsWith('PR')) {
                        sh 'mvn -U clean package -P civ-jenkins'
                    } 
                    else {
                         sh 'mvn -U clean install deploy -P civ-jenkins'
                     } 
                }
            }
        }
        stage ('Trigger cascading builds') {
             when {
                expression {
                    env.BRANCH_NAME == 'master'
                }
            }
            steps {
                build job: '../NameLayer/master', wait: false
            }
        }
        stage ('Archive binaries') {
            steps {
                script {
                    def allJob = env.JOB_NAME.tokenize('/') as String[];
                    def projectName = allJob[1];
                    archiveArtifacts artifacts: "target/${projectName}-*.jar", fingerprint: true
                }
            }
        }
        stage ('Archive javadoc') {
            when {
                expression {
                    env.BRANCH_NAME == 'master'
                }
            }
            steps {
               step([$class: 'JavadocArchiver', javadocDir: 'target/apidocs', keepAll: false])
            }
        }
        stage ('Aggregate reports') {
            steps {
                script {
                    def java = scanForIssues tool: java()
                    def javadoc = scanForIssues tool: javaDoc()
                    publishIssues issues: [java, javadoc]
                    def checkstyle = scanForIssues tool: checkStyle(pattern: '**/target/checkstyle-result.xml')
                    publishIssues issues: [checkstyle]
                    def spotbugs = scanForIssues tool: spotBugs(pattern: '**/target/findbugsXml.xml')
                    publishIssues issues: [spotbugs]
                    def maven = scanForIssues tool: mavenConsole()
                    publishIssues issues: [maven]
                }
            }
        }
    }

    post {
        
           civ_discord_send()
        
    }
}
