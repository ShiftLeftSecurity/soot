// package ca.mcgill.sable.soot.virtualCalls;


package ca.mcgill.sable.soot.jimple.toolkit.invoke;


//import java.util.*;
import java.io.*;
import ca.mcgill.sable.soot.jimple.*;
import ca.mcgill.sable.util.*;
import java.util.*;
import ca.mcgill.sable.soot.baf.*; 
// import ca.mcgill.sable.soot.sideEffect.*;
import ca.mcgill.sable.soot.*;

public class VTANativeAdjustor {



 public HashMap declaredtypesHT; 

 public HashMap parameterHT;

 public RTA rta;



 public VTANativeAdjustor ( HashMap declaredtypesht, HashMap parameterht, RTA rta ) {

  declaredtypesHT = declaredtypesht;

  parameterHT = parameterht;

  this.rta = rta;   

 }







 public void adjustNativeNode ( String s1, String s2 ) {
  
  if ( ( ( TypeNode ) declaredtypesHT.get ( s1 ) ) != null ) 
  {

   TypeNode tn = ( TypeNode ) declaredtypesHT.get ( s1 );
  
   tn.addInstanceType ( s2 );
  
   adjustSubClasses ( s2, tn );

   declaredtypesHT.put ( s1, tn );

  }

 }









