import java.util.*;

class ParamMap extends HashMap<String, Type> {

}

class Trip{
	Type typ;
	ParamMap par;
	boolean isDec;

	public Trip(Type t, ParamMap p, boolean d){
		typ = t;
		par = p;
		isDec = d;
	}

	public Type First(){
		return typ;
	}

	public ParamMap Second(){
		return par;
	}

	public boolean  Third(){
		return isDec;
	}




}


class ClassPair{
	Declarations locals;
	Functions funcs;

	public ClassPair(Declarations d, Functions f){
		locals = d;
		funcs = f;
	}

	public Declarations locs(){
		return locals;
	}

	public Functions functs(){
		return funcs;
	}
}

class ClassMap extends HashMap<String, ClassPair>{

}


public class TypeMap extends HashMap<String, Trip> { 

// TypeMap is implemented as a Java HashMap.  
// Plus a 'display' method to facilitate experimentation.
void display(){
	for(Map.Entry<String, Trip> entry : entrySet()){
		String key = entry.getKey();
		Type value = entry.getValue().First();
		ParamMap pm = entry.getValue().Second();
		if (! entry.getValue().Third()){
			System.out.print("{ <" + key + "," + value + ",\n  { ");
			for(Map.Entry<String, Type> pEntry : pm.entrySet()){
				String pKey = pEntry.getKey();
				Type pValue = pEntry.getValue();
				System.out.print("<" + pKey + "," + pValue + "> ");
			}
			System.out.print("}>,\n");
		}
		else{
			System.out.println("<" + key + "," + value + ">");
		}
	}
	System.out.print("\n");
}
}
