def call(String platformVersion) {
    def workspace = "${Paths.jenkinsHome}/installer"
    def installerSrc = "${Paths.jenkinsHome}/installer-src"
    def installerBin = "${workspace}/install-bin"

    sh "rm -rf ${workspace}"
    sh "cp -r server/src/installer/. ${workspace}"
    sh "chmod -R 777 ${workspace}"

    sh "cp -lr ${installerSrc}/* ${installerBin}"

    sh "mvn dependency:copy -Dartifact=lsfusion.platform:server:${platformVersion}:jar:assembly -DoutputDirectory=${installerBin}"
    sh "mvn dependency:copy -Dartifact=lsfusion.platform:desktop-client:${platformVersion}:jar:assembly -DoutputDirectory=${installerBin}"
    sh "mvn dependency:copy -Dartifact=lsfusion.platform:web-client:${platformVersion}:war -DoutputDirectory=${installerBin}"

    dir(installerBin) {
        sh "mv -f server-${platformVersion}-assembly.jar lsfusion-server-${platformVersion}.jar"
        sh "mv -f desktop-client-${platformVersion}-assembly.jar lsfusion-client-${platformVersion}.jar"
        sh "mv -f web-client-${platformVersion}.war lsfusion-client-${platformVersion}.war"
    }

    dir(workspace) {
        def makensis = "${installerSrc}/nsis-unicode-win/makensis.exe"
        def downloadDir = "${Paths.download}/${platformVersion}"

        sh "echo '\n!define LSFUSION_VERSION ${platformVersion}' >> Versions.nsh"
        String viVersion = platformVersion.replace('beta', '999') + '.0'
        if (!platformVersion.contains('beta')) {
            viVersion += '.0'
        }
        sh "echo '\n!define VI_LSFUSION_VERSION ${viVersion}' >> Versions.nsh"
        
        sh "mkdir -p ${downloadDir}"
        
        sh "wine ${makensis} Installer-x32.nsi"
        sh "chmod -x x32.exe"
        sh "cp -f x32.exe ${downloadDir}/lsfusion-${platformVersion}.exe"
        
        sh "wine ${makensis} Installer-x32-dev.nsi"
        sh "chmod -x x32-dev.exe"
        sh "cp -f x32-dev.exe ${downloadDir}/lsfusion-dev-${platformVersion}.exe"

        sh "wine ${makensis} Installer-x64.nsi"
        sh "chmod -x x64.exe"
        sh "cp -f x64.exe ${downloadDir}/lsfusion-${platformVersion}-x64.exe"

        sh "wine ${makensis} Installer-x64-dev.nsi"
        sh "chmod -x x64-dev.exe"
        sh "cp -f x64-dev.exe ${downloadDir}/lsfusion-dev-${platformVersion}-x64.exe"
    }
}
