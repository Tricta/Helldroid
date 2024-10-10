package codeGen

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import hellCryptOptions

class MainActivityClassVisitor(api: Int, classVisitor: ClassVisitor) : ClassVisitor(api, classVisitor) {
    private var className: String? = null

    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
        className = name
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        return if (className?.contains("MainActivity") == true && name == "onCreate") {
            MainActivityOnCreateMethodVisitor(api, mv)
        } else {
            mv
        }
    }

    override fun visitEnd() {
        if (className?.contains("MainActivity") == true) {
            val mv = super.visitMethod(
                    Opcodes.ACC_PUBLIC or Opcodes.ACC_NATIVE,
                    "ReplaceMethodByObject",
                    "(Ljava/lang/Object;Ljava/lang/Object;)V",
                    null,
                    null
            )

            mv.visitEnd()
        }

        super.visitEnd()
    }

    private class MainActivityOnCreateMethodVisitor(api: Int, mv: MethodVisitor) : MethodVisitor(api, mv) {

        private var foundSuperOnCreate = false
        var pkgMainApp: String? = hellCryptOptions.INSTANCE.obfuscationList.firstOrNull()?.replace(".", "/")

        override fun visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)

            // Check if we are invoking 'super.onCreate(Bundle savedInstanceState)'
            if (opcode == Opcodes.INVOKESPECIAL &&
                    name == "onCreate" &&
                    descriptor == "(Landroid/os/Bundle;)V") {

                foundSuperOnCreate = true
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/lib/hellcrypt/hellcrypt", "init", "()V", false)

                loadLibraryBlock()
                StackHook()
            }
        }

        override fun visitInsn(opcode: Int) {
            if (opcode == Opcodes.RETURN && foundSuperOnCreate) {
                endStackCheck()
            }

            super.visitInsn(opcode)
        }

        private fun loadLibraryBlock() {
            val startLabel = Label()
            val endLabel = Label()
            val handlerLabel = Label()

            // Add a try-catch block for Exception
            mv.visitTryCatchBlock(startLabel, endLabel, handlerLabel, "java/lang/Exception")
            mv.visitLabel(startLabel)

            // Load MainActivity class
            mv.visitLdcInsn(Type.getType("Lcom/lib/helldroid/Helldroid;"))

            // Load String = LoadLibrary
            mv.visitLdcInsn("LoadLibrary")

            // Create an empty array
            mv.visitInsn(Opcodes.ICONST_2)
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Class")

            //array at index 0
            mv.visitInsn(Opcodes.DUP)
            mv.visitInsn(Opcodes.ICONST_0)
            mv.visitLdcInsn(Type.getType("Landroid/content/Context;"))
            mv.visitInsn(Opcodes.AASTORE)

            //array at index 1
            mv.visitInsn(Opcodes.DUP)
            mv.visitInsn(Opcodes.ICONST_1)
            mv.visitLdcInsn(Type.getType("Ljava/lang/Class;"))
            mv.visitInsn(Opcodes.AASTORE)

            // getDeclaredMethod
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/Class",
                    "getDeclaredMethod",
                    "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
                    false
            )
            mv.visitVarInsn(Opcodes.ASTORE, 3);

            // Set the method accessible
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            mv.visitInsn(Opcodes.ICONST_1)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "setAccessible", "(Z)V", false)

            // Get LoadLibrary var and Null Param
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            mv.visitInsn(Opcodes.ACONST_NULL)

            // Create an array for Invoke Params
            mv.visitInsn(Opcodes.ICONST_2)
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object")

            //array at index 0
            mv.visitInsn(Opcodes.DUP)
            mv.visitInsn(Opcodes.ICONST_0)
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitInsn(Opcodes.AASTORE)

            //array at index 1
            mv.visitInsn(Opcodes.DUP)
            mv.visitInsn(Opcodes.ICONST_1)
            mv.visitLdcInsn(Type.getType("L$pkgMainApp/MainActivity;"))
            mv.visitInsn(Opcodes.AASTORE)

            //Invoke
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/reflect/Method",
                    "invoke",
                    "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
                    false
            )

            mv.visitInsn(Opcodes.POP)

            // Mark the end of the try block
            mv.visitLabel(endLabel)
            val afterCatchLabel = Label()
            mv.visitJumpInsn(Opcodes.GOTO, afterCatchLabel)

            // Exception handler block
            mv.visitLabel(handlerLabel)
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/app/Activity", "finish", "()V", false)
            mv.visitInsn(Opcodes.POP)
            mv.visitLabel(afterCatchLabel)
        }

        private fun StackHook() {
            val startLabel = Label()
            val endLabel = Label()
            val handlerLabel = Label()

            // Add a try-catch block for Exception
            mv.visitTryCatchBlock(startLabel, endLabel, handlerLabel, "java/lang/Exception")
            mv.visitLabel(startLabel)

            // Load Helldroid class
            mv.visitLdcInsn(Type.getType("Lcom/lib/helldroid/Helldroid;"))

            // Load String = createDummyFile
            mv.visitLdcInsn("createDummyFile")

            // Create an empty array
            mv.visitInsn(Opcodes.ICONST_1)
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Class")

            //array at index 0
            mv.visitInsn(Opcodes.DUP)
            mv.visitInsn(Opcodes.ICONST_0)
            mv.visitLdcInsn(Type.getType("Landroid/content/Context;"))
            mv.visitInsn(Opcodes.AASTORE)

            // getDeclaredMethod
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/Class",
                    "getDeclaredMethod",
                    "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
                    false
            )
            mv.visitVarInsn(Opcodes.ASTORE, 3);


            //line 24
            // Load Helldroid class
            mv.visitLdcInsn(Type.getType("Lcom/lib/helldroid/Helldroid;"))

            // Load String = LoadLibrary
            mv.visitLdcInsn("checkMainTrace")
            mv.visitInsn(Opcodes.ACONST_NULL)

            // getDeclaredMethod
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/Class",
                    "getDeclaredMethod",
                    "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
                    false
            )
            mv.visitVarInsn(Opcodes.ASTORE, 4);

            //line 26
            // Load MainActivity class
            mv.visitLdcInsn(Type.getType("L$pkgMainApp/MainActivity;"))

            // Load String = ReplaceMethodByObject
            mv.visitLdcInsn("ReplaceMethodByObject")

            // Create an empty array
            mv.visitInsn(Opcodes.ICONST_2)
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Class")

            //array at index 0
            mv.visitInsn(Opcodes.DUP)
            mv.visitInsn(Opcodes.ICONST_0)
            mv.visitLdcInsn(Type.getType("Ljava/lang/Object;"))
            mv.visitInsn(Opcodes.AASTORE)

            //array at index 1
            mv.visitInsn(Opcodes.DUP)
            mv.visitInsn(Opcodes.ICONST_1)
            mv.visitLdcInsn(Type.getType("Ljava/lang/Object;"))
            mv.visitInsn(Opcodes.AASTORE)

            // getDeclaredMethod
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/Class",
                    "getDeclaredMethod",
                    "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
                    false
            )
            mv.visitVarInsn(Opcodes.ASTORE, 5);

            // 3 -> targetMethod
            // 4 -> replacementMethod
            // 5 -> replaceMethodHook

            // Set the method accessible
            mv.visitVarInsn(Opcodes.ALOAD, 5);
            mv.visitInsn(Opcodes.ICONST_1)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "setAccessible", "(Z)V", false)

            // Get LoadLibrary var and Null Param
            mv.visitVarInsn(Opcodes.ALOAD, 5);
            mv.visitVarInsn(Opcodes.ALOAD, 0);

            // Create an array for Invoke Params
            mv.visitInsn(Opcodes.ICONST_2)
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object")

            //array at index 0
            mv.visitInsn(Opcodes.DUP)
            mv.visitInsn(Opcodes.ICONST_0)
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            mv.visitInsn(Opcodes.AASTORE)

            //array at index 1
            mv.visitInsn(Opcodes.DUP)
            mv.visitInsn(Opcodes.ICONST_1)
            mv.visitVarInsn(Opcodes.ALOAD, 4);
            mv.visitInsn(Opcodes.AASTORE)

            //Invoke
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/reflect/Method",
                    "invoke",
                    "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
                    false
            )

            mv.visitInsn(Opcodes.POP)

            // Mark the end of the try block
            mv.visitLabel(endLabel)
            val afterCatchLabel = Label()
            mv.visitJumpInsn(Opcodes.GOTO, afterCatchLabel)

            // Exception handler block
            mv.visitLabel(handlerLabel)
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/app/Activity", "finish", "()V", false)
            mv.visitInsn(Opcodes.POP)
            mv.visitLabel(afterCatchLabel)
        }

        private fun endStackCheck() {
            val startLabel = Label()
            val endLabel = Label()
            val handlerLabel = Label()

            // Add a try-catch block for Exception
            mv.visitTryCatchBlock(startLabel, endLabel, handlerLabel, "java/lang/Exception")
            mv.visitLabel(startLabel)

            // Load MainActivity class
            mv.visitLdcInsn(Type.getType("Lcom/lib/helldroid/Helldroid;"))

            // Load String = createDummyFile
            mv.visitLdcInsn("createDummyFile")

            // Create an empty array
            mv.visitInsn(Opcodes.ICONST_1)
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Class")

            //array at index 0
            mv.visitInsn(Opcodes.DUP)
            mv.visitInsn(Opcodes.ICONST_0)
            mv.visitLdcInsn(Type.getType("Landroid/content/Context;"))
            mv.visitInsn(Opcodes.AASTORE)

            // getDeclaredMethod
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/Class",
                    "getDeclaredMethod",
                    "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
                    false
            )
            mv.visitVarInsn(Opcodes.ASTORE, 3);

            // Set the method accessible
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            mv.visitInsn(Opcodes.ICONST_1)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "setAccessible", "(Z)V", false)

            //MainStack
            // Get the current thread
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "java/lang/Thread",
                    "currentThread",
                    "()Ljava/lang/Thread;",
                    false);

            // Store the current Thread object in a local variable
            mv.visitVarInsn(Opcodes.ASTORE, 4);
            mv.visitVarInsn(Opcodes.ALOAD, 4);

            // Invoke getStackTrace()
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "java/lang/Thread",
                    "getStackTrace",
                    "()[Ljava/lang/StackTraceElement;",
                    false);
            mv.visitVarInsn(Opcodes.ASTORE, 4);

            mv.visitVarInsn(Opcodes.ALOAD, 4);
            mv.visitFieldInsn(
                    Opcodes.PUTSTATIC,
                    "com/lib/helldroid/Helldroid",
                    "MainStackTrace",
                    "[Ljava/lang/StackTraceElement;")

            // Get LoadLibrary var and Null Param
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            mv.visitInsn(Opcodes.ACONST_NULL)

            // Create an array for Invoke Params
            mv.visitInsn(Opcodes.ICONST_1)
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object")

            //array at index 0
            mv.visitInsn(Opcodes.DUP)
            mv.visitInsn(Opcodes.ICONST_0)
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitInsn(Opcodes.AASTORE)

            //Invoke
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/reflect/Method",
                    "invoke",
                    "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
                    false
            )

            mv.visitInsn(Opcodes.POP)

            // Mark the end of the try block
            mv.visitLabel(endLabel)
            val afterCatchLabel = Label()
            mv.visitJumpInsn(Opcodes.GOTO, afterCatchLabel)

            // Exception handler block
            mv.visitLabel(handlerLabel)
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/app/Activity", "finish", "()V", false)
            mv.visitInsn(Opcodes.POP)
            mv.visitLabel(afterCatchLabel)
        }
    }
}
