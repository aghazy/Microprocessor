import java.util.ArrayList;

public class ReservationStations {
	String ResStation [][];
	RegistersStatus regStatus;
	RegistersFile registers;
	ROB rob;
	InstructionStatus insStatus;
	int loadLatency; int storeLatency; int addLatency; int multLatency;
	Memory memory;
	int pcReg;
	ArrayList<Boolean> predictions;
	ArrayList<Integer>branches;
	int numBranches;
	int misPrediction;
	int instructionsNum;
	String[] instructions;
	public ReservationStations(int load, int store, int add, int mult, int loadLatency, int storeLatency, int addLatency, int multLatency,int robSize, int [] delays, int [] S, int L, int [] M, int delayMem, boolean [] policy) {
		int sum = load+store+add+mult;
		ResStation = new String[sum][10];
		regStatus=new RegistersStatus();
		rob = new ROB(robSize);
		registers = new RegistersFile();
		insStatus= new InstructionStatus();
		this.loadLatency=loadLatency;
		this.storeLatency=storeLatency;
		this.addLatency=addLatency;
		this.multLatency=multLatency;
		this.predictions= new ArrayList<>();
		this.branches=new ArrayList<>();
		this.numBranches=0;
		this.misPrediction=0;
		this.instructionsNum=0;
		instructions = new String[4096];
		memory = new Memory(delays, S, L, M, delayMem, policy);
		pcReg = 0;
		int i;
		for(i=0; i<load;i++){
			ResStation[i][0]="LOAD";
			ResStation[i][1]="LOAD"+(i+1);
			ResStation[i][2]="N";
			ResStation[i][3]="";
			ResStation[i][4]="";
			ResStation[i][5]="";
			ResStation[i][6]="";
			ResStation[i][7]="";
			ResStation[i][8]="";
			ResStation[i][9]="";

		}
		for(i=load; i<store+load;i++){

			ResStation[i][0]="STORE";
			ResStation[i][1]="STORE"+(i+1-load);
			ResStation[i][2]="N";
			ResStation[i][3]="";
			ResStation[i][4]="";
			ResStation[i][5]="";
			ResStation[i][6]="";
			ResStation[i][7]="";
			ResStation[i][8]="";
			ResStation[i][9]="";
		}
		for(i=store+load; i<add+store+load;i++){

			ResStation[i][0]="ADD";
			ResStation[i][1]="ADD"+(i+1-load-store);
			ResStation[i][2]="N";
			ResStation[i][3]="";
			ResStation[i][4]="";
			ResStation[i][5]="";
			ResStation[i][6]="";
			ResStation[i][7]="";
			ResStation[i][8]="";
			ResStation[i][9]="";
		}

		for(i=store+load+add; i<sum;i++){

			ResStation[i][0]="MULTD";
			ResStation[i][1]="MULTD"+(i+1-load-store-add);
			ResStation[i][2]="N";
			ResStation[i][3]="";
			ResStation[i][4]="";
			ResStation[i][5]="";
			ResStation[i][6]="";
			ResStation[i][7]="";
			ResStation[i][8]="";
			ResStation[i][9]="";
		}

	}

