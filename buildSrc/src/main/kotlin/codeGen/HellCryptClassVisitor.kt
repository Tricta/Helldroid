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

        override fun visitLdcInsn(value: Any) {
            if (value is String) {
                val encrypted = hellEncryptor.encrypt(value).decodeToString()

                writeEncrypted(encrypted)
            } else {
                super.visitLdcInsn(value)
            }
        }

        override fun visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) {
            if (name == "init") {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
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
