public class ROB {
	int head;
	int tail;
	boolean empty;
	String rob[][];
	
	public ROB(int size) {
		super();
		this.head = 0;
		this.tail = 0;
		this.rob = new String [size][4];
		this.empty=true;
	}
   public void incrementHead(){
	   
	   head+=1;
	   head=head%rob.length;  
   }
   
   public void incrementTail(){
	   
	   tail+=1;
	   tail=tail%rob.length;  
   }
   public boolean robFull(){
	 if(!empty && head==tail) return true;
	 return false;  
   }
   public int writeToRob(String type, String dest){
	if(robFull())return -1;
	else{
	empty=false;
	rob[tail][0]=type;
	rob[tail][1]=dest;
	rob[tail][2]="";
	rob[tail][3]="N";
	int oldTail = tail;
	incrementTail();
	return oldTail;}
   }
   
   public void updateRob(int index, int value){
	   
		rob[index][2]=""+value;
		rob[index][3]="Y";	   
	   }
   public boolean commitRob(int index){
	   if(index==head){
	    rob[index][0]="";
		rob[index][1]="";
		rob[index][2]="";
		rob[index][3]="";
		incrementHead();
		//write to register
		return true;}
	    return false;
		   }
   
   public void flushRob(){ //if branch is false
	   for(int index=0 ;index< rob.length;index++){
		   
	    rob[index][0]="";
		rob[index][1]="";
		rob[index][2]="";
		rob[index][3]="";
	   }
	   head=0;
	   tail=0;
	   empty=true;
   }
   public int readFromRob(int index){
	   return Integer.parseInt(rob[index][2]);
	   
   }
   public boolean readFromRobReady(int index){
	   if(rob[index][3].equals("Y"))return true;
	   return false;   
   }
}
