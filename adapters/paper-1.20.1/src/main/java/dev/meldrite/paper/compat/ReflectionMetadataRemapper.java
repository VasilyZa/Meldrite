package dev.meldrite.paper.compat;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.commons.AnnotationRemapper;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.FieldRemapper;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.RecordComponentRemapper;
import org.objectweb.asm.commons.Remapper;

/** Annotation strings are inputs to their consumer, rather than JVM symbolic type references. */
final class ReflectionMetadataRemapper extends ClassRemapper {
    ReflectionMetadataRemapper(ClassVisitor visitor, Remapper remapper) {
        super(org.objectweb.asm.Opcodes.ASM9, visitor, remapper);
    }

    @Override protected AnnotationVisitor createAnnotationRemapper(String descriptor, AnnotationVisitor visitor) {
        return annotation(descriptor, visitor, remapper);
    }

    @Override protected FieldVisitor createFieldRemapper(FieldVisitor visitor) {
        return new FieldRemapper(visitor, remapper) {
            @Override protected AnnotationVisitor createAnnotationRemapper(String descriptor, AnnotationVisitor visitor) {
                return annotation(descriptor, visitor, remapper);
            }
        };
    }

    @Override protected MethodVisitor createMethodRemapper(MethodVisitor visitor) {
        return new MethodRemapper(visitor, remapper) {
            @Override protected AnnotationVisitor createAnnotationRemapper(String descriptor, AnnotationVisitor visitor) {
                return annotation(descriptor, visitor, remapper);
            }
        };
    }

    @Override protected RecordComponentVisitor createRecordComponentRemapper(RecordComponentVisitor visitor) {
        return new RecordComponentRemapper(visitor, remapper) {
            @Override protected AnnotationVisitor createAnnotationRemapper(String descriptor, AnnotationVisitor visitor) {
                return annotation(descriptor, visitor, remapper);
            }
        };
    }

    private static AnnotationVisitor annotation(String descriptor, AnnotationVisitor visitor, Remapper remapper) {
        return new AnnotationRemapper(descriptor, visitor, remapper) {
            @Override public void visit(String name, Object value) {
                if (value instanceof String) {
                    av.visit(descriptor == null ? name : remapper.mapAnnotationAttributeName(descriptor, name), value);
                } else {
                    super.visit(name, value);
                }
            }

            @Override protected AnnotationVisitor createAnnotationRemapper(String descriptor, AnnotationVisitor visitor) {
                return annotation(descriptor, visitor, remapper);
            }
        };
    }
}
