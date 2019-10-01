import java.util.ArrayList;
import java.util.Arrays;

class Block {
    int size;
    int [] data;

    public Block(int s){
        data = new int[s];
        size=s;

    }

    public Block clone(){
        Block r = new Block(size);
        r.size = this.size;
        for(int g = 0 ; g <size ; ++g){
            r.data[g] = this.data[g];       //5alty 3ndko la2 magtsh
        }
        return r;
    }

    public String toString(){
        return size+" "+ Arrays.toString(data)+" ** ";
    }

    public void insert(int [] d){
        data=d;

    }

    public int get (int offset){
        return data[offset];
    }
    public void  write (int off,int val){
        data[off]=val;
    }

}
class Entry {
    boolean valid;
    boolean dirty;          //true : dirty
    int tag;
    Block block;

    public Entry(int l){
        block = new Block(l);
    }

    public Entry clone (){
        Entry r = new Entry(this.block.size);
        r.valid = this.valid;
        r.dirty = this.dirty;
        r.tag = this.tag;
        r.block = this.block.clone();
        return r;
    }

    public boolean checkDirty(){
        return dirty;
    }
    public boolean checkValid(){
        return valid;
    }
    public boolean checkTag(int t){
        return tag==t;
    }
    public void replace(int [] b){
        block.insert(b);
    }
    public int get (int offset){
        return block.get(offset);
    }
    public void setTag (int t){
        tag = t;
    }

    public void  write (int off,int val){
        block.write(off,val);
        dirty=true;
    }
    public  String toString(){
        return dirty+" "+valid+" "+block;
    }

}

class Set{
    Entry [] entries;
    int m;
    ArrayList<Integer> q;
    public Set(int m , int l){
        this.m = m;
        entries = new Entry[m];

        for (int i = 0 ; i < m ; ++i){
            entries[i] = new Entry(l);
        }
        q=new ArrayList<>(m);
        for (int i=0;i<m;i++){
            q.add(m-i-1);
        }
    }

    public boolean checkTag (int t){
        for(int i = 0 ; i<m ; ++i){
            if(entries[i].checkTag(t) &&entries[i].checkValid()) return true;
        }
        return false;
    }

    public int get (int t,int o  ){       // t : tag , o : offset

        int index = -1;             //call checkTag

        for(int i = 0 ; i<m ; ++i) {
            if (entries[i].checkTag(t)) {
                index = i;
                break;
            }
        }
        for (int i =0 ; i<q.size() ; i++){
            if(q.get(i) == index) {
                q.remove(i);
                q.add(0,index);
                break;
            }
        }

        return entries[index].get(o);
    }

    public Entry replace (int [] eee , int Tag){
        int lru = q.remove(m-1);
        Entry e= entries[lru].clone();
        q.add(0,lru);
        entries[lru].replace(eee);
        entries[lru].setTag(Tag);
        entries[lru].valid = true;
        entries[lru].dirty = false;
        return e;
    }

    public void write (int tag , int offset , int val){
        int index = -1;             //call checkTag

        for(int i = 0 ; i<m ; ++i) {
            if (entries[i].checkTag(tag)) {
                index = i;
                break;
            }
        }
        entries[index].dirty = true;
        entries[index].write(offset , val);
    }


    public Entry getEntry(int tag){
        int index = -1;             //call checkTag

        for(int i = 0 ; i<m ; ++i) {
            if (entries[i].checkTag(tag)) {
                index = i;
                break;
            }
        }
        return entries[index].clone();
    }
}


class Level {
    int idx;            //level kam
    int ins;            //No of instructions
    int hits;
    int delay;
    int s , l , m ;     //geometry
    Set [] cache ;
    int sizeOfCache ;
    boolean wp;  // true write through else write back
    Memory Memory;
    public Level (int idx, int delay , int s , int l , int m ,boolean wp,Memory mem){
        this.idx = idx;
        this.delay = delay;
        sizeOfCache = (s/l)/m;
        cache = new Set[sizeOfCache];
        this.wp=wp;
        for(int b = 0 ; b <sizeOfCache ; b++){
            cache[b] = new Set(m,l);
        }
       this.Memory=mem;
        this.s=s;
        this.l=l;
        this.m=m;
    }

