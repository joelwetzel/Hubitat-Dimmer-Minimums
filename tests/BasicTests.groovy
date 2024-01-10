package joelwetzel.dimmer_minimums.tests

import joelwetzel.dimmer_minimums.mockDeviceFactories.MockDimmerFactory
import joelwetzel.dimmer_minimums.utils.SubscribingAppExecutor

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

    def api = Spy(SubscribingAppExecutor) {
        _*getLog() >> log
    }

    def dimmerFactory = new MockDimmerFactory()

    void "Basic validation"() {
        given:

        expect:
        // Compile, construct script object, and validate definition() and preferences()
        sandbox.run()
    }

    void "installed() logs the settings"() {
        given:
        def dimmerDevice = dimmerFactory.constructDevice('n')

        // Run the app sandbox, passing the virtual dimmer device in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice], minimumLevel: 5, enableLogging: true])
        api.setScript(script)

        when:
        // Run installed() method on app script.
        script.installed()

        then:
        // Expect that log.info() was called with this string
        1 * log.info('Installed with settings: [dimmers:[GeneratedDevice(input: n, type: t)], minimumLevel:5, enableLogging:true]')
    }

    void "initialize() subscribes to events"() {
        given:
        def dimmerDevice = dimmerFactory.constructDevice('n')

        // Run the app sandbox, passing the virtual dimmer device in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice], minimumLevel: 5, enableLogging: true])
        api.setScript(script)

        when:
        // Run initialize() method on app script.
        api.getMetaClass().subscribe = { Object toWhat, String attributeNameOrNameAndValueOrEventName, Object handler -> this.subscribe(toWhat, attributeNameOrNameAndValueOrEventName, handler) }
        script.initialize()

        then:
        // Expect that events are subscribe to
        1 * api.subscribe([dimmerDevice], 'level', 'levelHandler')
        1 * api.subscribe([dimmerDevice], 'switch.on', 'switchOnHandler')
    }
}
