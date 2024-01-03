package joelwetzel.dimmer_minimums.tests

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
* Tests of behavior when an ON event is received.
*/
class SwitchOnTests extends Specification {
    // Creating a sandbox object for device script from file.
    private HubitatAppSandbox sandbox = new HubitatAppSandbox(new File('dimmer-minimums.groovy'))

    // Create mock log
    def log = Mock(Log)

    // Make AppExecutor return the mock log
    AppExecutor api = Mock { _ * getLog() >> log }

    private def constructMockDimmerDevice(String name, Map state) {
        def dimmerDevice = new DeviceInputValueFactory([Switch, SwitchLevel])
            .makeInputObject(name, 't',  DefaultAndUserValues.empty(), false)
        dimmerDevice.getMetaClass().state = state
        dimmerDevice.getMetaClass().on = { state.switch = "on" }
        dimmerDevice.getMetaClass().off = { state.switch = "off" }
        dimmerDevice.getMetaClass().setLevel = { int level -> state.level = level }

        return dimmerDevice
    }

    void "switchOnHandler() ensures minimum level"() {
        given:
        // Define a virtual dimmer device
        def dimmerDevice = constructMockDimmerDevice('n', [switch: "off", level: 0])

        // Run the app sandbox, passing the virtual dimmer device in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice], minimumLevel: 5, enableLogging: true],
            )

        when:
        dimmerDevice.on()
        script.switchOnHandler([deviceId: dimmerDevice.deviceId])

        then:
        1 * log.debug('n (0) ON detected')
        1 * log.debug('n setLevel(5)')
        dimmerDevice.state.level == 5
    }

    void "switchOnHandler() does not change level if above the minimum"() {
        given:
        // Define a virtual dimmer device
        def dimmerDevice = constructMockDimmerDevice('n', [switch: "off", level: 99])

        // Run the app sandbox, passing the virtual dimmer device in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice], minimumLevel: 5, enableLogging: true],
            )

        when:
        dimmerDevice.on()
        script.switchOnHandler([deviceId: dimmerDevice.deviceId])

        then:
        1 * log.debug('n (99) ON detected')
        dimmerDevice.state.level == 99
    }

    void "switchOnHandler() adjusts correct dimmer from among multiple devices"() {
        given:
        // Define two virtual dimmer devices
        def dimmerDevice1 = constructMockDimmerDevice('n1', [switch: "off", level: 0])
        def dimmerDevice2 = constructMockDimmerDevice('n2', [switch: "off", level: 0])

        // Run the app sandbox, passing the virtual dimmer devices in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice1, dimmerDevice2], minimumLevel: 5, enableLogging: true],
            )

        when:
        dimmerDevice2.on()
        script.switchOnHandler([deviceId: dimmerDevice2.deviceId])

        then:
        1 * log.debug('n2 (0) ON detected')
        1 * log.debug('n2 setLevel(5)')
        dimmerDevice2.state.level == 5
        dimmerDevice1.state.level == 0
    }
}