	public int issue(String instruction){

		String [] ins = instruction.split(" ");
		int found=-1;
		int robEntry=0;
		int robEntry2=0;
		if(ins[0].equalsIgnoreCase("lw")){

			for(int i=0; i<ResStation.length ;i++ ){
				if(ResStation[i][0].equalsIgnoreCase("LOAD") && ResStation[i][2].equalsIgnoreCase("N")){
					found=i;	break;		}
			}
			if(found !=-1){

				int regB = Integer.parseInt(ins[2].charAt(1) + "");
				robEntry = regStatus.regStatus[regB];

				int val = rob.writeToRob("LD", ins[1]);
				if(val !=-1){
					ResStation[found][2]="Y";
					ResStation[found][3]="LD";

					if(robEntry==0)
						ResStation[found][4]=registers.registers[regB]+"";
					else{
						if(rob.readFromRobReady(robEntry-1))
							ResStation[found][4]=""+rob.readFromRob(robEntry-1);
						else
							ResStation[found][6]=""+robEntry;
					}
					ResStation[found][8]=(val+1)+"";
					ResStation[found][9]=ins[3];
					regStatus.writeToRegStatus(Integer.parseInt(ins[1].charAt(1)+""), val+1);
					int [] x= {1,found,loadLatency+1,val+1};
					insStatus.table.add(x);
				}
				else return -1;
			}	


		}
		else if(ins[0].equalsIgnoreCase("sw")){

			for(int i=0; i<ResStation.length ;i++ ){
				if(ResStation[i][0].equalsIgnoreCase("STORE") && ResStation[i][2].equalsIgnoreCase("N")){
					found=i;	break;		}
			}
			if(found !=-1){
				int regA = Integer.parseInt(ins[1].charAt(1)+"");
				int regB = Integer.parseInt(ins[2].charAt(1) + "");
				robEntry = regStatus.regStatus[regB];
				robEntry2 = regStatus.regStatus[regA];
				//if(robEntry==0){
				int val = rob.writeToRob("SW", "MEM");
				if(val !=-1){
					ResStation[found][2]="Y";
					ResStation[found][3]="SW";
					if(robEntry2 == 0)
						ResStation[found][4]=registers.registers[regA]+"";
					else{
						if(rob.readFromRobReady(robEntry2-1))
							ResStation[found][4]=""+rob.readFromRob(robEntry2-1);
						else
							ResStation[found][6]=""+robEntry2;
					}
					if(robEntry == 0)
						ResStation[found][5]=registers.registers[regB]+"";
					else{
						if(rob.readFromRobReady(robEntry-1))
							ResStation[found][5]=""+rob.readFromRob(robEntry-1);
						else
							ResStation[found][7]=""+robEntry;
					}
					ResStation[found][8]=(val+1)+"";
					ResStation[found][9]=ins[3];
					int [] x= {1,found,storeLatency+1,val+1};
					insStatus.table.add(x);
				}
				else return -1;
			}	


		}
		else if(ins[0].equalsIgnoreCase("mul")){
			for(int i=0; i<ResStation.length ;i++ ){
				if(ResStation[i][0].equalsIgnoreCase("MULTD") && ResStation[i][2].equalsIgnoreCase("N")){
					found=i;	break;		}
			}
			if(found !=-1){
				//int regA = Integer.parseInt(ins[1].charAt(1)+"");
				int regB = Integer.parseInt(ins[2].charAt(1) + "");
				int regC = Integer.parseInt(ins[3].charAt(1) + "");
				robEntry = regStatus.regStatus[regB];
				robEntry2 = regStatus.regStatus[regC];
				//if(robEntry==0){
				int val = rob.writeToRob("MUL", ins[1]);
				if(val !=-1){
					ResStation[found][2]="Y";
					ResStation[found][3]="MUL";
					if(robEntry == 0)
						ResStation[found][4]=registers.registers[regB]+"";
					else{
						if(rob.readFromRobReady(robEntry-1))
							ResStation[found][4]=""+rob.readFromRob(robEntry-1);
						else
							ResStation[found][6]=""+robEntry;
					}
					if(robEntry2 == 0)
						ResStation[found][5]=registers.registers[regC]+"";
					else{
						if(rob.readFromRobReady(robEntry2-1))
							ResStation[found][5]=""+rob.readFromRob(robEntry2-1);
						else
							ResStation[found][7]=""+robEntry2;
					}
					ResStation[found][8]=(val+1)+"";
					regStatus.writeToRegStatus(Integer.parseInt(ins[1].charAt(1)+""), val+1);
					int [] x= {1,found,multLatency+1,val+1};
					insStatus.table.add(x);
				}
				else return -1;
			}	


		}
		else{
			if(ins[0].equalsIgnoreCase("add") || ins[0].equalsIgnoreCase("sub") || ins[0].equalsIgnoreCase("nand")){
				for(int i=0; i<ResStation.length ;i++ ){
					if(ResStation[i][0].equalsIgnoreCase("ADD") && ResStation[i][2].equalsIgnoreCase("N")){
						found=i;	break;		}
				}

				if(found !=-1){
					//int regA = Integer.parseInt(ins[1].charAt(1)+"");
					int regB = Integer.parseInt(ins[2].charAt(1) + "");
					int regC = Integer.parseInt(ins[3].charAt(1) + "");
					robEntry = regStatus.regStatus[regB];
					robEntry2 = regStatus.regStatus[regC];
					//if(robEntry==0){
					int val = rob.writeToRob(ins[0], ins[1]);
					if(val !=-1){
						ResStation[found][2]="Y";
						ResStation[found][3]=ins[0];
						if(robEntry == 0)
							ResStation[found][4]=registers.registers[regB]+"";
						else{
							if(rob.readFromRobReady(robEntry-1))
								ResStation[found][4]=""+rob.readFromRob(robEntry-1);
							else
								ResStation[found][6]=""+robEntry;
						}
						if(robEntry2 == 0)
							ResStation[found][5]=registers.registers[regC]+"";
						else{
							if(rob.readFromRobReady(robEntry2-1))
								ResStation[found][5]=""+rob.readFromRob(robEntry2-1);
							else
								ResStation[found][7]=""+robEntry2;
						}
						ResStation[found][8]=(val+1)+"";
						regStatus.writeToRegStatus(Integer.parseInt(ins[1].charAt(1)+""), val+1);
						int [] x= {1,found,addLatency+1,val+1};
						insStatus.table.add(x);
					}
					else return -1;
				}	
			}
			else{
				if(ins[0].equalsIgnoreCase("beq")){
					numBranches++;
					for(int i=0; i<ResStation.length ;i++ ){
						if(ResStation[i][0].equalsIgnoreCase("ADD") && ResStation[i][2].equalsIgnoreCase("N")){
							found=i;	break;		}
					}

					if(found !=-1){
						//int regA = Integer.parseInt(ins[1].charAt(1)+"");
						int regB = Integer.parseInt(ins[1].charAt(1) + "");
						int regC = Integer.parseInt(ins[2].charAt(1) + "");
						robEntry = regStatus.regStatus[regB];
						robEntry2 = regStatus.regStatus[regC];
						//if(robEntry==0){
						int val = rob.writeToRob("beq", "");
						if(val !=-1){
							ResStation[found][2]="Y";
							ResStation[found][3]=ins[0];
							if(robEntry == 0)
								ResStation[found][4]=registers.registers[regB]+"";
							else{
								if(rob.readFromRobReady(robEntry-1))
									ResStation[found][4]=""+rob.readFromRob(robEntry-1);
								else
									ResStation[found][6]=""+robEntry;
							}
							if(robEntry2 == 0)
								ResStation[found][5]=registers.registers[regC]+"";
							else{
								if(rob.readFromRobReady(robEntry2-1))
									ResStation[found][5]=""+rob.readFromRob(robEntry2-1);
								else
									ResStation[found][7]=""+robEntry2;
							}
							ResStation[found][8]=(val+1)+"";
							regStatus.writeToRegStatus(Integer.parseInt(ins[1].charAt(1)+""), val+1);
							int [] x= {1,found,addLatency+1,val+1};
							insStatus.table.add(x);
							if(Integer.parseInt(ins[3])>0){
								branches.add(Integer.parseInt(ins[3])+pcReg+1);
								predictions.add(false);
							}
							else {
								predictions.add(true);
								pcReg=pcReg+Integer.parseInt(ins[3]);
								branches.add(pcReg+1);
							}

						}
						else return -1;
					}	
				}

				else{
					if(ins[0].equalsIgnoreCase("addi")){
						for(int i=0; i<ResStation.length ;i++ ){
							if(ResStation[i][0].equalsIgnoreCase("ADD") && ResStation[i][2].equalsIgnoreCase("N")){
								found=i;	break;		}
						}
						if(found !=-1){
							//int regA = Integer.parseInt(ins[1].charAt(1)+"");
							int regB = Integer.parseInt(ins[2].charAt(1) + "");
							robEntry = regStatus.regStatus[regB];
							//if(robEntry==0){
							int val = rob.writeToRob(ins[0], ins[1]);
							if(val !=-1){
								ResStation[found][2]="Y";
								ResStation[found][3]=ins[0];
								if(robEntry == 0)
									ResStation[found][4]=registers.registers[regB]+"";
								else{
									if(rob.readFromRobReady(robEntry-1))
										ResStation[found][4]=""+rob.readFromRob(robEntry-1);
									else
										ResStation[found][6]=""+robEntry;
								}

								ResStation[found][5]=ins[3];

								ResStation[found][8]=(val+1)+"";
								regStatus.writeToRegStatus(Integer.parseInt(ins[1].charAt(1)+""), val+1);
								int [] x= {1,found,addLatency+1,val+1};
								insStatus.table.add(x);
							}
							else return -1;
						}	
					}
				}
			}
		}
		return found;	}


