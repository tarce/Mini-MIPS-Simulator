package miniMipSim.pipeline.parts;

import static miniMipSim.MiniMIPSim.Opcode;

public class InstructionToken implements Comparable<InstructionToken>{
	
	public Opcode opcode;
	public int dest;
	public int src1;
	public int src2;
	private int priority;
	public int result;
	
	/**
	 * Constructor
	 * 
	 * @param op - the opcode as supported in the enum in MiniMIPSim
	 * @param dest - the destination register
	 * @param src1 - the first source register
	 * @param src2 - the second source register
	 * @param priority - keeps track of order in which instructions are input into the pipeline
	 */
	public InstructionToken(Opcode op, int dest, int src1, int src2, int priority) {
		this.opcode = op;
		this.dest = dest;
		this.src1 = src1;
		this.src2 = src2;
		this.priority = priority;
		result = 0;
	}
	
	/**
	 * Copies an instruction token into this.
	 * 
	 * @param t - the InstructionToken to copy
	 */
	public void copy(InstructionToken t) {
		this.opcode = t.opcode;
		this.dest = t.dest;
		this.src1 = t.src1;
		this.src2 = t.src2;
	}
	
	/**
	 * Comparator.
	 */
	public int compareTo(InstructionToken t) {
		return (priority - t.priority);
	}
	
	/**
	 * returns a string with the Instruction in assembler.
	 */
	public String toString() {
		return (new StringBuilder("<").append(opcode.toString()).append(",R")
				.append(dest).append(",R").append(src1).append(",R").append(src2).append(">")).toString();
	}
	
	/**
	 * returns a string with the Instruction in assembler.
	 * @return
	 */
	public String toString2() {
		return (new StringBuilder("<").append(opcode.toString()).append(",R")
				.append(dest).append(",").append(src1).append(",").append(src2).append(">")).toString();
	}
	
	/**
	 * returns a string with the Instruction in assembler.
	 * @return
	 */
	public String toString3() {
		return (new StringBuilder("<").append("R")
				.append(dest).append(",").append(result).append(">")).toString();
	}
}

