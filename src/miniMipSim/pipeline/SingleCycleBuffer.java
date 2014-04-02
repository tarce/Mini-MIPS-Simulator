package miniMipSim.pipeline;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

import miniMipSim.MiniMIPSim.Opcode;
import miniMipSim.pipeline.parts.InstructionToken;

public class SingleCycleBuffer {
	public LinkedList<InstructionToken> temporary;
	public LinkedList<InstructionToken> finalized;
	
	/**
	 * Constructor
	 */
	public SingleCycleBuffer() {
		temporary = new LinkedList<InstructionToken>();
		finalized = new LinkedList<InstructionToken>();
	}
	
	/**
	 * Returns whether the buffer is empty.
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return finalized.isEmpty();
	}

	/**
	 * Returns the instruction ready to go on to next stage.
	 * 
	 * @return 
	 */
	public InstructionToken getTop() {
		if (!finalized.isEmpty()) {
			return finalized.getFirst();
		}
		return null;
	}
	
	/**
	 * Removes the instruction that is to be put in the next stage.
	 * Note: call getTop() first.
	 * 
	 * @return
	 */
	public boolean removeTop() {
		if (!finalized.isEmpty()) {
			finalized.removeFirst();
			return true;
		}
		return false;
	}
	
	/**
	 * Synchronizes the two internal buffers for keeping time step.
	 * 
	 */
	public void sync() {

		while(!temporary.isEmpty()) {
			finalized.add(temporary.removeFirst());
		}
		temporary.clear();
	}
	
	/**
	 * Puts and instruction token in the buffer.
	 * 
	 * @param i - the instruction
	 */
	public void putToken(InstructionToken i) {
		if (i.opcode == Opcode.ADD || i.opcode == Opcode.SUB) {
			temporary.add(i);
		}
	}
	
	/**
	 * Writes the contents of the buffer out.
	 * 
	 * @param out - the buffer to be written to
	 * @throws IOException
	 */
	public void printContents(BufferedWriter out) throws IOException {
		
		Collections.sort(finalized);
		
		out.write("SB:");
		
		if (finalized.isEmpty()) {
			out.write("\n");
			return;
		}
		
		for (int i = 0; i < finalized.size() - 1; i++) {
			out.write(finalized.get(i).toString2() + ",");
		}
		out.write(finalized.get(finalized.size()-1).toString2() + "\n");
	}
	
	/**
	 * Prints the contents of the buffer.
	 */
	public void printContents() {
		
		Collections.sort(finalized);
		
		System.out.print("SB:");
		
		if (finalized.isEmpty()) {
			System.out.print("\n");
			return;
		}
		
		for (int i = 0; i < finalized.size() - 1; i++) {
			System.out.print(finalized.get(i).toString2() + ",");
		}
		System.out.print(finalized.get(finalized.size()-1).toString2() + "\n");
	}
}

