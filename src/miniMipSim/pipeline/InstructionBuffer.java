package miniMipSim.pipeline;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

import miniMipSim.pipeline.parts.InstructionToken;

public class InstructionBuffer {
	public LinkedList<InstructionToken> temporary;
	public LinkedList<InstructionToken> finalized;
	
	public InstructionBuffer() {
		temporary = new LinkedList<InstructionToken>();
		finalized = new LinkedList<InstructionToken>();
	}
	
	public boolean isEmpty() {
		return finalized.isEmpty();
	}

	public InstructionToken getTop() {
		if (!finalized.isEmpty()) {
			return finalized.getFirst();
		}
		return null;
	}
	
	public boolean removeTop() {
		if (!finalized.isEmpty()) {
			finalized.removeFirst();
			return true;
		}
		return false;
	}
	
	public void sync() {

		while(!temporary.isEmpty()) {
			finalized.add(temporary.removeFirst());
		}
		temporary.clear();
	}
	
	public void putToken(InstructionToken i) {
		temporary.add(i);
	}
	
	public void printContents(BufferedWriter out) throws IOException {
		
		Collections.sort(finalized);
		
		out.write("IB:");
		
		if (finalized.isEmpty()) {
			out.write("\n");
			return;
		}
		
		for (int i = 0; i < finalized.size() - 1; i++) {
			out.write(finalized.get(i).toString2() + ",");
		}
		out.write(finalized.get(finalized.size()-1).toString2() + "\n");
	}
	
	public void printContents() {
		
		Collections.sort(finalized);
		
		System.out.print("IB:");
		
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