package dev.meldrite.v1201.compat;

import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/** Inspects real JVM declarations and interface dispatch obligations without executing game behavior. */
public final class RuntimeAbiVerifier {
    public record Finding(String owner, String kind, String name, String descriptor,
                          String status, String declaringOwner, String detail) {
        public boolean matched() { return "STRUCTURAL_MATCH".equals(status); }
    }

    private RuntimeAbiVerifier() { }

    public static Finding checkMember(Class<?> owner, String kind, String name, String descriptor,
            Boolean expectedStatic, String visibility, boolean declared) {
        return checkMember(owner, kind, name, descriptor, expectedStatic, null, visibility, declared);
    }

    public static Finding checkMember(Class<?> owner, String kind, String name, String descriptor,
            Boolean expectedStatic, Boolean expectedFinal, String visibility, boolean declared) {
        if (!Set.of("field", "method", "constructor").contains(kind))
            throw new IllegalArgumentException("Unsupported ABI member kind: " + kind);
        if (visibility != null && !Set.of("public", "protected", "private", "package").contains(visibility))
            throw new IllegalArgumentException("Unsupported ABI visibility: " + visibility);
        try {
            Member member = switch (kind) {
                case "field" -> findField(owner, name, descriptor, declared, new LinkedHashSet<>());
                case "method" -> findMethod(owner, name, descriptor, declared);
                case "constructor" -> findConstructor(owner, descriptor);
                default -> throw new AssertionError(kind);
            };
            if (member == null) return finding(owner, kind, name, descriptor, "MISSING_MEMBER", null,
                    "No declaration with this complete JVM descriptor");
            int flags = member.getModifiers();
            String declaration = member.getDeclaringClass().getName();
            if (expectedStatic != null && expectedStatic != Modifier.isStatic(flags))
                return finding(owner, kind, name, descriptor, "STATIC_KIND_MISMATCH", declaration,
                        "Actual modifiers: " + Modifier.toString(flags));
            if (expectedFinal != null && expectedFinal != Modifier.isFinal(flags))
                return finding(owner, kind, name, descriptor, "FINAL_KIND_MISMATCH", declaration,
                        "Actual modifiers: " + Modifier.toString(flags));
            if (visibility != null && !visibility.equals(visibility(flags)))
                return finding(owner, kind, name, descriptor, "VISIBILITY_MISMATCH", declaration,
                        "Expected " + visibility + ", actual " + visibility(flags));
            if (member instanceof Method && Modifier.isAbstract(flags) && !owner.isInterface()
                    && !Modifier.isAbstract(owner.getModifiers()))
                return finding(owner, kind, name, descriptor, "ABSTRACT_IMPLEMENTATION", declaration,
                        "The concrete receiver has no concrete implementation");
            return finding(owner, kind, name, descriptor, "STRUCTURAL_MATCH", declaration,
                    "Declaration metadata only; behavior and caller access are not certified");
        } catch (AmbiguousDefaults failure) {
            return finding(owner, kind, name, descriptor, "AMBIGUOUS_INTERFACE_DEFAULT", null, failure.getMessage());
        } catch (LinkageError | SecurityException failure) {
            return finding(owner, kind, name, descriptor, "METADATA_LINKAGE_ERROR", null, failure.toString());
        }
    }

    public static List<Finding> checkInterface(Class<?> receiver, Class<?> contract, boolean declared) {
        if (!contract.isInterface()) throw new IllegalArgumentException("Not an interface: " + contract.getName());
        if (declared && java.util.Arrays.stream(receiver.getInterfaces()).noneMatch(type -> type == contract))
            return List.of(finding(receiver, "interface", contract.getName(), descriptor(contract),
                    "DIRECT_INTERFACE_MISSING", null, "The receiver does not directly declare this interface"));
        return checkInterface(receiver, contract);
    }

    /** Checks exact return descriptors too: a covariant method without its bridge is insufficient. */
    public static List<Finding> checkInterface(Class<?> receiver, Class<?> contract) {
        if (!contract.isInterface()) throw new IllegalArgumentException("Not an interface: " + contract.getName());
        if (!contract.isAssignableFrom(receiver)) return List.of(finding(receiver, "interface", contract.getName(),
                descriptor(contract), "MISSING_INTERFACE", null, "Receiver does not implement the expected interface"));
        if (receiver.isInterface() || Modifier.isAbstract(receiver.getModifiers()))
            return List.of(finding(receiver, "interface", contract.getName(), descriptor(contract),
                    "ABSTRACT_RECEIVER_NOT_CHECKED", null, "Concrete interface dispatch requires a concrete receiver"));
        try {
            var obligations = new TreeMap<String, Method>();
            for (Method method : contract.getMethods()) {
                if (!Modifier.isStatic(method.getModifiers()) && !Modifier.isPrivate(method.getModifiers()))
                    obligations.put(method.getName() + descriptor(method), method);
            }
            var findings = new ArrayList<Finding>();
            for (Method obligation : obligations.values()) findings.add(checkDispatch(receiver, obligation));
            return List.copyOf(findings);
        } catch (LinkageError | SecurityException failure) {
            return List.of(finding(receiver, "interface", contract.getName(), descriptor(contract),
                    "METADATA_LINKAGE_ERROR", null, failure.toString()));
        }
    }

    public static Set<Class<?>> interfaces(Class<?> receiver) {
        var interfaces = new LinkedHashSet<Class<?>>();
        collectInterfaces(receiver, interfaces);
        return java.util.Collections.unmodifiableSet(interfaces);
    }

