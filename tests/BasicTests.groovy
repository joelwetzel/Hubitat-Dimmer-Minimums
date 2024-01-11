package joelwetzel.dimmer_minimums.tests

import me.biocomp.hubitat_ci.util.device_fixtures.DimmerFixture
import me.biocomp.hubitat_ci.util.AppExecutorWithEventForwarding

import me.biocomp.hubitat_ci.api.app_api.AppExecutor
import me.biocomp.hubitat_ci.api.common_api.Log
import me.biocomp.hubitat_ci.app.HubitatAppSandbox
import me.biocomp.hubitat_ci.api.common_api.DeviceWrapper
import me.biocomp.hubitat_ci.app.preferences.DeviceInputValueFactory
import me.biocomp.hubitat_ci.capabilities.GeneratedCapability
import me.biocomp.hubitat_ci.capabilities.Switch
import me.biocomp.hubitat_ci.capabilities.SwitchLevel
import me.biocomp.hubitat_ci.util.NullableOptional
import me.biocomp.hubitat_ci.validation.DefaultAndUserValues
import me.biocomp.hubitat_ci.validation.Flags
import me.biocomp.hubitat_ci.validation.GeneratedDeviceInputBase

import spock.lang.Specification

/**
* Basic tests for dimmer-minimums.groovy
*/
class BasicTests extends Specification {
    private HubitatAppSandbox sandbox = new HubitatAppSandbox(new File('dimmer-minimums.groovy'))

    def log = Mock(Log)

    def appExecutor = Spy(AppExecutorWithEventForwarding) {
        _*getLog() >> log
    }

    void "Basic validation of app script"() {
        expect:
        sandbox.run()
    }

    void "installed() logs the settings"() {
        given:
        def dimmerFixture = DimmerFixture.create('n')

        // Run the app sandbox, passing the virtual dimmer device in.
        def appScript = sandbox.run(api: appExecutor,
            userSettingValues: [dimmers: [dimmerFixture], minimumLevel: 5, enableLogging: true])
        appExecutor.setSubscribingScript(appScript)

        when:
        // Run installed() method on app script.
        appScript.installed()

        then:
        // Expect that log.info() was called with this string
        1 * log.info('Installed with settings: [dimmers:[GeneratedDevice(input: n, type: t)], minimumLevel:5, enableLogging:true]')
    }

    void "initialize() subscribes to events"() {
        given:
        def dimmerFixture = DimmerFixture.create('n')

        // Run the app sandbox, passing the dimmer fixture in.
        def appScript = sandbox.run(api: appExecutor,
            userSettingValues: [dimmers: [dimmerFixture], minimumLevel: 5, enableLogging: true])
        appExecutor.setSubscribingScript(appScript)

        when:
        appScript.initialize()

        then:
        // Expect that events are subscribe to
        1 * appExecutor.subscribe([dimmerFixture], 'level', 'levelHandler')
        1 * appExecutor.subscribe([dimmerFixture], 'switch.on', 'switchOnHandler')
    }
}
