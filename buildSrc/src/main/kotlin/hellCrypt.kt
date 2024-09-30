import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import hellCryptOptions
import org.gradle.api.Plugin
import org.gradle.api.Project

class hellCrypt: Plugin<Project>{
    companion object{
        const val PLUGIN_NAME = "hellCrypt"
    }

    override fun apply(project: Project) {
        project.configurations.create(PLUGIN_NAME)

        hellCryptOptions.INSTANCE = project.extensions.create(PLUGIN_NAME, hellCryptOptions::class.java, project)

        val androidComponents = project.extensions.findByType(AndroidComponentsExtension::class.java)
        androidComponents?.onVariants { variant ->
            variant.instrumentation.transformClassesWith(HellCryptClassVisitorFactory::class.java, InstrumentationScope.ALL) {}
            variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
        }
    }
}