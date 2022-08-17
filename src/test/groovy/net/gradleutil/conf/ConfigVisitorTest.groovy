package net.gradleutil.conf

import net.gradleutil.conf.config.ConfigList
import net.gradleutil.conf.config.ConfigObject
import net.gradleutil.conf.config.ConfigValue
import net.gradleutil.conf.config.impl.ConfigVisitor

class ConfigVisitorTest extends AbstractTest {

    def "print all the things"() {
        setup:
        def configFile = getResourceText('conf/manytyped.conf')
        def config = Loader.resolveStringWithSystem(configFile)
        def visitor = new ConfigVisitor() {
            @Override
            void visitObject(String key, ConfigObject configValue) {
                println("visitObject ${key} : ${configValue.valueType()}")
            }

            @Override
            void visitList(String key, ConfigList list) {
                println("visitList ${key} : ${list.valueType()}")
            }

            @Override
            void visitString(String key, ConfigValue configValue) {
                println("visit ${key} : ${configValue.valueType()}")
            }

            @Override
            void visitNumber(String key, ConfigValue configValue) {
                println("visit ${key} : ${configValue.valueType()}")
            }

            @Override
            void visitNull(String key, ConfigValue configValue) {
                println("visit ${key} : ${configValue.valueType()}")
            }

            @Override
            void visitBoolean(String key, ConfigValue configValue) {
                println("visit ${key} : ${configValue.valueType()}")
            }

        }

        when:
        visitor.visit(config)

        then:
        true
    }

}