    public int getHits(){

        return hits;
    }

    public int getMisses(){

        return ins - hits;
    }

    public  boolean getBit(int num,int i){
        return (num &(1<<i))> 0;
    }
    public  int getOffset(int address,int w){
        int lg=(int)(Math.log(w)/Math.log(2));
        int num=0;
        for (int i=0;i<lg;i++){
            num+=(getBit(address,i)?1:0)*(1<<i);
        }
        return num;
    }

    public  int getIndex(int address){
        int lg=(int)(Math.log(l)/Math.log(2));
        address>>=lg;
        return getOffset(address,sizeOfCache);
    }

    public  int getTag(int address){
        int lg=(int)(Math.log(l)/Math.log(2));
         lg+=(int)(Math.log(sizeOfCache)/Math.log(2));
        address>>=lg;
        return address;
    }

    int Address(int address, int tag){
        int ofsize=(int)(Math.log(l)/Math.log(2));
        int indexsize=(int)(Math.log(sizeOfCache)/Math.log(2));
        int res=tag;
        res<<=indexsize;
        res+=getIndex(address);
        res<<=ofsize;
        return res;
    }

    public int read(int address){
        Memory.delayCounter+=delay;
        ins++;
      boolean f=cache[getIndex(address)].checkTag(getTag(address));
        if (!f)
        {
            Entry e=null;
            if (idx==Memory.numOfLevels-1)e=Memory.mem.access(address);
            else e=Memory.lev[idx+1].access(address);
           Entry tmp= cache[getIndex(address)].replace(e.block.data,getTag(address));
            if (!wp && tmp.dirty){
                if (idx==Memory.numOfLevels-1)Memory.mem.update(tmp,Address(address,tmp.tag));
                else  Memory.lev[idx+1].update(tmp,Address(address,tmp.tag));
            }
        }
        else {
            hits++;
        }
        return cache[getIndex(address)].get(getTag(address),getOffset(address,l));
    }




    public  Entry access (int address){
        Memory.delayCounter+=delay;
        ins++;
        boolean f=cache[getIndex(address)].checkTag(getTag(address));
        Entry e=null;
        if (!f)//
        {
            if (idx==Memory.numOfLevels-1)e=Memory.mem.access(address);
            else e=Memory.lev[idx+1].access(address);
           Entry tmp= cache[getIndex(address)].replace(e.block.data,getTag(address));
           if (!wp && tmp.dirty){
               if (idx==Memory.numOfLevels-1)Memory.mem.update(tmp,Address(address,tmp.tag));
              else  Memory.lev[idx+1].update(tmp,Address(address,tmp.tag));
           }
        }
        else{
          e=cache[getIndex(address)].getEntry(getTag(address));
            hits++;
        }
        return e;
    }

    public  void update(Entry e, int address){
        Memory.delayCounter+=delay;
        int index = getIndex(address);
        int off = getOffset(address,l);
        int tag = getTag(address);
        Entry tmp=cache[index].getEntry(tag);
        for (int i=0;i<l;i++)tmp.block.data[i]=e.block.data[i];
        tmp.valid=true;
        tmp.dirty=false;
        tmp.tag=tag;
        if (idx==Memory.numOfLevels-1)Memory.mem.update(tmp,address);
        else  Memory.lev[idx+1].update(tmp,address);
    }

