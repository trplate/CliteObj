import java.util.*;

public class Parser {
    // Recursive descent parser that inputs a C++Lite program and 
    // generates its abstract syntax.  Each method corresponds to
    // a concrete syntax grammar rule, which appears as a comment
    // at the beginning of the method.
  
    Token token;          // current token from the input stream
    Lexer lexer;
  
    public Parser(Lexer ts) { // Open the C++Lite source program
        lexer = ts;                          // as a token stream, and
        token = lexer.next();            // retrieve its first Token
    }
  
    private String match (TokenType t) {
        String value = token.value();
        if (token.type().equals(t))
            token = lexer.next();
        else
            error(t);
        return value;
    }
  
    private void error(TokenType tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }
  
    private void error(String tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }
  
    public Program program() {
        // Program
		Program p = new Program(null,null,null,null);
		Declarations ds = new Declarations();
		Functions fns = new Functions();
		Interfaces ins = new Interfaces();
		Classes cls = new Classes();
		//Parse Interfaces
		while(token.type().equals(TokenType.Interface)){
			Interface i = interFace();
			ins.add(i);
		}
		//Parse Classes
		while(token.type().equals(TokenType.Class)){
			Class c = classIm();
			cls.add(c);
		}
		while(isType() || token.type().equals(TokenType.Identifier)){
			Variable v;
			Type t;
			if(isType()){
				t = type();
			}
			else{
				Variable i = new Variable(match(TokenType.Identifier));
				t = new iType(i.toString());
			}
			//Check for Main
			if(token.type().equals(TokenType.Main)){
				v = new Variable(match(TokenType.Main));
				//match(TokenType.LeftParen);
				Declarations par = parameters();
				//match(TokenType.RightParen);
				match(TokenType.LeftBrace);
				Declarations d = declarations();
				Block b = statements(v);
				match(TokenType.RightBrace);
				Function m = new Function(t, v.toString(), par, d, b);
				fns.add(m);
				p = new Program(ins, cls, ds, fns);
				break;
			}
			//Check for FunctionOrGlobal
			else{
				//Check for globals of type t
				v = new Variable(match(TokenType.Identifier));
				if(token.type().equals(TokenType.Comma) || token.type().equals(TokenType.Semicolon)){
					Declaration globd;
					Variable globv;
					globv = v;
					globd = new Declaration(globv, t);
					ds.add(globd);
					while(token.type().equals(TokenType.Comma)){
						match(TokenType.Comma);
						globv = new Variable(match(TokenType.Identifier));
						globd = new Declaration(globv, t);
						ds.add(globd);
					}
					match(TokenType.Semicolon);
				}
				//Check for functions
				else{
					Declarations par = parameters();
					match(TokenType.LeftBrace);
					Declarations d = declarations();
					Block b = statements(v);
					match(TokenType.RightBrace);
					Function f = new Function(t, v.toString(), par, d, b);
					fns.add(f);
			}

		}
    }
	return p;
}

	private Declarations parameters (){
		Declarations par = new Declarations();
		match(TokenType.LeftParen);
		Variable v;
		Declaration d;
		if(isType()){
			Type t = type();
			v = new Variable(match(TokenType.Identifier));
			d = new Declaration(v, t);
			par.add(d);
			while(token.type().equals(TokenType.Comma)){
				match(TokenType.Comma);
				t = type();
				v = new Variable(match(TokenType.Identifier));
				d = new Declaration(v, t);
				par.add(d);
			}	
		}
		match(TokenType.RightParen);
		return par;


	}
    private Declarations declarations () {
        // Declarations --> { Declaration }
		Declarations ds = new Declarations();
		while(isType()){
			declaration(ds);
		}
        return ds;  // student exercise
    }
  
    private void declaration (Declarations ds) {
        // Declaration  --> Type Identifier { , Identifier } ;
		Declaration d;
		Variable v;
		Type t = type();
		v = new Variable(match(TokenType.Identifier));
		d = new Declaration(v, t);
		ds.add(d);
		while(token.type().equals(TokenType.Comma)){
			match(TokenType.Comma);
			v = new Variable(match(TokenType.Identifier));
			d = new Declaration(v, t);
			ds.add(d);
		}
		match(TokenType.Semicolon);

    }

	private Interface interFace() {
		//Interface --> interface Identifier { Methods } ;
		Variable id;
		Methods ms = new Methods();

		match(TokenType.Interface);
		id = new Variable(match(TokenType.Identifier));
		match(TokenType.LeftBrace);
		//Methods --> {Method}
		while(isType()){
			Method m = method();
			ms.add(m);
		}
		match(TokenType.RightBrace);
		Interface face = new Interface(id, ms);
		return face;				
	}