	public void Execute(){
		for(int i=0 ; i<insStatus.table.size(); i++){
			if(insStatus.table.get(i)[0]==1){
				int func = insStatus.table.get(i)[1];
				String [] funcStat = ResStation[func];
				if(funcStat[6].equals("") && funcStat[7].equals("") && insStatus.table.get(i)[2]!=0)
					insStatus.table.get(i)[2]--;
				else{
					if((!funcStat[6].equals("")) && rob.readFromRobReady(Integer.parseInt(funcStat[6]) -1)){
						ResStation[func][4]=""+rob.readFromRob(Integer.parseInt(funcStat[6]) -1);
						ResStation[func][6] = "";
					}
					if((!funcStat[7].equals("")) && rob.readFromRobReady(Integer.parseInt(funcStat[7]) -1)){
						ResStation[func][5]=""+rob.readFromRob(Integer.parseInt(funcStat[7]) -1);
						ResStation[func][7] = "";
					}
				}
				if(funcStat[3].equalsIgnoreCase("LD") && insStatus.table.get(i)[2] == loadLatency && !funcStat[4].equals(""))
					funcStat[9]=""+(Integer.parseInt(funcStat[4])+Integer.parseInt(funcStat[9]));
				if(funcStat[3].equalsIgnoreCase("SW") && insStatus.table.get(i)[2] == storeLatency && !funcStat[5].equals(""))
					funcStat[9]=""+(Integer.parseInt(funcStat[5])+Integer.parseInt(funcStat[9]));
				if(insStatus.table.get(i)[2]==0){

					insStatus.table.get(i)[0]=2;
					insStatus.table.get(i)[2]=1;


				}




			}


		}




	}

