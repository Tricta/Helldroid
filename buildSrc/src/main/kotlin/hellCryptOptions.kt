import org.gradle.api.Project

open class hellCryptOptions(@Suppress("UNUSED_PARAMETER") project: Project) {
    companion object {
        internal lateinit var INSTANCE: hellCryptOptions
    }

    var obfuscationList = setOf<String>()
}