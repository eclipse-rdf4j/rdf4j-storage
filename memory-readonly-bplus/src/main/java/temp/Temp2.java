package temp;

import org.eclipse.rdf4j.sail.memory_readonly_bplus.Temp;

public class Temp2 extends Temp {
	public Temp2(String s) {
		super(s);
	}
}

class Temp3 {
	Temp2 t = new Temp2("s");
}