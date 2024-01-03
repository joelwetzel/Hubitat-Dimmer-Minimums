# Dimmer Minimums app for Hubitat
[![Build/Test State](https://github.com/joelwetzel/Hubitat-Dimmer-Minimums/actions/workflows/gradle.yml/badge.svg)](https://github.com/joelwetzel/Hubitat-Dimmer-Minimums/actions/workflows/gradle.yml)

An app for Hubitat to enforce a minimum level for smart switches/lights that don't have a config setting for it.

## Use Case
Some dimmable lights (such as some LEDs) will dim to very low levels, but will not initially turn on at those levels.  For example, an LED that can be dimmed to 1%, but must be at 5% or more to turn on from off.  This app enforces a minimum dimming level for dimmers/lights that don't have their own minimum brightness settings, to ensure fast and accurate startup.

It does this in two ways:
1. If the lights are on and you dim them below the minimum, it raises them back up to the minimum.
2. If the lights are off and are below the minimum when you turn them on, it will raise them up to the minimum.

This is to prevent the situation where the switch turns on at 1% but the lights don't actually come on, confusing the user.

## End-User Installation

The best way to install this code is by using [Hubitat Package Manager](https://community.hubitat.com/t/beta-hubitat-package-manager).

However, it can also be installed manually, by copying the contents of dimmer-minimums.groovy into your Hubitat admin site.

## Developer Notes
*The following notes are for myself when working on the code:*

### Prereqs
- Gradle 8.5
- OpenJDK 11
- [Groovy](https://groovy-lang.org/install.html)

### Unit Tests
Unit tests are stored in the /tests folder.

They rely on having an environment variable set, called **MAVENKEY**.  It should be a Github Personal Access Token.

You can run them in the terminal like so:
>./gradlew build

They will also be run automatically by the Github [workflow](https://github.com/joelwetzel/Hubitat-Dimmer-Minimums/actions/workflows/gradle.yml).  The workflow runs the tests and tracks test results.  It does NOT do any publishing to HPM.

### Testing Fixture
- The unit tests make use of the [Hubitat CI](https://github.com/biocomp/hubitat_ci) package from #biocomp.  It's a fixture that emulates some of the Hubitat system, and helps with unit testing Hubitat apps and drivers.
- However, I extended it, so for now, this app's build.gradle uses my own fork:
  - Repo: https://github.com/joelwetzel/hubitat_ci
  - Maven artifacts: https://github.com/joelwetzel/hubitat_ci/packages/2028932
