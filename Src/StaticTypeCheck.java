// StaticTypeCheck.java

import java.util.*;

// Static type checking for Clite is defined by the functions 
// V and the auxiliary functions typing and typeOf.  These
// functions use the classes in the Abstract Syntax of Clite.


public class StaticTypeCheck {
	
	//function to see if a given global is overridden by local
	//vars or parameters. Used in typing. ~TRP
	public static boolean checkGlobalOrFunct(String gf, Function f){
		for(Declaration l : f.locals){
			if(l.v.toString() == gf){
				return true;
			}
		}
		for(Declaration p : f.params){
			if(p.v.toString() == gf){
				return true;
			}
		}
		return false;
	}
	
	//Things to rework
	//1. Change function id type to Variable rather than String
	//2. Change checkGlobalOrFunct to reflect that
	//3. Finish functions in typing by adding the ParamMap for the given function.
    public static TypeMap typing (Declarations glob, Functions fns, Function f) {
        TypeMap map = new TypeMap();
        for (Declaration d : glob){
			String g = d.v.toString();
			if(checkGlobalOrFunct(g, f)){
				continue;
			}
			else{
				Trip s = new Trip(d.t, null, true);	
            	map.put (g, s);
			}
		}
		for (Function fn : fns){
			ParamMap pmap = new ParamMap();
			if(checkGlobalOrFunct(fn.id, f)){
				continue;
			}
			else{
				for(Declaration p : fn.params){
					pmap.put (p.v.toString(), p.t);
				}
				Trip s = new Trip(fn.t, pmap, false);
				map.put (fn.id, s);
			}
		}
		for(Declaration p : f.params){
			Trip s = new Trip(p.t, null, true);
			map.put (p.v.toString(), s);
		}
		for(Declaration l : f.locals){
			Trip s = new Trip(l.t, null, true);
			map.put (l.v.toString(), s);
		}
        return map;
    }

	//Separate Map for Classes containing
	//The name of the Class as the key and
	//a Pair of the locals and the functions
	//as the value.
	public static ClassMap typingC (Classes cls) {
		ClassMap map = new ClassMap();
		for(Class c : cls){
			ClassPair lf = new ClassPair(c.locals, c.fns);
			map.put(c.id.toString(),lf);
		}
		return map;
	}

    public static void check(boolean test, String msg) {
        if (test)  return;
        System.err.println(msg);
        System.exit(1);
    }
	
	//Changed program V
	//~TRP
    public static void V (Declarations d, Functions fns, Interfaces ins, Classes cls) {
		boolean isVoid;
		boolean hasMain = false;
		//globals
        for (int i=0; i<d.size(); i++){
			//Check for void type
			boolean hasClass = false;
			Declaration g = d.get(i);
			if(g.t.toString() == "void"){
				isVoid = true;
			}
			else{
				isVoid = false;
			}
			check(! (isVoid), g.v + " cannot have type void");
            for (int j=i+1; j<d.size(); j++) {
                Declaration di = d.get(i);
                Declaration dj = d.get(j);
                check( ! (di.v.equals(dj.v)),
                       "duplicate declaration: " + dj.v);
            }
			for(int k=0; k<fns.size(); k ++){
				Declaration gi = d.get(i);
				Function fun = fns.get(k);
				check ( ! (gi.v.toString() == fun.id), 
						"duplicate declaration: " + fun.id);
			}
			//If the type of the global variable is
			//Not one of the standard ones, then check
			//to make sure there exists a class with the
			//same name.
			if(g.t != Type.VOID && g.t != Type.INT && g.t != Type.FLOAT && g.t != Type.BOOL && g.t != Type.CHAR ){
				for(int h = 0; h < cls.size(); h++){
					Class c = cls.get(h);
					if(c.id.toString().equals(g.t.toString()))
							hasClass = true;
				}
				check(hasClass, "No class defined for type: " + g.t.toString());
			}
		}

		//functions
		for (int a=0; a<fns.size(); a++){
			for(int b=a+1; b<fns.size(); b++){
				Function fi = fns.get(a);
				Function fj = fns.get(b);
				check ( ! (fi.id == fj.id),
						"duplicate function declarations: " + fj.id);
			}
			Function fn = fns.get(a);
			if(fn.id == "main"){
				hasMain = true;
			}
		}
		check ( hasMain, "No Main Method");

		//Classes
		for(int z = 0; z < cls.size(); z++){
			boolean hasInterface = false;
			Class c = cls.get(z);
			//First see if there is a duplicate class
			//name.
			for(int y = z+1; y < ins.size(); y++){
				Class c2 = cls.get(y);
				check(! (c.id.equals(c2.id)),
						"duplicate class declarations: " + c2.id);
			}
			//Then make sure that that an interface exists
			//for that class and that all the methods correspond
			//to a methods in the class.
			for(int x = 0; x < ins.size() ; x++){
				Interface in = ins.get(x);
				
				if(c.id.equals(in.id)){
					for(Function fc : c.fns){
						boolean matchMethod = false;
						for(Method m : in.methods){
							if(m.id.equals(fc.id))
								matchMethod = true;
						}
						check(matchMethod, "No method " + fc.id + " for function in Class " + c.id);
					}
					hasInterface = true;
				}
			}
			check(hasInterface, "No interface defined for class: " + c.id);
			
		}

		//Interfaces
		//
		//First make sure that each Interface
		//id is unique
		for(int w = 0; w < ins.size(); w++){
			Interface in = ins.get(w);
			for(int u = w+1; u < ins.size(); u++){
				Interface in2 = ins.get(u);
				check(! (in.id.equals(in2)),
						"duplicate interface declarations: " + in2.id);
			}

			//Then find the class that is the 
			//implementation of the given interface
			//and make sure that all the methods in
			//the interface are implemented in the 
			//class
			for(Class ci : cls){
				if(in.id.equals(ci.id)){
					for(Method mi : in.methods){
						boolean matchFunct = false;
						for(Function fi: ci.fns){
							if(mi.id.equals(fi.id)){
								matchFunct = true;
							}
						}
						check(matchFunct, "Missing method " + mi.id + " from class " + ci.id);
					}
				}

			}
		}
    } 