    public void  write(int address,int val){
        if (!wp)writeBack(address,val);
        else writethrough(address,val);
    }
    public void  writeBack(int address,int val){
        Memory.delayCounter+=delay;
        ins++;
        boolean f=cache[getIndex(address)].checkTag(getTag(address));
        if (!f)
        {
            Entry e=null;
            if (idx==Memory.numOfLevels-1)e=Memory.mem.access(address);
            else e=Memory.lev[idx+1].access(address);
           Entry tmp= cache[getIndex(address)].replace(e.block.data,getTag(address));//
            if (!wp && tmp.dirty){
                if (idx==Memory.numOfLevels-1)Memory.mem.update(tmp,Address(address,tmp.tag));
                else  Memory.lev[idx+1].update(tmp,Address(address,tmp.tag));
            }

        }

        else hits++;

            cache[getIndex(address)].write(getTag(address),getOffset(address,l),val);

    }
    public void  writethrough(int address,int val){
        Memory.delayCounter+=delay;
        ins++;
        boolean f=cache[getIndex(address)].checkTag(getTag(address));
        if (!f)
        {
            Entry e=null;
            if (idx==Memory.numOfLevels-1)e=Memory.mem.access(address);
            else e=Memory.lev[idx+1].access(address);
            Entry tmp= cache[getIndex(address)].replace(e.block.data,getTag(address));

        }

        else hits++;

        cache[getIndex(address)].write(getTag(address),getOffset(address,l),val);
        if (idx==Memory.numOfLevels-1)Memory.mem.write(address,val);
        else  Memory.lev[idx+1].write(address,val);

    }
}

class MainMemory{
    int size ;
    int data [];
    int len;
    int delay;
    Memory mem;

    public MainMemory(int l , int delay,Memory mem){
        this.delay = delay;
        this.mem=mem;
        size=1<<16;
        data=new int [size];
        len=l;
        data[1]=2;
    }
    public  void write(int address,int val){
        mem.delayCounter+=delay;
        data[address]=val;
    }
    public int [] getBlock(int address){
       int block []=new int [len];
        int start=(address/len)*len;
        for (int k=0,i=start;k<len;i++,k++){
            block[k]=data[i];
        }
        return block;
    }
    public Entry access(int address){
        mem.delayCounter+=delay;
        Entry e=new Entry(len);
        e.block.data=getBlock(address);
       return e;
    }

   public void  update(Entry e ,int address){
       mem.delayCounter+=delay;
       int start=(address/len)*len;
       int block []=e.block.data;
       for (int k=0,i=start;k<len;i++,k++){
          data[i]= block[k];
       }
   }

}
public class Memory {
     int numOfLevels;
     Level lev [];
    MainMemory mem;//level kam
    int delayCounter;       //Total delay (bta3y w bta3 el levels elly t7ty).
   // int ins;            //No of instructions
    boolean wp;  // true write through else write back
    Memory Memory;
    public Memory (int delay[] , int s[] , int l , int m [],int de,boolean pol[]) {// update l[i]
        numOfLevels=delay.length;
        lev=new Level[numOfLevels];
        for (int i=0;i<numOfLevels;i++){
            lev[i]=new Level (i, delay[i] , s[i] , l , m[i] ,pol[i],this);
        }
        mem=new MainMemory(l,de,this);

    }



//    public int getHits(){
//        int hits = 0 ;
//        for (int i = 0 ; i<numOfLevels ; ++i){
//            hits += lev[i].getHits();
//        }
//        return hits;
//    }
//
//    public int getMisses(){
//
//
//    }


    public int  read(int ad){

        return lev[0].read(ad);
    }

    public void write(int address , int val){
        lev[0].write(address,val);


    }
    public static void main(String[] args) {
        int []sha22aal = {2,2,4,6};
        int [] sha22aal2 = {64,64,256,1024};
        int sha22aal3 = 4;
        int sha22aal4 [] = {2,2,2,2} ;
        int sha22aal5 = 2;
        boolean sha22aal6 [] = {true , true , true , true} ;

        Memory m =new Memory(sha22aal , sha22aal2 , sha22aal3 , sha22aal4 , sha22aal5,sha22aal6 );
       // m.read(100);

        m.write(100,101);
        System.out.println(m.delayCounter);
        m.write(1<<12,33);
        System.out.println(m.delayCounter);
        System.out.println(m.read(1<<12));
        System.out.println(m.read(1<<12));
        System.out.println(m.delayCounter);

    }
}
