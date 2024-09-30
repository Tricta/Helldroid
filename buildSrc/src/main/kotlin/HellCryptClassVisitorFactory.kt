import codeGen.HellCryptClassVisitor
import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

@Suppress("UnstableApiUsage")
abstract class HellCryptClassVisitorFactory: AsmClassVisitorFactory<InstrumentationParameters> {
    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
        return HellCryptClassVisitor(Opcodes.ASM7, nextClassVisitor)
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return classData.classAnnotations.contains("hellCrypt") ||
                hellCryptOptions.INSTANCE.obfuscationList.parallelStream().anyMatch { classData.className.startsWith(it) }
    }
}