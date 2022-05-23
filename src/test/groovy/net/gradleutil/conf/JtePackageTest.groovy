package net.gradleutil.conf

import net.gradleutil.conf.AbstractTest
import net.gradleutil.conf.util.ConfUtil
import net.gradleutil.gen.JtePackage

class JtePackageTest extends AbstractTest  {

    def "jte package is great"() {
        setup:
        def jarDir = new File(base, "jar")
        def jtePackage = new JtePackage()
        extractFiles('mhf/gradleplugin', baseDir)
        def pluginDir = new File(baseDir, 'mhf/gradleplugin')

        when:
        jtePackage.execute(pluginDir.toPath(), jarDir.toPath(), 'net.gradleutil.conf.temp.jtepackagetest.jar')

        then:
        true

    }

}
