import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

	public static void main(String[] args) throws IOException {
		BufferedReader buf=new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Please specify the number of cache levels");
		int cacheLevels = Integer.parseInt(buf.readLine());
		int[]delay=new int[cacheLevels];
		int[]s=new int[cacheLevels];
		int[]m=new int[cacheLevels];
		boolean[]policy=new boolean[cacheLevels];
		int delayMem=0;
		int lsingle;
		int pipelineWidth;
		int loadStations;
		int storeStations;
		int addStations;
		int multStations;
		int loadLatency;
		int storeLatency;
		int addLatency;
		int multLatency;
		int robEntries;
		int clockCycles = 0;
		System.out.println("specify the blocksize");
		lsingle=Integer.parseInt(buf.readLine());
		for(int i =0; i<cacheLevels;i++){
			System.out.println("for cache level L"+(i+1)+" specify the delay");
			delay[i]=Integer.parseInt(buf.readLine());
			System.out.println("specify the size");
			s[i]=Integer.parseInt(buf.readLine());
			System.out.println("specify the Associativity");
			m[i]=Integer.parseInt(buf.readLine());
			System.out.println("specify the writing policy");
			policy[i]=Boolean.parseBoolean(buf.readLine());
		}
		System.out.println("specify the main memory access time");
		delayMem=Integer.parseInt(buf.readLine());

		System.out.println("specify the pipeline width");
		pipelineWidth=Integer.parseInt(buf.readLine());

		System.out.println("specify the number of load reservation stations");
		loadStations=Integer.parseInt(buf.readLine());

		System.out.println("specify the number of store reservation stations");
		storeStations=Integer.parseInt(buf.readLine());

		System.out.println("specify the number of add reservation stations");
		addStations=Integer.parseInt(buf.readLine());

		System.out.println("specify the number of mult reservation stations");
		multStations=Integer.parseInt(buf.readLine());


		//System.out.println("specify the load latency");
		loadLatency=1;

		//System.out.println("specify the store latency");
		storeLatency=1;

		System.out.println("specify the add latency");
		addLatency=Integer.parseInt(buf.readLine());

		System.out.println("specify the mult latency");
		multLatency=Integer.parseInt(buf.readLine());

		System.out.println("specify the number of rob entries");
		robEntries=Integer.parseInt(buf.readLine());
		String [] insMem = new String[4096];
		int tempPC =0;
		ReservationStations reservationStation = new ReservationStations(loadStations, storeStations, addStations, multStations, loadLatency, storeLatency, addLatency, multLatency, robEntries, delay, s, lsingle, m, delayMem, policy);
		System.out.println("Specify where the assembly program will start in the memory");
			reservationStation.pcReg = Integer.parseInt(buf.readLine());
			tempPC = reservationStation.pcReg;
		System.out.println("Please enter the assembly program use only spaces between inputs of the same instruction and lines between every 2 instructions. Type done to end the program");
		String userInput = buf.readLine();
		while(!userInput.equalsIgnoreCase("done")){
			insMem[tempPC] = userInput;
			tempPC++;
			userInput = buf.readLine();
		}
		System.out.println("Please enter the data in mem required write position then space then value ex. 5 2 this will store value 2 in position 5. Type done to stop entering data");
		String userData = buf.readLine();
		while(!userData.equalsIgnoreCase("done")){
			String [] input = userData.split(" ");
			reservationStation.memory.mem.data[Integer.parseInt(input[0])] = Integer.parseInt(input[1]);
			userData = buf.readLine();
		}
		// To adjust the initial values in the register file
		
		//reservationStation.registers.registers[1] = 5;
				
		String[] instructions= new String[4096]; //zabatha
		//String[] insBuffer=new String[instructions.length];	
		int insBufferTail=0;
		int insBufferHead=0;
		boolean done = false;
		for (int i = 0; i < pipelineWidth; i++) {
			instructions[insBufferTail] = insMem[reservationStation.pcReg];
			insBufferTail++;
			reservationStation.pcReg++;
		}
		while(!done) {
			for(int i=0; i<pipelineWidth && instructions[insBufferHead] != null && !instructions[insBufferHead].equals(""); i++){
				String ins = instructions[insBufferHead].split(" ")[0];
				if(ins.equalsIgnoreCase("JMP") || ins.equalsIgnoreCase("JALR") || ins.equalsIgnoreCase("RET")){
					if(ins.equalsIgnoreCase("JMP"))
						reservationStation.pcReg = reservationStation.pcReg+Integer.parseInt(instructions[insBufferHead].split(" ")[1].charAt(1)+"") + Integer.parseInt(instructions[insBufferHead].split(" ")[2]);
					if(ins.equalsIgnoreCase("JALR")){
						reservationStation.registers.registers[Integer.parseInt(""+instructions[insBufferHead].split(" ")[1].charAt(1))] = reservationStation.pcReg;
						reservationStation.pcReg = reservationStation.registers.registers[Integer.parseInt(instructions[insBufferHead].split(" ")[2].charAt(1)+"")];
					}
					if(ins.equalsIgnoreCase("RET"))
						reservationStation.pcReg = reservationStation.registers.registers[Integer.parseInt(""+instructions[insBufferHead].split(" ")[1].charAt(1))];
					reservationStation.instructionsNum++;
					insBufferHead++;
				}
				else if(ins.equalsIgnoreCase("BEQ")){
					if(reservationStation.issue(instructions[insBufferHead]) != -1)
						insBufferHead++;
					//break;
				}
				else {
					if(reservationStation.issue(instructions[insBufferHead]) != -1){
						insBufferHead++;
					}
					else {
						//reservationStation.pcReg++;
						break;
					}
					
				}
				instructions[insBufferTail] = insMem[reservationStation.pcReg];
				insBufferTail++;
				reservationStation.pcReg++;
			}

			reservationStation.Execute();
			reservationStation.Write();
			reservationStation.commit();
			reservationStation.storeLatencyCheck();

			boolean check = true;

			for(int j=0; j < reservationStation.insStatus.table.size() && check; j++) {


				if(reservationStation.insStatus.table.get(j) != null && reservationStation.insStatus.table.get(j)[0] != 4) {

					check = false;
				}
			}

			clockCycles++;
			done = check && (instructions[insBufferHead] == null || instructions[insBufferHead].equals(""));

		}
		System.out.println("-----------------------------------------------------------------------------");
		System.out.println("Number of Clock Cycles: " + (clockCycles+1 + reservationStation.misPrediction)); // Add memory clock cycles +1 for the first fetch
		System.out.println("Number of Instructions: "+reservationStation.instructionsNum);
		System.out.println("IPC: "+((reservationStation.instructionsNum*1.0)/((clockCycles+1)*1.0)));
		if(reservationStation.numBranches == 0)
			System.out.println("Branch Misprediction percentage: 0%");
		else
			System.out.println("Branch Misprediction percentage: "+(reservationStation.misPrediction*1.0/(reservationStation.numBranches*1.0))+"%");
		for(int i = 0; i<reservationStation.memory.lev.length; i++){
			if(reservationStation.memory.lev[i].ins == 0)
				System.out.println("Hit rate of cache level "+(i+1)+":0%");
			else
				System.out.println("Hit rate of cache level "+(i+1)+": "+((reservationStation.memory.lev[i].hits*1.0)/(reservationStation.memory.lev[i].ins*1.0)) +"%");
		}
		System.out.println("Global AMAT: "+getAMAT(0, reservationStation));
		System.out.println("Register File");
		System.out.println("R1: "+reservationStation.registers.registers[1]);
		System.out.println("R2: "+reservationStation.registers.registers[2]);
		System.out.println("R3: "+reservationStation.registers.registers[3]);
		System.out.println("R4: "+reservationStation.registers.registers[4]);
		System.out.println("R5: "+reservationStation.registers.registers[5]);
		System.out.println("R6: "+reservationStation.registers.registers[6]);
		System.out.println("R7: "+reservationStation.registers.registers[7]);
		System.out.println("-----------------------------------------------------------------------------");
	}
	public static double getAMAT(int level, ReservationStations r){
		if(level == r.memory.lev.length-1)
			return r.memory.lev[level].delay + (1-getHitRate(r.memory.lev[level]))*r.memory.mem.delay;
		return r.memory.lev[level].delay + (1-getHitRate(r.memory.lev[level]))*getAMAT(level+1, r);
	}
	public static double getHitRate(Level lev){
		if(lev.getHits() == 0 && lev.getMisses() == 0)
			return 1;
		return (lev.hits*1.0)/(lev.ins*1.0);
	}
}
