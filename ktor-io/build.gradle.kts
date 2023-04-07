kotlin {
    sourceSets {
        commonTest {
            dependencies {
                api(project(":ktor-test-dispatcher"))
                api(project(":ktor-io:ktor-io-testing"))
            }
        }
    }
}
