package io.github.tox1cozz.mixinextras.injector;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.AnnotationType;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.HandlerPrefix;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

import java.util.List;

@AnnotationType(WrapWithCondition.class)
@HandlerPrefix("wrapWithCondition")
public class WrapWithConditionInjectionInfo extends InjectionInfo {

    static final String POPPED_OPERATION_DECORATOR = "mixinextras_operationIsImmediatelyPopped";

    public WrapWithConditionInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
        super(mixin, method, annotation);
    }

    @Override
    protected Injector parseInjector(AnnotationNode injectAnnotation) {
        return new WrapWithConditionInjector(this);
    }

    @Override
    public void prepare() {
        super.prepare();
        for (List<InjectionNodes.InjectionNode> nodeList : targetNodes.values()) {
            for (InjectionNodes.InjectionNode node : nodeList) {
                AbstractInsnNode currentTarget = node.getCurrentTarget();
                if (currentTarget instanceof MethodInsnNode) {
                    Type returnType = Type.getReturnType(((MethodInsnNode)currentTarget).desc);
                    if (isTypePoppedByInstruction(returnType, currentTarget.getNext())) {
                        node.decorate(POPPED_OPERATION_DECORATOR, true);
                    }
                }
            }
        }
    }

    private boolean isTypePoppedByInstruction(Type type, AbstractInsnNode insn) {
        switch (type.getSize()) {
            case 2:
                return insn.getOpcode() == Opcodes.POP2;
            case 1:
                return insn.getOpcode() == Opcodes.POP;
            default:
                return false;
        }
    }
}