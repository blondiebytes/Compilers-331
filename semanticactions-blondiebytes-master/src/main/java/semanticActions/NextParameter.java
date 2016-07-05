package semanticActions;

/**
 * Created by kathrynhodge on 5/3/16.
 */

import java.util.Stack;
import java.util.LinkedList;
import symbolTable.ParmInformation;

public class NextParameter {
    Stack<LinkedList<ParmInformation>> parmLists;
    Stack<Integer> parmPointers;

    public NextParameter(){
        parmLists = new Stack<LinkedList<ParmInformation>>();
        parmPointers = new Stack<Integer>();
    }

    public void push(LinkedList<ParmInformation> parmList){
        this.parmLists.push(parmList);
        this.parmPointers.push(0);
    }

    public ParmInformation getNextParm(){
        return this.parmLists.peek().get(this.parmPointers.peek());
    }

    public LinkedList<ParmInformation> pop(){
        this.parmPointers.pop();
        return this.parmLists.pop();
    }

    public void increment(){
        int temp = this.parmPointers.pop();
        this.parmPointers.push(temp + 1);
    }
}
