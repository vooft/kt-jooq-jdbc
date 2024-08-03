dependencies {
    compileOnly(libs.jooq)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.testcontainers.postgres)
    testImplementation(libs.pg.jdbc)
    testImplementation(libs.hikaricp)
    testImplementation(libs.jooq)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.junit)
    testImplementation(libs.blockhound)
    testImplementation(libs.kotlinx.coroutines.debug)
}
