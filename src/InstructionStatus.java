import java.util.ArrayList;

public class InstructionStatus {
	
	ArrayList<int []> table;
	int clockCycles;

	public InstructionStatus() {
		
		this.table = new ArrayList<>();
		clockCycles=0;
	}
	public void flushInsStatus(){
		
		table.clear();
		
		
	}
	

}
