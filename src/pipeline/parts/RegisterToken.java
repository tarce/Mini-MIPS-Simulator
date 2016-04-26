package miniMipSim.pipeline.parts;

public class RegisterToken implements Comparable<RegisterToken> {
	public int registerName;
	public int registerValue;
	
	/**
	 * Constructor
	 * 
	 * @param name
	 * @param value
	 */
	public RegisterToken(int name, int value) {
		this.registerName = name;
		this.registerValue = value;
	}
	
	/**
	 * Copies an instruction token into this.
	 * 
	 * @param t - the register token to copy
	 */
	public void copy(RegisterToken t) {
		this.registerName = t.registerName;
		this.registerValue = t.registerValue;
	}
	
	/**
	 * Comparator.
	 */
	public int compareTo(RegisterToken t) {
		return (registerName - t.registerName);
	}
	
	/**
	 * returns a string with the register in assembler.
	 */
	public String toString() {
		return (new StringBuilder("<R").append(registerName).append(",")
				.append(registerValue).append(">")).toString();
	}

}