	public void Write() {

		boolean wrote = false;

		for(int i=0 ; i<insStatus.table.size(); i++){
				if(insStatus.table.get(i)[0]==2){
					if (insStatus.table.get(i)[2] == 1) 
						insStatus.table.get(i)[2]--;
					else {if (!wrote){
						wrote = true;
						int func = insStatus.table.get(i)[1];
						String [] funcStat = ResStation[func];
						insStatus.table.get(i)[0]=3;
						insStatus.table.get(i)[2]=1;
						int val = 0;
						if(funcStat[3].equalsIgnoreCase("beq")){
							val = Integer.parseInt(funcStat[4]) - Integer.parseInt(funcStat[5]);
						}
						if(funcStat[3].equalsIgnoreCase("LD")){
							int loadDelay = memory.delayCounter;
							val = memory.read(Integer.parseInt(funcStat[9]));
							loadDelay = memory.delayCounter-loadDelay;
							insStatus.table.get(i)[2] = loadDelay;
						}
						if(funcStat[3].equalsIgnoreCase("SW")){
							val = Integer.parseInt(funcStat[4]);
							rob.rob[insStatus.table.get(i)[3]-1][1] = funcStat[9];
						}


						if(funcStat[3].equalsIgnoreCase("ADD") || funcStat[3].equalsIgnoreCase("ADDI") ){
							val = Integer.parseInt(funcStat[4]) + Integer.parseInt(funcStat[5]);
						}
						if(funcStat[3].equalsIgnoreCase("SUB")){
							val = Integer.parseInt(funcStat[4]) - Integer.parseInt(funcStat[5]);
						}
						if(funcStat[3].equalsIgnoreCase("NAND")){
							val = ~(Integer.parseInt(funcStat[4]) & Integer.parseInt(funcStat[5]));
						}

						if(funcStat[3].equalsIgnoreCase("MUL")){
							val = Integer.parseInt(funcStat[4]) * Integer.parseInt(funcStat[5]);
						}

						int dest = Integer.parseInt(funcStat[8]);
						rob.updateRob(dest-1, val);
						ResStation[func][2] = "N";
						ResStation[func][3] = "";
						ResStation[func][4] = "";
						ResStation[func][5] = "";
						ResStation[func][6] = "";
						ResStation[func][7] = "";
						ResStation[func][8] = "";
						ResStation[func][9] = "";
					}


				}

			}




		}
	}

