package miniMipSim.pipeline.parts;

public class RegisterToken implements Comparable<RegisterToken> {
	public int registerName;
	public int registerValue;
	
	public RegisterToken(int name, int value) {
		this.registerName = name;
		this.registerValue = value;
	}
	
	public void copy(RegisterToken t) {
		this.registerName = t.registerName;
		this.registerValue = t.registerValue;
	}
	
	public int compareTo(RegisterToken t) {
		return (registerName - t.registerName);
	}
	
	public String toString() {
		return (new StringBuilder("<R").append(registerName).append(",")
				.append(registerValue).append(">")).toString();
	}

}