	private Method method() {
		//Method --> Type Identifier ( parameters ) ;
		Type t = type();
		Variable id = new Variable(match(TokenType.Identifier));
		Declarations par = parameters();
		match(TokenType.Semicolon);
		Method m = new Method(t, id.toString(), par);
		return m;

	}

	private Class classIm() {
		//Class --> class Identifier { Constructor Declarations Functions }
		Variable id;
		Declarations locs = new Declarations();
		Functions fns = new Functions();

		match(TokenType.Class);
		id = new Variable(match(TokenType.Identifier));
		match(TokenType.LeftBrace);

		Variable conId = new Variable(match(TokenType.Identifier));
		Declarations conPar = parameters();
		match(TokenType.LeftBrace);
		Declarations conD = declarations();
		Block conB = statements(conId);
		match(TokenType.RightBrace);
		Constructor c = new Constructor(conId.toString(), conPar, conD, conB);

		while(isType()){
			Variable v;
			Type t = type();
			v = new Variable(match(TokenType.Identifier));
				if(token.type().equals(TokenType.Comma) || token.type().equals(TokenType.Semicolon)){
					Declaration localD;
					Variable localV;
					localV = v;
					localD = new Declaration(localV, t);
					locs.add(localD);
					while(token.type().equals(TokenType.Comma)){
						match(TokenType.Comma);
						localV = new Variable(match(TokenType.Identifier));
						localD = new Declaration(localV, t);
						locs.add(localD);
					}
					match(TokenType.Semicolon);
				}
				//Check for functions
				else{
					Declarations par = parameters();
					match(TokenType.LeftBrace);
					Declarations d = declarations();
					Block b = statements(v);
					match(TokenType.RightBrace);
					Function f = new Function(t, v.toString(), par, d, b);
					fns.add(f);
			}
		}


		match(TokenType.RightBrace);


		Class cls = new Class(id, c, locs, fns);
		return cls;
	}
  
    private Type type () {
        // Type  -->  int | bool | float | char | Identifier
		Type t = null;
		if(token.type().equals(TokenType.Int)){
			match(TokenType.Int);
			t = Type.INT;
		}
		else if(token.type().equals(TokenType.Bool)){
			match(TokenType.Bool);
			t = Type.BOOL;
		}
		else if(token.type().equals(TokenType.Float)){
			match(TokenType.Float);
			t = Type.FLOAT;
		}
		else if(token.type().equals(TokenType.Char)){
			match(TokenType.Char);
			t = Type.CHAR;
		}
		else if(token.type().equals(TokenType.Void)){
			match(TokenType.Void);
			t = Type.VOID;
		}
		else if(token.type().equals(TokenType.Identifier)){
			Variable i = new Variable(match(TokenType.Identifier));
			t = new iType(i.toString());
		}
		else{
			error("Type");
		}
        return t;          
    }
  
    private Statement statement(Variable functName) {
        // Statement --> ; | Block | Assignment | IfStatement | WhileStatement | CallStatement | MethodStatement
        Statement s = null;
		if(token.type().equals(TokenType.Identifier)){
			Variable v = new Variable(match(TokenType.Identifier));
			if(token.type().equals(TokenType.Assign)){
				s = assignment(v);
			}
			else{
				s = callStatement(v);
			}
		}
		else if(token.type().equals(TokenType.LeftBrace)){
			match(TokenType.LeftBrace);
			s = statements(functName);
			match(TokenType.RightBrace);
		}
		else if(token.type().equals(TokenType.Semicolon)){
			s = new Skip();
		}
		else if(token.type().equals(TokenType.If)){
			s = ifStatement(functName);
		}
		else if(token.type().equals(TokenType.While)){
			s = whileStatement(functName);
		}
		else if(token.type().equals(TokenType.Return)){
			s = returnStatement(functName);
		}	
		else{
			error("not implemented yet");
		}
        // student exercise
        return s;
    }
  
    private Block statements (Variable functName) {
        // Block --> '{' Statements '}'
        Block b = new Block();
		Statement s = null;
		//match(TokenType.LeftBrace);
		while(token.type().equals(TokenType.Semicolon) || token.type().equals(TokenType.Identifier) || token.type().equals(TokenType.LeftBrace) || token.type().equals(TokenType.If) || token.type().equals(TokenType.While) || token.type().equals(TokenType.Return)){
			s = statement(functName);
			b.members.add(s);
		}
		//match(TokenType.RightBrace);
        // student exercise
        return b;
    }
  
