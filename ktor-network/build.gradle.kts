description = "Ktor network utilities"

kotlin {
    createCInterop("network", posixTargets()) {
        defFile = projectDir.resolve("posix/interop/network.def")
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":ktor-utils"))
            }
        }

        commonTest {
            dependencies {
                api(project(":ktor-test-dispatcher"))
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.mockk)
            }
        }
    }
}