    public static void V (Program p) {
		ClassMap cmap = typingC(p.cls);
		Declarations gAndC = new Declarations();
		Functions fAndC = new Functions();
		gAndC.addAll(p.globals);
		fAndC.addAll(p.functions);
		for(Class c : p.cls){
			gAndC.addAll(c.locals);
			fAndC.addAll(c.fns);
		}
		V (gAndC, fAndC, p.ins, p.cls);

		for(Class c : p.cls){
			gAndC = new Declarations();
			gAndC.addAll(p.globals);
			gAndC.addAll(c.locals);
			for(Function f : c.fns){
				V (f, typing (gAndC, c.fns, f), cmap);
			}
		}
		for(Function f : p.functions){
       		 V (f, typing (p.globals, p.functions, f), cmap);
		}
    }

	//new V for functions ~TRP
	public static void V (Function f, TypeMap tm, ClassMap cm){
		V(f.params, f.locals);
		V(f.body, tm, cm);
		if((f.t != Type.VOID) && (f.id != "main")){
			boolean hasReturn = findReturnB(f.body, f.t, tm, cm);
			check( hasReturn, "function " + f.id + " must have return");
		}
		else{
			boolean hasReturn = findReturnB(f.body, f.t, tm, cm);
			check( (! hasReturn), "function " + f.id + " cannot have return");
		}
			
	}
	//A function to check for returns within
	//function body ~TRP
	public static boolean findReturnB(Block b, Type t, TypeMap tm, ClassMap cm){
		boolean hasReturn = false;
		for(Statement s : b.members){
			if(s instanceof Return){
				Return r = (Return)s;
				check( (typeOf(r.result, tm, cm) == t),
						"Return type does not match function type");
				hasReturn = true;
			}
			else if(s instanceof Conditional) {
				Conditional c = (Conditional)s;
				boolean hasCR = findReturnC(c, t, tm, cm);
				if(hasCR){
					hasReturn = true;
				}

			}
			else if(s instanceof Loop) {
				Loop l = (Loop)s;
				boolean hasLR = findReturnL(l, t, tm, cm);
				if(hasLR){
					hasReturn = true;
				}
			}
		}
		return hasReturn;
	}
	//A function to check for returns in conditionals within
	//function body. ~TRP
	public static boolean findReturnC(Conditional c, Type t, TypeMap tm, ClassMap cm){
		boolean hasReturn = false;
		if(c.thenbranch instanceof Return){
			Return r = (Return)c.thenbranch;
			check( (typeOf(r.result, tm, cm) == t),
					"Return type does not match function type");
			hasReturn = true;
		}
		if(c.elsebranch instanceof Return){
			Return r = (Return)c.elsebranch;
			check( (typeOf(r.result, tm, cm) == t),
					"Return type does not match function type");
			hasReturn = true;
		}
		if(c.thenbranch instanceof Block){
			Block b = (Block)c.thenbranch;
			boolean hasBR = findReturnB(b, t, tm, cm);
			if(hasBR){
				hasReturn = true;
			}
		}
		if(c.elsebranch instanceof Block){
			Block b = (Block)c.elsebranch;
			boolean hasBR = findReturnB(b, t, tm, cm);
			if(hasBR){
				hasReturn = true;
			}
		}
		if(c.thenbranch instanceof Conditional){
			Conditional th = (Conditional)c.thenbranch;
			boolean hasCR = findReturnC(th, t, tm, cm);
			if(hasCR){
				hasReturn = true;
			}
		}
		if(c.elsebranch instanceof Conditional){
			Conditional e = (Conditional)c.elsebranch;
			boolean hasCR = findReturnC(e, t, tm, cm);
			if(hasCR){
				hasReturn = true;
			}
		}
		if(c.thenbranch instanceof Loop){
			Loop l = (Loop)c.thenbranch;
			boolean hasLR = findReturnL(l, t, tm, cm);
			if(hasLR){
				hasReturn = true;
			}
		}
		if(c.elsebranch instanceof Loop){
			Loop l = (Loop)c.elsebranch;
			boolean hasLR = findReturnL(l, t, tm, cm);
			if(hasLR){
				hasReturn = true;
			}
		}

		return hasReturn;
	}