    private Assignment assignment (Variable input) {
        // Assignment --> Identifier = Expression ;
		Variable target = input;
		match(TokenType.Assign);
		Expression source = expression();
		match(TokenType.Semicolon);
        return new Assignment(target, source);  // student exercise
    }
  
    private Conditional ifStatement (Variable functName) {
        // IfStatement --> if ( Expression ) Statement [ else Statement ]
		match(TokenType.If);
		match(TokenType.LeftParen);
		Expression test = expression();
		match(TokenType.RightParen);
		Statement thenbranch = statement(functName);
		if(token.type().equals(TokenType.Else)){
			match(TokenType.Else);
			Statement elsebranch = statement(functName);
			return new Conditional(test, thenbranch, elsebranch);
		}
		else{
			return new Conditional(test, thenbranch);
		}
        //return null;  // student exercise
    }
  
    private Loop whileStatement (Variable functName) {
        // WhileStatement --> while ( Expression ) Statement
		match(TokenType.While);
		match(TokenType.LeftParen);
		Expression test = expression();
		match(TokenType.RightParen);
		Statement body = statement(functName);
        return new Loop(test, body);  // student exercise
    }

	private Call callStatement(Variable input) {
		Variable id = input;
		Expressions args = new Expressions();
		//First Check if the callStatement belongs to an object
		if(token.type().equals(TokenType.Period)){
			match(TokenType.Period);
			Variable newid = new Variable(match(TokenType.Identifier));
			match(TokenType.LeftParen);
			if(token.type().equals(TokenType.RightParen)){
				match(TokenType.RightParen);
				match(TokenType.Semicolon);
				return new MCall(newid.toString(), args, id.toString());
			}
			else{
				arguments(args);
				match(TokenType.RightParen);
				match(TokenType.Semicolon);
				return new MCall(newid.toString(), args, id.toString());
			}

		}
		else{
			//Otherwise Just make a regular call statement
			match(TokenType.LeftParen);
			if(token.type().equals(TokenType.RightParen)){
				match(TokenType.RightParen);
				match(TokenType.Semicolon);
				return new Call(id.toString(), args);
			}
			else{
				arguments(args);
				match(TokenType.RightParen);
				match(TokenType.Semicolon);
				return new Call(id.toString(), args);
			}
		}
	}

	private void arguments(Expressions args){
		Expression e = expression();
		args.add(e);
		while(token.type().equals(TokenType.Comma)){
			match(TokenType.Comma);
			e = expression();
			args.add(e);
		}
	}

	private Return returnStatement(Variable functName){
		match(TokenType.Return);
		Expression e = expression();
		Variable v = functName;
		match(TokenType.Semicolon);
		return new Return(v, e);
	}

    private Expression expression () {
        // Expression --> Conjunction { || Conjunction }
		Expression e = conjunction();
		while(token.type().equals(TokenType.Or)){
			Operator op = new Operator(match(TokenType.Or));
			Expression con2 = conjunction();
			e = new Binary(op, e, con2);
		}
        return e;  // student exercise
    }
  
    private Expression conjunction () {
        // Conjunction --> Equality { && Equality }
		Expression e = equality();
		while(token.type().equals(TokenType.And)){
			Operator op = new Operator(match(TokenType.And));
			Expression equ2 = equality();
			e = new Binary(op, e, equ2);
		}
        return e;  // student exercise
    }
  
    private Expression equality () {
        // Equality --> Relation [ EquOp Relation ]
		Expression e = relation();
		if(isEqualityOp()){
			Operator op = new Operator(match(token.type()));
			Expression rel2 = relation();
			e = new Binary(op, e, rel2);
		}
        return e;  // student exercise
    }

    private Expression relation (){
        // Relation --> Addition [RelOp Addition]
		Expression e = addition();
	    if(isRelationalOp()){
			Operator op = new Operator(match(token.type()));
			Expression add2 = addition();
			e = new Binary(op, e, add2);
		}	
        return e;  // student exercise
    }
  
