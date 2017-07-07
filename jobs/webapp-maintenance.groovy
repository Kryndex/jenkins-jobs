// Pipeline job that does weekly maintenance on our webapp repo, other
// repos, the jenkins machine itself, etc.
// Tasks include things like:
//    * compressing all png and svg images
//    * cleaning out old docker containers
//    * deleting obsolete translations files
// etc.

@Library("kautils")
// Classes we use, under jenkins-tools/src/.
import org.khanacademy.Setup;
// Vars we use, under jenkins-tools/vars/.  This is just for documentation.
//import vars.exec
//import vars.kaGit
//import vars.notify
//import vars.onMaster


new Setup(steps

).addCronSchedule("H H * * 0"

).apply();


def runScript() {
   onMaster('6h') {
      kaGit.safeSyncTo("git@github.com:Khan/webapp", "master");
      sh("jenkins-tools/weekly-maintenance.sh");
   }
}


notify([slack: [channel: '#infrastructure',
                sender: 'Mr Monkey',
                emoji: ':monkey_face:',
                when: ['SUCCESS', 'FAILURE', 'UNSTABLE', 'ABORTED']],
        email: [to: 'jenkins-admin+builds, csilvers',
                when: ['BACK TO NORMAL', 'FAILURE', 'UNSTABLE']],
        aggregator: [initiative: 'infrastructure',
                     when: ['BACK TO NORMAL', 'FAILURE', 'UNSTABLE']]]) {
   stage("Running maintenance") {
      runScript();
   }
}
