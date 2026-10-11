package dev.meldrite.contract;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/** Deterministic source forwarding only. Unsupported signatures fail generation, never degrade to Object. */
public final class ProfileAdapterGenerator {
    private ProfileAdapterGenerator() {}
    public static String generate(JsonNode inventory,String owner,String adapterClass,Map<String,String> delegates){
        if(!adapterClass.matches("[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)+"))throw new IllegalArgumentException("Qualified adapter name required");
        JsonNode type=null;for(JsonNode candidate:inventory.path("classes"))if(candidate.path("owner").asText().equals(owner))type=candidate;
        if(type==null||(type.path("access").asInt()&Opcodes.ACC_INTERFACE)==0)throw new IllegalArgumentException("Generator requires a known interface; concrete factories are owned separately");
        if(type.path("signature").asText().startsWith("<"))throw new IllegalArgumentException("Generic owner substitution requires an explicit profile policy: "+owner);
        int split=adapterClass.lastIndexOf('.');StringBuilder source=new StringBuilder("package ").append(adapterClass,0,split).append(";\npublic final class ").append(adapterClass.substring(split+1)).append(" implements ").append(owner.replace('/','.').replace('$','.')).append(" {\n");
        Set<String> usedDelegates=new HashSet<>(),erasedSignatures=new HashSet<>();
        for(JsonNode method:inventory.path("symbols")){
            if(!method.path("owner").asText().equals(owner)||!method.path("kind").asText().endsWith("METHOD")||!method.path("requiresImplementation").asBoolean())continue;
            int access=method.path("access").asInt();if((access&(Opcodes.ACC_BRIDGE|Opcodes.ACC_SYNTHETIC|Opcodes.ACC_STATIC))!=0)continue;
            String name=method.path("name").asText(),descriptor=method.path("descriptor").asText(),id=method.path("id").asText();
            String erasedKey=name+descriptor.substring(0,descriptor.indexOf(')')+1);
            if(!erasedSignatures.add(erasedKey))throw new IllegalArgumentException("Covariant override selection requires explicit policy: "+id);
            Signature signature=Signature.read(method.path("signature").asText(),descriptor);
            source.append("  @Override public ").append(signature.parametersPrefix()).append(signature.result()).append(' ').append(name).append('(');
            for(int i=0;i<signature.arguments().size();i++){
                if(i>0)source.append(", ");String argument=signature.arguments().get(i);
                if(i==signature.arguments().size()-1&&(access&Opcodes.ACC_VARARGS)!=0&&argument.endsWith("[]"))argument=argument.substring(0,argument.length()-2)+"...";
                source.append(argument).append(" p").append(i);
            }
            source.append(')');List<String> exceptions=signature.exceptions();
            if(exceptions.isEmpty()){exceptions=new ArrayList<>();for(JsonNode exception:method.path("exceptions"))exceptions.add(exception.asText().replace('/','.').replace('$','.'));}
            if(!exceptions.isEmpty())source.append(" throws ").append(String.join(", ",exceptions));
            source.append(" { ");String delegate=delegates.get(id);
            if(delegate!=null){
                if(delegate.isBlank())throw new IllegalArgumentException("Empty delegate for "+id);
                usedDelegates.add(id);if(!signature.result().equals("void"))source.append("return ");source.append(delegate).append(';');
            } else source.append("throw new UnsupportedOperationException(\"UNSUPPORTED_OPERATION: ").append(id.replace("\\","\\\\").replace("\"","\\\"")).append("\");");
            source.append(" }\n");
        }
        if(!usedDelegates.equals(delegates.keySet()))throw new IllegalArgumentException("Delegate does not name a generated abstract symbol: "+delegates.keySet());
        return source.append("}\n").toString();
    }
    private record Signature(String parametersPrefix,List<String> arguments,String result,List<String> exceptions){
        static Signature read(String generic,String descriptor){
            if(generic.isBlank()){List<String> args=Arrays.stream(Type.getArgumentTypes(descriptor)).map(t->t.getClassName().replace('$','.')).toList();return new Signature("",args,Type.getReturnType(descriptor).getClassName().replace('$','.'),List.of());}
            Parser p=new Parser(generic);String prefix="";
            if(p.peek()=='<'){
                p.position++;List<String> variables=new ArrayList<>();
                while(p.peek()!='>'){
                    int start=p.position;while(p.peek()!=':')p.position++;String name=p.text.substring(start,p.position);List<String> bounds=new ArrayList<>();
                    while(p.peek()==':'){p.position++;if(p.peek()!=':')bounds.add(p.type());}
                    variables.add(name+(bounds.isEmpty()?"":" extends "+String.join(" & ",bounds)));
                }
                p.position++;prefix="<"+String.join(", ",variables)+"> ";
            }
            p.expect('(');List<String> arguments=new ArrayList<>();while(p.peek()!=')')arguments.add(p.type());p.position++;
            String result=p.type();List<String> exceptions=new ArrayList<>();while(p.position<p.text.length()){p.expect('^');exceptions.add(p.type());}
            return new Signature(prefix,arguments,result,exceptions);
        }
    }
    private static final class Parser {
        final String text;int position;
        Parser(String text){this.text=text;}
        char peek(){if(position>=text.length())throw new IllegalArgumentException("Truncated signature: "+text);return text.charAt(position);}
        void expect(char c){if(peek()!=c)throw new IllegalArgumentException("Invalid signature: "+text);position++;}
        String type(){
            char c=peek();position++;
            return switch(c){
                case 'V'->"void";case 'Z'->"boolean";case 'B'->"byte";case 'C'->"char";case 'S'->"short";case 'I'->"int";case 'J'->"long";case 'F'->"float";case 'D'->"double";
                case '['->type()+"[]";case '*'->"?";case '+'->"? extends "+type();case '-'->"? super "+type();
                case 'T'->{int end=text.indexOf(';',position);if(end<0)throw new IllegalArgumentException("Invalid type variable");String result=text.substring(position,end);position=end+1;yield result;}
                case 'L'->{StringBuilder result=new StringBuilder();while(peek()!=';'){
                    if(peek()=='<'){position++;List<String> args=new ArrayList<>();while(peek()!='>')args.add(type());position++;result.append('<').append(String.join(", ",args)).append('>');}
                    else {char part=peek();position++;result.append(part=='/'||part=='$'?'.':part);}
                }position++;yield result.toString();}
                default->throw new IllegalArgumentException("Unsupported signature: "+text);
            };
        }
    }
}
