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
* Tests of behavior when a LEVEL event is received.
*/
class SetLevelTests extends Specification {
    private static def getDevice(def from) {
        if (DeviceWrapper.isInstance(from)) {
            from
        } else {
            from[0]
        }
    }

    // Creating a sandbox object for device script from file.
    private HubitatAppSandbox sandbox = new HubitatAppSandbox(new File('dimmer-minimums.groovy'))

    private def constructMockDimmerDevice(String name, Map state) {
        def dimmerDevice = getDevice(new DeviceInputValueFactory([Switch, SwitchLevel])
            .makeInputObject(name, 't',  DefaultAndUserValues.empty(), false))
        dimmerDevice.getMetaClass().state = state
        dimmerDevice.getMetaClass().on = { state.switch = "on" }
        dimmerDevice.getMetaClass().off = { state.switch = "off" }
        dimmerDevice.getMetaClass().setLevel = { int level -> state.level = level }

        return dimmerDevice
    }

    // void setup() {

    // }

    void "levelHandler() ensures minimum level"() {
        given:
        // Create mock log
        def log = Mock(Log)

        // Make AppExecutor return the mock log
        AppExecutor api = Mock { _ * getLog() >> log }

        // Define a virtual dimmer device
        def dimmerDevice = constructMockDimmerDevice('n', [switch: "on", level: 99])

        // Run the app sandbox, passing the virtual dimmer device in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice], minimumLevel: 5, enableLogging: true],
            )

        when:
        // The framework does not yet actually implement event subscriptions, so we have
        // to call the app's handler directly after setting level on the device.
        dimmerDevice.setLevel(2)
        script.levelHandler([deviceId: dimmerDevice.deviceId, value: 2])

        then:
        1 * log.debug('n LEVEL CHANGE detected (2)')
        1 * log.debug('n setLevel(5)')
        dimmerDevice.state.level == 5
    }

    void "levelHandler() does not change level if dimmer is off"() {
        given:
        // Create mock log
        def log = Mock(Log)

        // Make AppExecutor return the mock log
        AppExecutor api = Mock { _ * getLog() >> log }

        // Define a virtual dimmer device
        def dimmerDevice = constructMockDimmerDevice('n', [switch: "off", level: 99])

        // Run the app sandbox, passing the virtual dimmer device in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice], minimumLevel: 5, enableLogging: true],
            )

        when:
        // The framework does not yet actually implement event subscriptions, so we have
        // to call the app's handler directly after setting level on the device.
        dimmerDevice.setLevel(2)
        script.levelHandler([deviceId: dimmerDevice.deviceId, value: 2])

        then:
        1 * log.debug('n LEVEL CHANGE detected (2)')
        dimmerDevice.state.level == 2
    }

    void "levelHandler() does not change level if above the minimum"() {
        given:
        // Create mock log
        def log = Mock(Log)

        // Make AppExecutor return the mock log
        AppExecutor api = Mock { _ * getLog() >> log }

        // Define a virtual dimmer device
        def dimmerDevice = constructMockDimmerDevice('n', [switch: "on", level: 99])

        // Run the app sandbox, passing the virtual dimmer device in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice], minimumLevel: 5, enableLogging: true],
            )

        when:
        dimmerDevice.setLevel(80)
        script.levelHandler([deviceId: dimmerDevice.deviceId, value: 80])

        then:
        1 * log.debug('n LEVEL CHANGE detected (80)')
        dimmerDevice.state.level == 80
    }

    void "levelHandler() adjusts correct dimmer from among multiple devices"() {
        given:
        // Create mock log
        def log = Mock(Log)

        // Make AppExecutor return the mock log
        AppExecutor api = Mock { _ * getLog() >> log }

        // Define a virtual dimmer device
        def dimmerDevice1 = constructMockDimmerDevice('n1', [switch: "on", level: 99])

        // Define a second virtual dimmer device
        def dimmerDevice2 = constructMockDimmerDevice('n2', [switch: "on", level: 99])

        // Run the app sandbox, passing the virtual dimmer devices in.
        def script = sandbox.run(api: api,
            userSettingValues: [dimmers: [dimmerDevice1, dimmerDevice2], minimumLevel: 5, enableLogging: true],
            )

        when:
        dimmerDevice2.setLevel(2)
        script.levelHandler([deviceId: dimmerDevice2.deviceId, value: 2])

        then:
        1 * log.debug('n2 LEVEL CHANGE detected (2)')
        1 * log.debug('n2 setLevel(5)')
        dimmerDevice2.state.level == 5
        dimmerDevice1.state.level == 99
    }
}
