import java.util.*;

class State{

	Enviroment delta;

	Memory mu;

	int a;

	public State(int n){
		delta = new Enviroment();
		mu = new Memory();
		for(int i = 0; i <= n; i ++){
			mu.put(i, "unused");
		}
		a = 0;

	}

	public State(Enviroment d, Memory m, int inUse){
		delta = d;
		mu = m;
		a = inUse;

	}

	Enviroment getEnv(){
		return delta;
	}

	Memory getMem(){
		return mu;
	}

	int  getUsed(){
		return a;
	}	

}

class Enviroment extends HashMap<Integer, Variable> {

}

class Memory extends HashMap<Integer, String>{

}