  private void adjustSubClasses ( String s, TypeNode tn ) {
      
   ClassNode cn = rta.getClassGraphBuilder().getNode ( s );
      
   Set subclassnodes = rta.getClassGraphBuilder().getAllSubClassesOf ( cn );
       
   Iterator subclassnodesit = subclassnodes.iterator();
       
   while ( subclassnodesit.hasNext() )
   {  
       
    try {
       
    ClassNode subcn = ( ClassNode ) subclassnodesit.next();

    String name = subcn.getSootClass().getName();
    
    tn.addInstanceType ( name );
  
    } catch ( java.lang.RuntimeException e ) {}
       
   }
      
 }


















public void adjustForNativeMethods() {



 String s1 = "return_<java.lang.Object: java.lang.Class getClass()>";
 String s2 = "java.lang.Class"; 
 adjustNativeNode ( s1, s2 );


 try {

 s1 = "return_<java.lang.Object: java.lang.Object clone()>";
 
 s2 = "this_<java.lang.Object: java.lang.Object clone()>";

 if ( ( ( ( TypeNode ) declaredtypesHT.get ( s1 ) ) != null ) && ( ( ( TypeNode ) declaredtypesHT.get ( s2 ) ) != null ) )
 {
 
 TypeNode tn1 = ( TypeNode ) declaredtypesHT.get ( s1  );

 TypeNode tn2 = ( TypeNode ) declaredtypesHT.get ( s2 );
 
 tn2.addForwardNode ( tn1 );

 tn1.addBackwardNode ( tn2 );

 declaredtypesHT.put ( s1, tn1 ); 
 
 declaredtypesHT.put ( s2, tn2 );
 }


 } catch ( java.lang.RuntimeException e ) {}




 s1 = "return_<java.lang.String: java.lang.String intern()>";
 s2 = "java.lang.String";
 adjustNativeNode ( s1, s2 );


 s1 = "return_<java.lang.Throwable: java.lang.Throwable fillInStackTrace()>";
 s2 = "java.lang.Throwable";
 adjustNativeNode ( s1, s2 );


 s1 = "return_<java.lang.Class: java.lang.Class forName(java.lang.String)>";
 s2 = "java.lang.Class";
 adjustNativeNode ( s1, s2 );




 s1 = "return_<java.lang.Class: java.lang.Object newInstance()>";

 Iterator newinstit = rta.newinstances.iterator();

 while ( newinstit.hasNext() )
 {

  String newinstance = ( String ) newinstit.next();

  adjustNativeNode ( s1, newinstance );

 }






 s1 = "return_<java.lang.Class: java.lang.String getName()>";
 s2 = "java.lang.String";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.lang.Class: java.lang.ClassLoader getClassLoader()>";
 s2 = "java.lang.ClassLoader";
 adjustNativeNode ( s1, s2 );


 s1 = "return_<java.lang.Class: java.lang.Class getSuperclass()>";
 s2 = "java.lang.Class";
 adjustNativeNode ( s1, s2 );
 
 s1 = "return_<java.lang.Class: java.lang.Class[] getInterfaces()>";
 s2 = "java.lang.Class";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.lang.Class: java.lang.Class getComponentType()>";
 s2 = "java.lang.Class";
 adjustNativeNode ( s1, s2 );


// ADD NATIVE METHOD java.lang.Class.getSigners():java.lang.Object[]
// ADD NATIVE METHOD java.lang.Class.setSigners(java.lang.Object[]):void

 try {

 s1 = "return_<java.lang.Class: java.lang.Object[] getSigners()>";

 s2 = ( String ) /* parameterHT.get ( */ "<java.lang.Class: void setSigners(java.lang.Object[])>$0" /* ) */ ;

 if ( ( ( ( TypeNode ) declaredtypesHT.get ( s1 ) ) != null ) && ( ( ( TypeNode ) declaredtypesHT.get ( s2 ) ) != null ) )
 {

 TypeNode tn1 = ( TypeNode ) declaredtypesHT.get ( s1  );
 
 TypeNode tn2 = ( TypeNode ) declaredtypesHT.get ( s2 );

 tn2.addForwardNode ( tn1 );

 tn1.addBackwardNode ( tn2 );   

 tn1.addForwardNode ( tn2 );

 tn2.addBackwardNode ( tn1 );

 declaredtypesHT.put ( s1, tn1 );

 declaredtypesHT.put ( s2, tn2 );
 }


 } catch ( java.lang.RuntimeException e ) {}



 s1 = "return_<java.lang.Class: java.lang.Class getPrimitiveClass(java.lang.String)>";
 s2 = "java.lang.Class";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.lang.Class: java.lang.reflect.Field[] getFields0(int)>";
 s2 = "java.lang.reflect.Field";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.lang.Class: java.lang.reflect.Method[] getMethods0(int)>";
 s2 = "java.lang.reflect.Method";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.lang.Class: java.lang.reflect.Constructor[] getConstructors0(int)>";
 s2 = "java.lang.reflect.Constructor";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.lang.Class: java.lang.reflect.Field getField0(java.lang.String,int)>";
 s2 = "java.lang.reflect.Field";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.lang.Class: java.lang.reflect.Method getMethod0(java.lang.String,java.lang.Class[],int)>";
 s2 = "java.lang.reflect.Method";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.lang.Class: java.lang.reflect.Constructor getConstructor0(java.lang.Class[],int)>";
 s1 = "java.lang.reflect.Constructor";
 adjustNativeNode ( s1, s2 );


// ADD NATIVE METHOD java.lang.System.arraycopy(java.lang.Object,int,java.lang.Object,int,int):void

 try {

 s1 = ( String ) /* parameterHT.get ( */ "<java.lang.System: void arraycopy(java.lang.Object,int,java.lang.Object,int,int)>$0" /* ) */ ;

 s2 = ( String ) /* parameterHT.get ( */ "<java.lang.System: void arraycopy(java.lang.Object,int,java.lang.Object,int,int)>$2" /* ) */ ;

 if ( ( ( ( TypeNode ) declaredtypesHT.get ( s1 ) ) != null ) && ( ( ( TypeNode ) declaredtypesHT.get ( s2 ) ) != null ) )
 {

 TypeNode tn1 = ( TypeNode ) declaredtypesHT.get ( s1  );

 TypeNode tn2 = ( TypeNode ) declaredtypesHT.get ( s2 );
 
 tn2.addForwardNode ( tn1 );

 tn1.addBackwardNode ( tn2 );

 tn1.addForwardNode ( tn2 );

 tn2.addBackwardNode ( tn1 );

 declaredtypesHT.put ( s1, tn1 ); 
 
 declaredtypesHT.put ( s2, tn2 );
 }

 } catch ( java.lang.RuntimeException e ) {}
 

 

 
 try {

 s1 = (String) /* parameterHT.get ( */ "<java.lang.System: void setErr0(java.io.PrintStream)>$0" /* ) */ ;

 s2 = "<java.lang.System: err>";

 if ( ( ( ( TypeNode ) declaredtypesHT.get ( s1 ) ) != null ) && ( ( ( TypeNode ) declaredtypesHT.get ( s2 ) ) != null ) )
 {

  TypeNode tn1 = ( TypeNode ) declaredtypesHT.get ( s1  );

  TypeNode tn2 = ( TypeNode ) declaredtypesHT.get ( s2 );
  
  tn1.addForwardNode ( tn2 );

  tn2.addBackwardNode ( tn1 );

  declaredtypesHT.put ( s1, tn1 ); 
 
  declaredtypesHT.put ( s2, tn2 );
 }

} catch ( java.lang.RuntimeException e ) {}




 try {

 s1 = (String) /* parameterHT.get ( */ "<java.lang.System: void setOut0(java.io.PrintStream)>$0" /* ) */ ;

 s2 = "<java.lang.System: out>";

 if ( ( ( ( TypeNode ) declaredtypesHT.get ( s1 ) ) != null ) && ( ( ( TypeNode ) declaredtypesHT.get ( s2 ) ) != null ) )
 {

  TypeNode tn1 = ( TypeNode ) declaredtypesHT.get ( s1  );

  TypeNode tn2 = ( TypeNode ) declaredtypesHT.get ( s2 );

  tn1.addForwardNode ( tn2 );

  tn2.addBackwardNode ( tn1 );

  declaredtypesHT.put ( s1, tn1 ); 
 
  declaredtypesHT.put ( s2, tn2 );
 }

} catch ( java.lang.RuntimeException e ) {}





 try {

 s1 = (String) /* parameterHT.get ( */ "<java.lang.System: void setIn0(java.io.InputStream)>$0" /* ) */ ;

 s2 = "<java.lang.System: in>";

 if ( ( ( ( TypeNode ) declaredtypesHT.get ( s1 ) ) != null ) && ( ( ( TypeNode ) declaredtypesHT.get ( s2 ) ) != null ) )
 {

  TypeNode tn1 = ( TypeNode ) declaredtypesHT.get ( s1  );

  TypeNode tn2 = ( TypeNode ) declaredtypesHT.get ( s2 );
  
  tn1.addForwardNode ( tn2 );

  tn2.addBackwardNode ( tn1 );

  declaredtypesHT.put ( s1, tn1 ); 
 
  declaredtypesHT.put ( s2, tn2 );
 }

} catch ( java.lang.RuntimeException e ) {}





 s1 = "return_<java.lang.System: java.util.Properties initProperties(java.util.Properties)>";
 s2 = "java.util.Properties";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.lang.Thread: java.lang.Thread currentThread()>";
 s2 = "java.lang.Thread";
 adjustNativeNode ( s1, s2 );


// ADD NATIVE METHOD java.lang.reflect.Field.get(java.lang.Object):java.lang.Object
// ADD NATIVE METHOD java.lang.reflect.Field.set(java.lang.Object,java.lang.Object):void

 try {

 s1 = "return_<java.lang.reflect.Field: java.lang.Object get(java.lang.Object)>";

 s2 = ( String ) /* parameterHT.get ( */ "<java.lang.reflect.Field: void set(java.lang.Object,java.lang.Object)>$0" /* ) */ ;

 if ( ( ( ( TypeNode ) declaredtypesHT.get ( s1 ) ) != null ) && ( ( ( TypeNode ) declaredtypesHT.get ( s2 ) ) != null ) )
 {

 TypeNode tn1 = ( TypeNode ) declaredtypesHT.get ( s1  );
 
 TypeNode tn2 = ( TypeNode ) declaredtypesHT.get ( s2 );

 tn2.addForwardNode ( tn1 );
 
 tn1.addBackwardNode ( tn2 );
 
 tn1.addForwardNode ( tn2 );

 tn2.addBackwardNode ( tn1 );

 declaredtypesHT.put ( s1, tn1 );

 declaredtypesHT.put ( s2, tn2 );

 }


 } catch ( java.lang.RuntimeException e ) {}


// ADD NATIVE METHOD java.lang.reflect.Method.invoke(java.lang.Object,java.lang.Object[]):java.lang.Object

// ADD NATIVE METHOD java.lang.reflect.Constructor.newInstance(java.lang.Object[]):java.lang.Object



 s1 = "return_<java.lang.ClassLoader: java.lang.Class defineClass0(java.lang.String,byte[],int,int)>";
 s2 = "java.lang.Class";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.lang.ClassLoader: java.lang.Class findSystemClass0(java.lang.String)>";
 s2 = "java.lang.Class";
 adjustNativeNode ( s1, s2 );

// ADD NATIVE METHOD java.lang.ClassLoader.getSystemResourceAsStream0(java.lang.String):java.io.InputStream

 s1 = "return_<java.lang.ClassLoader: java.lang.String getSystemResourceAsName0(java.lang.String)>";
 s2 = "java.lang.String";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.lang.SecurityManager: java.lang.Class[] getClassContext()>";
 s2 = "java.lang.Class";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.lang.SecurityManager: java.lang.ClassLoader currentClassLoader()>";
 s2 = "java.lang.ClassLoader";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.lang.SecurityManager: java.lang.Class currentLoadedClass0()>";
 s2 = "java.lang.Class";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.io.ObjectInputStream: java.lang.Class loadClass0(java.lang.Class,java.lang.String)>";
 s2 = "java.lang.Class";
 adjustNativeNode ( s1, s2 );


// ADD NATIVE METHOD java.io.ObjectInputStream.inputClassFields(java.lang.Object,java.lang.Class,int[]):void
// ADD NATIVE METHOD java.io.ObjectInputStream.allocateNewObject(java.lang.Class,java.lang.Class):java.lang.Object
// ADD NATIVE METHOD java.io.ObjectInputStream.allocateNewArray(java.lang.Class,int):java.lang.Object
// ADD NATIVE METHOD java.io.ObjectInputStream.invokeObjectReader(java.lang.Object,java.lang.Class):boolean

// ADD NATIVE METHOD java.lang.Runtime.execInternal(java.lang.String[],java.lang.String[]):java.lang.Process

 s1 = "return_<java.lang.Runtime: java.lang.String initializeLinkerInternal()>";
 s2 = "java.lang.String";
 adjustNativeNode ( s1, s2 );


 s1 = "return_<java.lang.Runtime: java.lang.String buildLibName(java.lang.String,java.lang.String)>";
 s2 = "java.lang.String";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.io.FileDescriptor: java.io.FileDescriptor initSystemFD(java.io.FileDescriptor,int)>";
 s2 = "java.io.FileDescriptor";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.util.ResourceBundle: java.lang.Class[] getClassContext()>";
 s2 = "java.lang.Class";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.io.File: java.lang.String[] list0()>";
 s2 = "java.lang.String";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.io.File: java.lang.String canonPath(java.lang.String)>";
 s2 = "java.lang.String";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.io.ObjectStreamClass: java.lang.String[] getMethodSignatures(java.lang.Class)>";
 s2 = "java.lang.String";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.io.ObjectStreamClass: java.lang.String[] getFieldSignatures(java.lang.Class)>";
 s2 = "java.lang.String";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.io.ObjectStreamClass: java.io.ObjectStreamField[] getFields0(java.lang.Class)>";
 s2 = "java.io.ObjectStreamField";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.net.InetAddressImpl: java.lang.String getLocalHostName()>";
 s2 = "java.lang.String";
 adjustNativeNode ( s1, s2 );

 s1 = "return_<java.net.InetAddressImpl: java.lang.String getHostByAddr(int)>";
 s2 = "java.lang.String";
 adjustNativeNode ( s1, s2 );

}



}