    private static Finding checkDispatch(Class<?> receiver, Method obligation) {
        String name = obligation.getName(), descriptor = descriptor(obligation);
        for (Class<?> type = receiver; type != null; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                int flags = method.getModifiers();
                if (!method.getName().equals(name) || !descriptor(method).equals(descriptor)
                        || Modifier.isStatic(flags) || Modifier.isPrivate(flags)) continue;
                if (!Modifier.isPublic(flags)) return finding(receiver, "interface-method", name, descriptor,
                        "INTERFACE_METHOD_NOT_PUBLIC", type.getName(), "invokeinterface requires a public selected method");
                if (Modifier.isAbstract(flags)) return finding(receiver, "interface-method", name, descriptor,
                        "INTERFACE_METHOD_MISSING", type.getName(), "The selected class method is abstract");
                return finding(receiver, "interface-method", name, descriptor, "STRUCTURAL_MATCH", type.getName(),
                        "Concrete public dispatch declaration; behavior is not executed");
            }
        }
        List<Method> maximal = maximalInterfaceMethods(receiver, name, descriptor);
        List<Method> defaults = maximal.stream().filter(Method::isDefault).toList();
        if (defaults.size() == 1) return finding(receiver, "interface-method", name, descriptor,
                "STRUCTURAL_MATCH", defaults.get(0).getDeclaringClass().getName(), "Inherited maximally-specific default");
        if (defaults.size() > 1) return finding(receiver, "interface-method", name, descriptor,
                "AMBIGUOUS_INTERFACE_DEFAULT", null, defaults.stream().map(method -> method.getDeclaringClass().getName())
                        .sorted().toList().toString());
        return finding(receiver, "interface-method", name, descriptor, "INTERFACE_METHOD_MISSING", null,
                "No exact public class implementation or unambiguous interface default");
    }

    private static Field findField(Class<?> owner, String name, String descriptor, boolean declared, Set<Class<?>> seen) {
        if (!seen.add(owner)) return null;
        for (Field field : owner.getDeclaredFields())
            if (field.getName().equals(name) && descriptor(field.getType()).equals(descriptor)) return field;
        if (declared) return null;
        // JVMS field resolution searches direct superinterfaces before the superclass.
        for (Class<?> parent : owner.getInterfaces()) {
            Field field = findField(parent, name, descriptor, false, seen);
            if (field != null) return field;
        }
        return owner.getSuperclass() == null ? null : findField(owner.getSuperclass(), name, descriptor, false, seen);
    }

    private static Method findMethod(Class<?> owner, String name, String descriptor, boolean declared) {
        // Finish the class hierarchy before considering any interface: an abstract
        // interface inherited through a superclass can have a more-specific default.
        for (Class<?> type = owner; type != null; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods())
                if (method.getName().equals(name) && descriptor(method).equals(descriptor)) return method;
            if (declared) return null;
        }
        if (owner.isInterface()) {
            for (Method method : Object.class.getDeclaredMethods()) {
                int flags = method.getModifiers();
                if (Modifier.isPublic(flags) && !Modifier.isStatic(flags) && method.getName().equals(name)
                        && descriptor(method).equals(descriptor)) return method;
            }
        }
        List<Method> maximal = maximalInterfaceMethods(owner, name, descriptor);
        List<Method> defaults = maximal.stream().filter(Method::isDefault).toList();
        if (defaults.size() > 1) throw new AmbiguousDefaults(defaults.stream()
                .map(method -> method.getDeclaringClass().getName()).sorted().toList().toString());
        if (defaults.size() == 1) return defaults.get(0);
        return maximal.isEmpty() ? null : maximal.get(0);
    }

    private static List<Method> maximalInterfaceMethods(Class<?> receiver, String name, String descriptor) {
        var candidates = new ArrayList<Method>();
        for (Class<?> type : interfaces(receiver)) {
            for (Method method : type.getDeclaredMethods()) {
                if (!Modifier.isStatic(method.getModifiers()) && !Modifier.isPrivate(method.getModifiers())
                        && method.getName().equals(name) && descriptor(method).equals(descriptor)) candidates.add(method);
            }
        }
        // A more-specific abstract redeclaration suppresses an inherited default too.
        return candidates.stream().filter(candidate -> candidates.stream().noneMatch(other ->
                candidate.getDeclaringClass() != other.getDeclaringClass()
                        && candidate.getDeclaringClass().isAssignableFrom(other.getDeclaringClass())))
                .sorted(java.util.Comparator.comparing(method -> method.getDeclaringClass().getName())).toList();
    }

    private static final class AmbiguousDefaults extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private AmbiguousDefaults(String owners) { super(owners); }
    }

    private static Constructor<?> findConstructor(Class<?> owner, String descriptor) {
        for (Constructor<?> constructor : owner.getDeclaredConstructors())
            if (MethodType.methodType(void.class, constructor.getParameterTypes()).toMethodDescriptorString().equals(descriptor))
                return constructor;
        return null;
    }

    private static void collectInterfaces(Class<?> type, Set<Class<?>> interfaces) {
        for (Class<?> parent : type.getInterfaces()) if (interfaces.add(parent)) collectInterfaces(parent, interfaces);
        if (type.getSuperclass() != null) collectInterfaces(type.getSuperclass(), interfaces);
    }
    private static String descriptor(Method method) {
        return MethodType.methodType(method.getReturnType(), method.getParameterTypes()).toMethodDescriptorString();
    }
    private static String descriptor(Class<?> type) { return type.descriptorString(); }
    private static String visibility(int flags) {
        if (Modifier.isPublic(flags)) return "public";
        if (Modifier.isProtected(flags)) return "protected";
        if (Modifier.isPrivate(flags)) return "private";
        return "package";
    }
    private static Finding finding(Class<?> owner, String kind, String name, String descriptor,
            String status, String declaration, String detail) {
        return new Finding(owner.getName(), kind, name, descriptor, status, declaration, detail);
    }
}