	public static boolean findReturnL(Loop l, Type t, TypeMap tm, ClassMap cm){
		boolean hasReturn = false;
		if(l.body instanceof Return){
			Return r = (Return)l.body;
			check( (typeOf(r.result, tm, cm) == t),
					"Return type does not match function type");
			hasReturn = true;
		}
		if(l.body instanceof Block){
			Block b = (Block)l.body;
			boolean hasBR = findReturnB(b, t, tm, cm);
			if(hasBR){
				hasReturn = true;
			}
		}
		if(l.body instanceof Conditional){
			Conditional c = (Conditional)l.body;
			boolean hasCR = findReturnC(c, t, tm, cm);
			if(hasCR){
				hasReturn = true;
			}
		}
		if(l.body instanceof Loop){
			Loop ll = (Loop)l.body;
			boolean hasLR = findReturnL(ll, t, tm, cm);
			if(hasLR){
				hasReturn = true;
			}
		}
		return hasReturn;		
	}
	
	//new V for function params and locals ~TRP
	public static void V (Declarations param, Declarations locals){
			for (int i=0; i<param.size(); i ++){
				for (int j=i+1; j<param.size(); j++){
					Declaration pi = param.get(i);
					Declaration pj = param.get(j);
					check( ! (pi.v.equals(pj.v)),
							"duplicate declaration: " + pj.v);
				}
				for(int k=0; k<locals.size(); k ++){
					Declaration ppi = param.get(i);
					Declaration loc = locals.get(k);
					check(! (ppi.v.equals(loc.v)),
							"duplicate declaration: " + loc.v);
				}
			}
			for (int h=0; h<locals.size() - 1; h++){
				for(int g=h+1; g<locals.size(); g++){
					Declaration loch = locals.get(h);
					Declaration locg = locals.get(g);
					check(! (loch.v.equals(locg.v)),
							"duplicate declaration: " + locg.v);
				}
			}
	}
	

