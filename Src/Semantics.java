// Following is the semantics class:
// The meaning M of a Statement is a State
// The meaning M of a Expression is a Value

public class Semantics {

	//Allocating memory function ~TRP
	State allocate (Declarations d, State state){
		for(Declaration dec : d){
			state.delta.put(state.a, dec.v);
			state.mu.put(state.a, "undefined");
			state.a = state.a + 1;
		}
		return state;
	}

	//Deallocating memory function ~TRP
	State deallocate (Declarations d, State state){
		for(Declaration dec : d){
			state.delta.remove(state.a);
			state.mu.put(state.a, "unused");
			state.a = state.a - 1;
		}
		return state;
	}

	/* OLD VERSION
    State M (Program p) { 
        return M (p.body, initialState(p.decpart)); 
    }
    */

	State M (Program p){
		State state = new State(50);
		state = allocate(p.globals, state);
		for(Function f : p.functions){
			if(f.id.equals("main"))
				return M(f, state);
			else
				continue;
		}
		throw new IllegalArgumentExecption("you have no main function");
	}

    State initialState (Declarations d) {
        State state = new State();
        Value intUndef = new IntValue();
        for (Declaration decl : d)
            state.put(decl.v, Value.mkValue(decl.t));
        return state;
    }
  
  
    State M (Statement s, State state) {
        if (s instanceof Skip) return M((Skip)s, state);
        if (s instanceof Assignment)  return M((Assignment)s, state);
        if (s instanceof Conditional)  return M((Conditional)s, state);
        if (s instanceof Loop)  return M((Loop)s, state);
        if (s instanceof Block)  return M((Block)s, state);
        throw new IllegalArgumentException("should never reach here");
    }
  
    State M (Skip s, State state) {
        return state;
    }
  
    State M (Assignment a, State state) {
        return state.onion(a.target, M (a.source, state));
    }
  
    State M (Block b, State state) {
        for (Statement s : b.members)
            state = M (s, state);
        return state;
    }
  
    State M (Conditional c, State state) {
        if (M(c.test, state).boolValue( ))
            return M (c.thenbranch, state);
        else
            return M (c.elsebranch, state);
    }
  
    State M (Loop l, State state) {
        if (M (l.test, state).boolValue( ))
            return M(l, M (l.body, state));
        else return state;
    }

	int intOf(boolean x){
		if(x){
			return 1;
		}
		else{
			return 0;
		}
	}

