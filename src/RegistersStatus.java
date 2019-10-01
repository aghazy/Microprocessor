public class RegistersStatus {
	
	int[]regStatus;

	public RegistersStatus() {
		
		this.regStatus = new int [8];
	}
	
	public void writeToRegStatus(int reg, int index){
		
		regStatus[reg]=index;
		
	}
	
	public void removeFromRegStatus(int reg){
		
		regStatus[reg]=0;
	}
	public void flushRegStatus(){
		
		for(int i=0; i<regStatus.length; i++)
			regStatus[i]=0;
		
	}
	
	public int readFromRegStatus(int reg){
		
		return regStatus[reg];
	}
	
	

}
