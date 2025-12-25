package io.github.tox1cozz.mixinextras.injector;

import io.github.tox1cozz.mixinextras.utils.CompatibilityHelper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes.InjectionNode;
import org.spongepowered.asm.mixin.injection.struct.Target;

public class ModifyReturnValueInjector extends Injector {

    public ModifyReturnValueInjector(InjectionInfo info) {
        super(info, "@ModifyReturnValue");
    }

    @Override
    protected void inject(Target target, InjectionNode node) {
        int opcode = node.getCurrentTarget().getOpcode();
        if (opcode < Opcodes.IRETURN || opcode >= Opcodes.RETURN) {
            throw CompatibilityHelper.makeInvalidInjectionException(info, String.format("%s annotation is targeting an invalid insn in %s in %s", annotationType, target, this));
        }
        checkTargetModifiers(target, false);
        injectReturnValueModifier(target, node);
    }

    private void injectReturnValueModifier(Target target, InjectionNode node) {
        InjectorData handler = new InjectorData(target, "return value modifier");
        InsnList insns = new InsnList();

        validateParams(handler, target.returnType, target.returnType);

        if (!isStatic) {
            insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
            if (target.returnType.getSize() == 2) {
                insns.add(new InsnNode(Opcodes.DUP_X2));
                insns.add(new InsnNode(Opcodes.POP));
            } else {
                insns.add(new InsnNode(Opcodes.SWAP));
            }
        }

        if (handler.captureTargetArgs > 0) {
            pushArgs(target.arguments, insns, target.getArgIndices(), 0, handler.captureTargetArgs);
        }

        invokeHandler(insns);
        target.insertBefore(node, insns);
    }
}