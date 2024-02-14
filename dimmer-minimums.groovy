/**
 *  Dimmer Minimums v1.4
 *
 *  Copyright 2019 Joel Wetzel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

definition(
    name: 'Dimmer Minimums',
    namespace: 'joelwetzel',
    author: 'Joel Wetzel',
    description: "Some dimmable lights (such as some LEDs) will dim to very low levels,\
     but will not initially turn on at those levels.  For example, an LED that can be dimmed\
     to 1%, but must be at 5% or more to turn on from off.  This app enforces that minimum\
     dimming level for dimmers/lights that don't have their own minimum brightness settings,\
     to ensure fast and accurate startup.",
    category: 'Lighting',
    iconUrl: '',
    iconX2Url: '',
    iconX3Url: '')

def dimmers = [
        name:                'dimmers',
        type:                'capability.switchLevel',
        title:                'Dimmers to control',
        description:        'Select the dimmers to control.',
        multiple:            true,
        required:            true
    ]

def minimumLevel = [
        name:                'minimumLevel',
        type:                'number',
        title:                'Minimum Brightness Level that will be allowed for these dimmers.',
        defaultValue:        5,
        required:            true
    ]

def enableLogging = [
        name:                'enableLogging',
        type:                'bool',
        title:                'Enable Debug Logging?',
        defaultValue:        false,
        required:            true
    ]

preferences {
    page(name: 'mainPage', title: 'Preferences', install: true, uninstall: true) {
        section(getFormat('title', 'Dimmer Minimums')) {
            paragraph "Some dimmable lights (such as some LEDs) will dim to very low levels,\
                but will not initially turn on at those levels.  For example, an LED that can be dimmed\
                to 1%, but must be at 5% or more to turn on from off.  This app enforces that minimum\
                dimming level for dimmers/lights that don't have their own minimum brightness settings,\
                to ensure fast and accurate startup."
        }
        section('') {
            input dimmers
            input minimumLevel
        }
        section() {
            input enableLogging
        }
    }
}

def installed() {
    log.info "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.info "Updated with settings: ${settings}"

    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(dimmers, 'level', levelHandler)
    subscribe(dimmers, 'switch.on', switchOnHandler)
}

def levelHandler(evt) {
    def triggeredDevice = dimmers.find { it.deviceId == evt.deviceId }
    def newLevel = evt.value.toInteger()

    log "${triggeredDevice.displayName} LEVEL CHANGE detected (${newLevel})"

    if (newLevel < minimumLevel
        && newLevel > 0
        && triggeredDevice.currentValue('switch') == 'on') {
        triggeredDevice.setLevel(minimumLevel)
        log "${triggeredDevice.displayName} setLevel(${minimumLevel})"
    }
}

def switchOnHandler(evt) {
    def triggeredDevice = dimmers.find { it.deviceId == evt.deviceId }
    def currentLevel = triggeredDevice.currentValue('level')

    log "${triggeredDevice.displayName} (${currentLevel}) ON detected"

    if (currentLevel < minimumLevel) {
        triggeredDevice.setLevel(minimumLevel)
        log "${triggeredDevice.displayName} setLevel(${minimumLevel})"
    }
}

def getFormat(type, myText='') {
    if (type == 'header-green') return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if (type == 'line') return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
    if (type == 'title') return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def log(msg) {
    if (enableLogging) {
        log.debug msg
    }
}