	public void commit(){

		boolean commited = false;

		for(int i=0 ; i<insStatus.table.size(); i++){
			if(insStatus.table.get(i)[0]==3){
				if (insStatus.table.get(i)[2] >= 1) 
					insStatus.table.get(i)[2]--;
				else{
					if(!commited){
						if(rob.rob[insStatus.table.get(i)[3]-1][0].equalsIgnoreCase("beq")){
							int branch = Integer.parseInt(rob.rob[insStatus.table.get(i)[3]-1][2]);
							
							//if(rob.commitRob(insStatus.table.get(i)[3]-1)){
								commited = true;
								instructionsNum++;
								if((branch==0 && !predictions.get(0)) ||(branch!=0 && predictions.get(0)) ){
									insStatus.flushInsStatus();
									this.flushResStation();
									regStatus.flushRegStatus();
									for(int k=0; k<rob.rob.length; k++)
										if(rob.rob[k][0].equalsIgnoreCase("beq"))
											numBranches--;
									rob.flushRob();
									pcReg=branches.get(0);
									misPrediction++;
									predictions.clear();
									branches.clear();
									for(int f=0; f<instructions.length; f++)
										instructions[f] = "";
								}
								else{
								predictions.remove(0);
								branches.remove(0);
								}
							//}
						}
						else{
							if(rob.rob[insStatus.table.get(i)[3]-1][0].equalsIgnoreCase("sw")){
								int loadDelay = memory.delayCounter;
								memory.write(Integer.parseInt(rob.rob[insStatus.table.get(i)[3]-1][1]), Integer.parseInt(rob.rob[insStatus.table.get(i)[3]-1][2]));
								loadDelay = memory.delayCounter-loadDelay;
								insStatus.table.get(i)[0]=5;
								if(rob.commitRob(insStatus.table.get(i)[3]-1)){
									commited = true;
									instructionsNum++;
								}
							}
							else{
								int destReg = Integer.parseInt(""+(rob.rob[insStatus.table.get(i)[3]-1][1]).charAt(1));
								int newVal = Integer.parseInt(rob.rob[insStatus.table.get(i)[3]-1][2]);
								if(rob.commitRob(insStatus.table.get(i)[3]-1)){
									commited = true;

									registers.registers[destReg] = newVal;
									regStatus.removeFromRegStatus(destReg);
									insStatus.table.get(i)[0]=4;
									instructionsNum++;
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void storeLatencyCheck(){
		for(int i=0 ; i<insStatus.table.size(); i++){
			if(insStatus.table.get(i)[0]==5){
				if (insStatus.table.get(i)[2] >= 1) 
					insStatus.table.get(i)[2]--;
				else{
					insStatus.table.get(i)[0]=4;
					instructionsNum++;
				}
			}
		}
	}
	

	public void flushResStation(){

		for (int i = 0; i < ResStation.length; i++) {
			ResStation[i][2]="N";
			ResStation[i][3]="";
			ResStation[i][4]="";
			ResStation[i][5]="";
			ResStation[i][6]="";
			ResStation[i][7]="";
			ResStation[i][8]="";
			ResStation[i][9]="";
		}

	}

}