    public static Type typeOf (Expression e, TypeMap tm, ClassMap cm) {
        if (e instanceof Value) return ((Value)e).type;
        if (e instanceof Variable) {
			if(e instanceof OVariable){
				OVariable v = (OVariable)e;
				Declarations locC = null;
				Type t = objType(v.object, tm);
				for(Map.Entry<String, ClassPair> entry : cm.entrySet()){
					String key = entry.getKey();
					if(key.equals(t.toString())){
						locC = entry.getValue().locs();
					}
				}
				boolean hasLocal = false;
				Type getType = null;
				for(Declaration l : locC){
					if(l.v.toString().equals(v.toString())){
						hasLocal = true;
						getType = l.t;
					}
				}
				check(hasLocal,
						"undeclared variable " + v);
				return getType;
			}

			else{
            	Variable v = (Variable)e;
            	check (tm.containsKey(v.toString()), "undefined variable: " + v);
            	return (Type) tm.get(v.toString()).First();
			}
        }
        if (e instanceof Binary) {
            Binary b = (Binary)e;
            if (b.op.ArithmeticOp( ))
                if (typeOf(b.term1,tm, cm)== Type.FLOAT)
                    return (Type.FLOAT);
                else return (Type.INT);
            if (b.op.RelationalOp( ) || b.op.BooleanOp( )) 
                return (Type.BOOL);
        }
        if (e instanceof Unary) {
            Unary u = (Unary)e;
            if (u.op.NotOp( ))        return (Type.BOOL);
            else if (u.op.NegateOp( )) return typeOf(u.term,tm, cm);
            else if (u.op.intOp( ))    return (Type.INT);
            else if (u.op.floatOp( )) return (Type.FLOAT);
            else if (u.op.charOp( ))  return (Type.CHAR);
        }
		if (e instanceof CallE) {
			if(e instanceof MCallE){
				MCallE mc = (MCallE)e;
				Functions fns = null;
				Type type = objType(mc.object, tm);
				for(Map.Entry<String, ClassPair> entry: cm.entrySet()){
					String key = entry.getKey();
					if(key.equals(type.toString())){
						fns = entry.getValue().functs();
					}
				}
				for(Function f : fns){
					if(mc.id.equals(f.id)){
						check( (f.params.size() == mc.args.size()),
								"Wrong number of parameters on method call: " + mc.id);
						int placeMarker = 0;
						for(Declaration p : f.params){
							Expression arg = mc.args.get(placeMarker);
							Type pType = p.t ;
							check( (typeOf(arg, tm, cm) == pType),
									"type of argument does not match parameter in method call " + mc.id);
							placeMarker ++;
							
						}
						return f.t;
					}
					else{
						continue;
					}
				}
				check(false, "Could not find " + mc.id);				
			}

			else{
				CallE c = (CallE)e;
				//boolean HasFunction = false;
				for(Map.Entry<String, Trip> entry : tm.entrySet()){
					String key = entry.getKey();
					Type type = entry.getValue().First();
					ParamMap p = entry.getValue().Second();
					if(c.id.equals(key)){
						check( (p.size() == c.args.size()),
								"Wrong number of parameters on function call: " + c.id);
						int placeMarker = 0;
						for(Map.Entry<String, Type> pEntry : p.entrySet()){
							Expression arg = c.args.get(placeMarker);
							Type pType = pEntry.getValue();
							check( (typeOf(arg, tm, cm) == pType),
									"type of argument does not match parameter in function call " + c.id);
							placeMarker ++;
						}
						return type;
					}
				}
					check( false, "Could not find " + c.id);
			}
		}
        throw new IllegalArgumentException("should never reach here");
    } 