    Value applyBinary (Operator op, Value v1, Value v2) {
        StaticTypeCheck.check( ! v1.isUndef( ) && ! v2.isUndef( ),
               "reference to undef value");
        if (op.val.equals(Operator.INT_PLUS)) 
            return new IntValue(v1.intValue( ) + v2.intValue( ));
        if (op.val.equals(Operator.INT_MINUS)) 
            return new IntValue(v1.intValue( ) - v2.intValue( ));
        if (op.val.equals(Operator.INT_TIMES)) 
            return new IntValue(v1.intValue( ) * v2.intValue( ));
        if (op.val.equals(Operator.INT_DIV)) 
            return new IntValue(v1.intValue( ) / v2.intValue( ));

		if (op.val.equals(Operator.FLOAT_PLUS))
			return new FloatValue(v1.floatValue( ) + v2.floatValue( ));
		if (op.val.equals(Operator.FLOAT_MINUS))
			return new FloatValue(v1.floatValue( ) - v2.floatValue( ));
		if (op.val.equals(Operator.FLOAT_TIMES))
			return new FloatValue(v1.floatValue( ) * v2.floatValue( ));
		if (op.val.equals(Operator.FLOAT_DIV))
			return new FloatValue(v1.floatValue( ) / v2.floatValue( ));

		if (op.val.equals(Operator.INT_LT))
			return new BoolValue(v1.intValue( ) < v2.intValue( ));
		if (op.val.equals(Operator.INT_LE))
			return new BoolValue(v1.intValue( ) <= v2.intValue( ));
		if (op.val.equals(Operator.INT_EQ))
			return new BoolValue(v1.intValue( ) == v2.intValue( ));
		if (op.val.equals(Operator.INT_NE))
			return new BoolValue(v1.intValue( ) != v2.intValue( ));
		if (op.val.equals(Operator.INT_GT))
			return new BoolValue(v1.intValue( ) > v2.intValue( ));
		if (op.val.equals(Operator.INT_GE))
			return new BoolValue(v1.intValue( ) >= v2.intValue( ));
		
		if (op.val.equals(Operator.FLOAT_LT))
			return new BoolValue(v1.floatValue( ) < v2.floatValue( ));
		if (op.val.equals(Operator.FLOAT_LE))
			return new BoolValue(v1.floatValue( ) <= v2.floatValue( ));
		if (op.val.equals(Operator.FLOAT_EQ))
			return new BoolValue(v1.floatValue( ) ==  v2.floatValue( ));
		if (op.val.equals(Operator.FLOAT_NE))
			return new BoolValue(v1.floatValue( ) != v2.floatValue( ));
		if (op.val.equals(Operator.FLOAT_GT))
			return new BoolValue(v1.floatValue( ) > v2.floatValue( ));
		if (op.val.equals(Operator.FLOAT_GE))
			return new BoolValue(v1.floatValue( ) >= v2.floatValue( ));

		if (op.val.equals(Operator.CHAR_LT))
			return new BoolValue(v1.charValue( ) < v2.charValue( ));
		if (op.val.equals(Operator.CHAR_LE))
			return new BoolValue(v1.charValue( ) <= v2.charValue( ));
		if (op.val.equals(Operator.CHAR_EQ))
			return new BoolValue(v1.charValue( ) == v2.charValue( ));
		if (op.val.equals(Operator.CHAR_NE))
			return new BoolValue(v1.charValue( ) != v2.charValue( ));
		if (op.val.equals(Operator.CHAR_GT))
			return new BoolValue(v1.charValue( ) > v2.charValue( ));
		if (op.val.equals(Operator.CHAR_GE))
			return new BoolValue(v1.charValue( ) >= v2.charValue( ));

		
		if (op.val.equals(Operator.BOOL_LT))
			return new BoolValue(intOf(v1.boolValue( )) > intOf(v2.boolValue( )));
		if (op.val.equals(Operator.BOOL_LE))
			return new BoolValue(intOf(v1.boolValue( )) >= intOf(v2.boolValue( )));
		if (op.val.equals(Operator.BOOL_EQ))
			return new BoolValue(intOf(v1.boolValue( )) == intOf(v2.boolValue( )));
		if (op.val.equals(Operator.BOOL_NE))
			return new BoolValue(intOf(v1.boolValue( )) !=  intOf(v2.boolValue( )));
		if (op.val.equals(Operator.BOOL_GT))
			return new BoolValue(intOf(v1.boolValue( )) > intOf(v2.boolValue( )));
		if (op.val.equals(Operator.BOOL_GE))
			return new BoolValue(intOf(v1.boolValue( )) >=  intOf(v2.boolValue( )));
		if (op.val.equals(Operator.BOOL_AND))
			return new BoolValue(v1.boolValue( ) && v2.boolValue( ));
		if (op.val.equals(Operator.BOOL_OR))
			return new BoolValue(v1.boolValue( ) || v2.boolValue( ));

       // student exercise
        throw new IllegalArgumentException("should never reach here");
    } 
    
    Value applyUnary (Operator op, Value v) {
        StaticTypeCheck.check( ! v.isUndef( ),
               "reference to undef value");
        if (op.val.equals(Operator.NOT))
            return new BoolValue(!v.boolValue( ));
        else if (op.val.equals(Operator.INT_NEG))
            return new IntValue(-v.intValue( ));
        else if (op.val.equals(Operator.FLOAT_NEG))
            return new FloatValue(-v.floatValue( ));
        else if (op.val.equals(Operator.I2F))
            return new FloatValue((float)(v.intValue( ))); 
        else if (op.val.equals(Operator.F2I))
            return new IntValue((int)(v.floatValue( )));
        else if (op.val.equals(Operator.C2I))
            return new IntValue((int)(v.charValue( )));
        else if (op.val.equals(Operator.I2C))
            return new CharValue((char)(v.intValue( )));
        throw new IllegalArgumentException("should never reach here");
    } 

    Value M (Expression e, State state) {
        if (e instanceof Value) 
            return (Value)e;
        if (e instanceof Variable) 
            return (Value)(state.get(e));
        if (e instanceof Binary) {
            Binary b = (Binary)e;
            return applyBinary (b.op, 
                                M(b.term1, state), M(b.term2, state));
        }
        if (e instanceof Unary) {
            Unary u = (Unary)e;
            return applyUnary(u.op, M(u.term, state));
        }
        throw new IllegalArgumentException("should never reach here");
    }

    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display();    // student exercise
        System.out.println("\nBegin type checking...");
        System.out.println("Type map:");
        TypeMap map = StaticTypeCheck.typing(prog.decpart);
        map.display();    // student exercise
        StaticTypeCheck.V(prog);
        Program out = TypeTransformer.T(prog, map);
        System.out.println("Output AST");
         out.display();    // student exercise
        Semantics semantics = new Semantics( );
        State state = semantics.M(out);
        System.out.println("Final State");
         state.display( );  // student exercise
    }
}
