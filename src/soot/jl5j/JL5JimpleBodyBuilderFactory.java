package soot.jl5j;

public class JL5JimpleBodyBuilderFactory extends soot.javaToJimple.AbstractJBBFactory {

    protected soot.javaToJimple.AbstractJimpleBodyBuilder createJimpleBodyBuilder(){
        soot.javaToJimple.JimpleBodyBuilder jbb = new soot.javaToJimple.JimpleBodyBuilder();
        JL5JimpleBodyBuilder afjbb = new JL5JimpleBodyBuilder();
        afjbb.ext(jbb);
        return afjbb;
    }
}
