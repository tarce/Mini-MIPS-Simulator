package miniMipSim.pipeline;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

import miniMipSim.MiniMIPSim.Opcode;
import miniMipSim.pipeline.parts.InstructionToken;
import miniMipSim.pipeline.parts.RegisterToken;

public class ResultBuffer {
	public LinkedList<InstructionToken> temporary;
	public LinkedList<InstructionToken> finalized;
	public LinkedList<RegisterToken> finalized2;
	
	public ResultBuffer() {
		temporary = new LinkedList<InstructionToken>();
		finalized = new LinkedList<InstructionToken>();
		finalized2 = new LinkedList<RegisterToken>();
	}
	
	public boolean isEmpty() {
		return finalized.isEmpty();
	}

	public InstructionToken getTop() {
		
		Collections.sort(finalized);
		
		if (!finalized.isEmpty()) {
			return finalized.getFirst();
		}
		return null;
	}
	
	public boolean removeTop() {
		
		Collections.sort(finalized);
		
		if (!finalized.isEmpty()) {
			finalized.removeFirst();
			
			finalized2.clear();
			
			if (!finalized.isEmpty()) {
				for (InstructionToken t: finalized) {
					finalized2.add(new RegisterToken (t.dest, t.result));
				}
			}

			return true;
		}
		
		return false;
	}
	
	public void sync() {
		
		for(InstructionToken t: temporary) {
			finalized.add(t);
		}
		
		
		for (InstructionToken t: temporary) {
			finalized2.add(new RegisterToken (t.dest, t.result));
		}
		
		temporary.clear();
	}
	
	public void putToken(InstructionToken i) {
		if (i.opcode == Opcode.ADD || i.opcode == Opcode.SUB) {
			
			if(i.opcode == Opcode.ADD) {
				i.result = i.src1 + i.src2;
			}
			if(i.opcode == Opcode.SUB) {
				i.result = i.src1 - i.src2;
			}
		}
		temporary.add(i);
	}
	
	public void printContents(BufferedWriter out) throws IOException {
		
		Collections.sort(finalized2);
		
		out.write("RB:");
		
		if (finalized.isEmpty()) {
			out.write("\n");
			return;
		}

		for (int i = 0; i < finalized2.size() - 1; i++) {
			out.write(finalized2.get(i).toString() + ",");
		}
		out.write(finalized2.get(finalized2.size()-1).toString() + "\n");
		
	}
	
	public void printContents() {
		
		Collections.sort(finalized2);
		
		System.out.print("RB:");
		
		if (finalized2.isEmpty()) {
			System.out.print("\n");
			return;
		}
		
		for (int i = 0; i < finalized2.size() - 1; i++) {
			System.out.print(finalized2.get(i).toString() + ",");
		}
		System.out.print(finalized2.get(finalized2.size()-1).toString() + "\n");
		
	}
}