    private Expression addition () {
        // Addition --> Term { AddOp Term }
        Expression e = term();
        while (isAddOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = term();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression term () {
        // Term --> Factor { MultiplyOp Factor }
        Expression e = factor();
        while (isMultiplyOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = factor();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression factor() {
        // Factor --> [ UnaryOp ] Primary 
        if (isUnaryOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term = primary();
            return new Unary(op, term);
        }
        else return primary();
    }
  
    private Expression primary () {
        // Primary --> Identifier | Literal | ( Expression )
        //             | Type ( Expression ) | CallE | MethodCallE
        Expression e = null;
        if (token.type().equals(TokenType.Identifier)) {
			Variable v = new Variable(match(TokenType.Identifier));
			//Check For Call Expression
			if(token.type().equals(TokenType.LeftParen)){
				Expressions args = new Expressions();
				match(TokenType.LeftParen);
				if(token.type().equals(TokenType.RightParen)){
					match(TokenType.RightParen);
					e = new CallE(v.toString(), args);
				}		
				else{
					arguments(args);
					match(TokenType.RightParen);
					e = new CallE(v.toString(), args);
				}
			}	
			else{
			//Check if its a method Call or Object Variable
				if(token.type().equals(TokenType.Period)){
					match(TokenType.Period);
					Variable a = new Variable(match(TokenType.Identifier));
					//Method Call
					if(token.type().equals(TokenType.LeftParen)){
						Expressions args = new Expressions();
						match(TokenType.LeftParen);
						if(token.type().equals(TokenType.RightParen)){
							match(TokenType.RightParen);
							e = new MCallE(a.toString(), args, v.toString());
						}		
						else{
							arguments(args);
							match(TokenType.RightParen);
							e = new MCallE(a.toString(), args, v.toString());
						}
					}
					else{
						//an Object Variable
						e = new OVariable(a.toString(), v.toString());
					}
				}
				else{
					//Otherwise just a normal variable
					e = v;
				}
			}
        } else if (isLiteral()) {
            e = literal();
        } else if (token.type().equals(TokenType.LeftParen)) {
            token = lexer.next();
            e = expression();       
            match(TokenType.RightParen);
        } else if (isType( )) {
            Operator op = new Operator(match(token.type()));
            match(TokenType.LeftParen);
            Expression term = expression();
            match(TokenType.RightParen);
            e = new Unary(op, term);
        } else error("Identifier | Literal | ( | Type");
        return e;
    }

    private Value literal( ) {
		Value v;
		
		if (token.type().equals(TokenType.IntLiteral)){
			//match(TokenType.IntLiteral);
			int myVal = Integer.parseInt(token.value());
			v = new IntValue(myVal);
	  	}
		else if(token.type().equals(TokenType.FloatLiteral)){
			float myVal = Float.parseFloat(token.value());
			v = new FloatValue(myVal);
		}
		else if(token.type().equals(TokenType.True) || token.type().equals(TokenType.False)){
			boolean myVal = Boolean.parseBoolean(token.value());
			v = new BoolValue(myVal);
		}
		else if(token.type().equals(TokenType.CharLiteral)){
		char myVal = token.value().charAt(0);
		v = new CharValue(myVal);
		}
		else{
			error("Literal");
			v = null;
		}
	    token = lexer.next();	// student exercise
		return v;
    }
  

    private boolean isAddOp( ) {
        return token.type().equals(TokenType.Plus) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isMultiplyOp( ) {
        return token.type().equals(TokenType.Multiply) ||
               token.type().equals(TokenType.Divide);
    }
    
    private boolean isUnaryOp( ) {
        return token.type().equals(TokenType.Not) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isEqualityOp( ) {
        return token.type().equals(TokenType.Equals) ||
            token.type().equals(TokenType.NotEqual);
    }
    
    private boolean isRelationalOp( ) {
        return token.type().equals(TokenType.Less) ||
               token.type().equals(TokenType.LessEqual) || 
               token.type().equals(TokenType.Greater) ||
               token.type().equals(TokenType.GreaterEqual);
    }
    
    private boolean isType( ) {
		//Here You check if an Identifier is a type
		//by seeing if the first letter is capitalized
		//
		//Ideally there would be a diference between
		//Identifier tokens that start with upper and
		//lowercase letters defined in the Lexer
		//I didn't manage to get to that point yet
		if(token.type().equals(TokenType.Identifier)){
			Variable v = new Variable(token.value());
			if(Character.isUpperCase(v.toString().charAt(0)))
				return true;
		}
        return token.type().equals(TokenType.Int)
            || token.type().equals(TokenType.Bool) 
            || token.type().equals(TokenType.Float)
            || token.type().equals(TokenType.Char)
			|| token.type().equals(TokenType.Void);
    }
    
    private boolean isLiteral( ) {
        return token.type().equals(TokenType.IntLiteral) ||
            isBooleanLiteral() ||
            token.type().equals(TokenType.FloatLiteral) ||
            token.type().equals(TokenType.CharLiteral);
    }
    
    private boolean isBooleanLiteral( ) {
        return token.type().equals(TokenType.True) ||
            token.type().equals(TokenType.False);
    }
    
    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display();           // display abstract syntax tree
    } //main

} // Parser
