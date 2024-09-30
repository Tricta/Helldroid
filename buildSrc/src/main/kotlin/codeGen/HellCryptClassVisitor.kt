package codeGen

import hellEncryptor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.GETSTATIC
import org.objectweb.asm.Opcodes.IF_ICMPGE
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL
import org.objectweb.asm.Opcodes.PUTSTATIC
import org.objectweb.asm.Opcodes.RETURN

class HellCryptClassVisitor(api: Int, cv: ClassVisitor) : ClassVisitor(api, cv) {
    private lateinit var className: String

    private var staticBlock = false
    private val stringMap = mutableMapOf<String, String>()

    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
        className = name
        super.visit(version, access, className, signature, superName, interfaces)
    }

    override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor? {
        if (descriptor == "Ljava/lang/String;" && value is String) {
            stringMap[name] = hellEncryptor.encrypt(value).decodeToString()
            return super.visitField(access, name, descriptor, signature, null)
        }
        return super.visitField(access, name, descriptor, signature, value)
    }

    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        return if (name == "<clinit>") {
            staticBlock = true
            HellCryptMethodVisitor(mv, true)
        } else {
            HellCryptMethodVisitor(mv, false)
        }
    }

    override fun visitEnd() {
        if (!staticBlock) {
            val mv = super.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null)
            val smv = HellCryptMethodVisitor(mv, true)
            smv.visitCode()
            smv.visitInsn(RETURN)
            smv.visitMaxs(0, 0)
            smv.visitEnd()
        }
        super.visitEnd()
    }

    private inner class HellCryptMethodVisitor(mv: MethodVisitor, private val clinit: Boolean) : MethodVisitor(api, mv) {
        private var modified = false

        private fun writeEncrypted(encrypted: String) {
            modified = true
            super.visitFieldInsn(GETSTATIC, "com/lib/hellcrypt/Stub", "instance", "Lcom/lib/hellcrypt/Stub;")
            super.visitLdcInsn(encrypted)
            super.visitMethodInsn(INVOKEVIRTUAL, "com/lib/hellcrypt/Stub", "hellYoki", "(Ljava/lang/String;)Ljava/lang/String;", false)
        }

        /*private fun generateRandomString(): String {
            val length = 8 // Length of the random string
            val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            val result = StringBuilder(length)
            for (i in 0 until length) {
                val index = (Math.random() * characters.length).toInt()
                result.append(characters[index])
            }
            return result.toString()
        }*/

        override fun visitLdcInsn(value: Any) {
            if (value is String) {
                val encrypted = hellEncryptor.encrypt(value).decodeToString()

                writeEncrypted(encrypted)
            } else {
                // Handle non-String values normally
                super.visitLdcInsn(value)

                /*// Push two integer values onto the stack for comparison
                super.visitLdcInsn(2) // First value
                super.visitLdcInsn(1) // Second value

                // Perform the comparison and jump if first >= second
                val falseBranch = Label()
                super.visitJumpInsn(IF_ICMPGE, falseBranch)

                // Remove top stack value if the comparison fails (to balance the stack)
                super.visitInsn(Opcodes.POP)

                // Push a random string onto the stack
                super.visitLdcInsn(generateRandomString())

                // Mark the false branch for the jump
                super.visitLabel(falseBranch)*/
            }
        }

        override fun visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) {
            if (name == "init") {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)

                /*// Step 1: Call Method targetMethod = MainActivity.class.getDeclaredMethod("newFunc");
                super.visitLdcInsn(Type.getType("Lcom/sdk/helldroid/MainActivity;"))
                super.visitLdcInsn("newFunc")
                super.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Class")
                super.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/lang/Class",
                        "getDeclaredMethod",
                        "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
                        false
                )

                super.visitVarInsn(Opcodes.ASTORE, 1)

                // Step 2: Call Method replacementMethod = MainActivity.class.getDeclaredMethod("newFunc2");
                super.visitLdcInsn(Type.getType("Lcom/sdk/helldroid/MainActivity;"))
                super.visitLdcInsn("newFunc2")
                super.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Class")
                super.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/lang/Class",
                        "getDeclaredMethod",
                        "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
                        false
                )

                super.visitVarInsn(Opcodes.ASTORE, 2)*/
            } else {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        }

        override fun visitCode() {
            if (clinit) stringMap.forEach {
                writeEncrypted(it.value)
                super.visitFieldInsn(PUTSTATIC, className, it.key, "Ljava/lang/String;")
            }

            super.visitCode()
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            if (!modified) super.visitMaxs(maxStack, maxLocals)
            else super.visitMaxs((maxStack + 1).coerceAtLeast(2), maxLocals)
        }
    }
}