    public static void V (Expression e, TypeMap tm, ClassMap cm) {
        if (e instanceof Value) 
            return;
        if (e instanceof Variable) {
			if(e instanceof OVariable){
				OVariable v = (OVariable)e;
				Declarations locC = null;
				Type t = objType(v.object, tm);
				for(Map.Entry<String, ClassPair> entry : cm.entrySet()){
					String key = entry.getKey();
					if(key.equals(t.toString())){
						locC = entry.getValue().locs();
					}
				}
				boolean hasLocal = false;
				for(Declaration l : locC){
					if(l.v.toString().equals(v.toString())){
						hasLocal = true;
					}
				}
				check(hasLocal,
						"undeclared variable " + v);
				return;
			}
			else{	
            	Variable v = (Variable)e;
            	check( tm.containsKey(v.toString())
                	   , "undeclared variable: " + v);
            	return;
			}
        }
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            Type typ1 = typeOf(b.term1, tm, cm);
            Type typ2 = typeOf(b.term2, tm, cm);
            V (b.term1, tm, cm);
            V (b.term2, tm, cm);
            if (b.op.ArithmeticOp( ))  
                check( typ1 == typ2 &&
                       (typ1 == Type.INT || typ1 == Type.FLOAT)
                       , "type error for " + b.op);
            else if (b.op.RelationalOp( )) 
                check( typ1 == typ2 , "type error for " + b.op);
            else if (b.op.BooleanOp( )) 
                check( typ1 == Type.BOOL && typ2 == Type.BOOL,
                       b.op + ": non-bool operand");
            else
                throw new IllegalArgumentException("should never reach here");
            return;
        }
		if (e instanceof Unary){
			Unary u = (Unary)e;
			Type typ = typeOf(u.term, tm, cm);
			V(u.term, tm, cm);
			if(u.op.NotOp()){
				check(typ == Type.BOOL
						, u.op + ": non-bool operand");
			}
			else if(u.op.NegateOp()){
				check(typ == Type.INT || typ == Type.FLOAT
						, u.op + " requires int or float operand");
			}
			else if(u.op.intOp()){
				check(typ == Type.FLOAT || typ == Type.CHAR
						, u.op + " requires float or char operand");
			}
			else if(u.op.floatOp() || u.op.charOp()){
				check(typ == Type.INT
						, u.op + " requires int operand");
			}
			else{
				throw new IllegalArgumentException("should never reach here");
			}
			return;
		}
		if (e instanceof CallE){
			if(e instanceof MCallE){
				MCallE mc = (MCallE)e;
				Functions fns = null;
				Type type = objType(mc.object, tm);
				for(Map.Entry<String, ClassPair> entry: cm.entrySet()){
					String key = entry.getKey();
					if(key.equals(type.toString())){
						fns = entry.getValue().functs();
					}
				}
				boolean HasFunction = false;
				for(Function f : fns){
					if(mc.id.equals(f.id)){
						HasFunction = true;
						check( (f.params.size() == mc.args.size()),
								"Wrong number of parameters on method call: " + mc.id);
						int placeMarker = 0;
						for(Declaration p : f.params){
							Expression arg = mc.args.get(placeMarker);
							Type pType = p.t ;
							check( (typeOf(arg, tm, cm) == pType),
									"type of argument does not match parameter in method call " + mc.id);
							placeMarker ++;
						}
					}
					else{
						continue;
					}
				}
				check( HasFunction,
					"No function " + mc.id + " found");
				return;
				
			}
			else{
				CallE c = (CallE)e;
				boolean HasFunction = false;
				for(Map.Entry<String, Trip> entry : tm.entrySet()){
					String key = entry.getKey();
					Type type = entry.getValue().First();
					ParamMap p = entry.getValue().Second();
					if(c.id.equals(key)){
						HasFunction = true;
						check( (p.size() == c.args.size()),
								"Wrong number of parameters on function call: " + c.id);
						int placeMarker = 0;
						for(Map.Entry<String, Type> pEntry : p.entrySet()){
							Expression arg = c.args.get(placeMarker);
							Type pType = pEntry.getValue();
							check( (typeOf(arg, tm, cm) == pType),
									"type of argument does not match parameter in function call " + c.id);
							placeMarker ++;
					}
				}
				else{
					continue;
				}
			}
			check( HasFunction,
				"No function " + c.id + " found");	
			return;
			}	
		}
        // student exercise
        throw new IllegalArgumentException("should never reach here");
    }

    public static void V (Statement s, TypeMap tm, ClassMap cm) {
        if ( s == null )
            throw new IllegalArgumentException( "AST error: null statement");
        if (s instanceof Skip) return;
        if (s instanceof Assignment) {
            Assignment a = (Assignment)s;
            check( tm.containsKey(a.target.toString())
                   , " undefined target in assignment: " + a.target);
            V(a.source, tm, cm);
            Type ttype = (Type)tm.get(a.target.toString()).First();
            Type srctype = typeOf(a.source, tm, cm);
            if (ttype != srctype) {
                if (ttype == Type.FLOAT)
                    check( srctype == Type.INT
                           , "mixed mode assignment to " + a.target);
                else if (ttype == Type.INT)
                    check( srctype == Type.CHAR
                           , "mixed mode assignment to " + a.target);
                else
                    check( false
                           , "mixed mode assignment to " + a.target);
            }
            return;
        }
	   if(s instanceof Conditional) {
			Conditional c = (Conditional)s;
			V(c.test, tm, cm);
			Type ttype = typeOf(c.test, tm, cm);
			check(ttype == Type.BOOL
					, "Expression " + c.test + " is not boolean");
			V(c.thenbranch, tm, cm);
			V(c.elsebranch, tm, cm);
			return;
		}
	  if(s instanceof Loop) {
			Loop l = (Loop)s;
			V(l.test, tm, cm);
			Type ttype = typeOf(l.test, tm, cm);
			check(ttype == Type.BOOL
					, "Expression " + l.test + " is not boolean");
			V(l.body, tm, cm);
			return;
		}
	 if(s instanceof Block) {
			Block b = (Block)s;
			for( Statement state : b.members){
				V(state, tm, cm);
			}
			return;
		}
	if(s instanceof Call) {
		//If s is a method call
		//Find the type of the object
		//find the class the same name
		//as the type
		//find the function that matches
		//the call id
		//make sure they have the right params
		if(s instanceof MCall){
			MCall mc = (MCall)s;
			Functions fns = null;
			Type type = objType(mc.object, tm);
			for(Map.Entry<String, ClassPair> entry: cm.entrySet()){
				String key = entry.getKey();
				if(key.equals(type.toString())){
					fns = entry.getValue().functs();
				}
			}
			boolean HasFunction = false;
			for(Function f : fns){
				if(f.t == Type.VOID && mc.id.equals(f.id)){
					HasFunction = true;
					check( (f.params.size() == mc.args.size()),
							"Wrong number of parameters on method call: " + mc.id);
					int placeMarker = 0;
					for(Declaration p : f.params){
						Expression arg = mc.args.get(placeMarker);
						Type pType = p.t ;
						check( (typeOf(arg, tm, cm) == pType),
								"type of argument does not match parameter in method call " + mc.id);
						placeMarker ++;
					}
				}
				else{
					continue;
				}
			}
			check( HasFunction,
				"No function " + mc.id + " found");
			return;

		}


		else{
			Call c = (Call)s;
			boolean HasFunction = false;
			for(Map.Entry<String, Trip> entry : tm.entrySet()){
				String key = entry.getKey();
				Type type = entry.getValue().First();
				ParamMap p = entry.getValue().Second();
				if(type == Type.VOID && c.id.equals(key)){
					HasFunction = true;
					check( (p.size() == c.args.size()),
							"Wrong number of parameters on function call: " + c.id);
					int placeMarker = 0;
					for(Map.Entry<String, Type> pEntry : p.entrySet()){
						Expression arg = c.args.get(placeMarker);
						Type pType = pEntry.getValue();
						check( (typeOf(arg, tm, cm) == pType),
								"type of argument does not match parameter in function call " + c.id);
						placeMarker ++;
					}
				}
				else{
					continue;
				}
			}
			check( HasFunction,
				"No function " + c.id + " found");
			return;
		}	
	}
    if(s instanceof Return) {
		Return r = (Return)s;
		for(Map.Entry<String, Trip> entry : tm.entrySet()){
			if(r.target.toString() == entry.getKey()){
				check( (typeOf(r.result, tm, cm) == entry.getValue().First()),
						"Return type not matching for function " + entry.getKey());
			}		
		}
		return;
 	 } 	 
        // student exercise
        throw new IllegalArgumentException("should never reach here");
  }

  //Function used with MCall to get the
  //type of the object that way you can
  //see if there exists a class that
  //matches it.
  private static Type objType(String obj, TypeMap tm){
	  Type type = null;
	  check(tm.containsKey(obj),
			  "undeclared variable: " + obj);
	for(Map.Entry<String, Trip> entry : tm.entrySet()){
		String key = entry.getKey();
		Type t = entry.getValue().First();
		if(key.equals(obj)){
			type = t;
			break;
		}
	}
	return type;
  }

    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display();           // student exercise
        System.out.println("\nBegin type checking...");
        System.out.println("Type map:");
		System.out.print("Globals: \n {");
		for(Declaration g : prog.globals){
			System.out.print(" <" + g.v + "," + g.t + "> ");
		}
		System.out.println("}");
		System.out.print("\n");
		System.out.println("Classes:");
		for(Class c : prog.cls){
			Declarations gAndC = new Declarations();
			Functions fAndC = new Functions();
			gAndC.addAll(prog.globals);
			gAndC.addAll(c.locals);
			fAndC.addAll(prog.functions);
			fAndC.addAll(c.fns);
			System.out.println("Class " + c.id);
			System.out.print("Declarations: \n {");
			for(Declaration l : c.locals){
				System.out.print(" <" + l.v + "," + l.t + "> ");
			}
			System.out.println("}");
			System.out.println("Functions: ");
			for(Function f: c.fns){
				TypeMap cmap = typing(gAndC, fAndC, f);
				System.out.println("Function " + f.id + "=");
				cmap.display();

			}
		}
		for(Function f : prog.functions){
        TypeMap map = typing(prog.globals, prog.functions, f);
		System.out.println("Function " + f.id + "=");
        map.display();
		}
		// student exercise
        V(prog);
    } //main

} // class StaticTypeCheck

