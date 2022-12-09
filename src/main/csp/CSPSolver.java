package main.csp;

import java.time.LocalDate;
import java.util.*;

import test.csp.CSPTests;

/**
 * CSP: Calendar Satisfaction Problem Solver
 * Provides a solution for scheduling some n meetings in a given
 * period of time and according to some unary and binary constraints
 * on the dates of each meeting.
 */
public class CSPSolver {

    // Backtracking CSP Solver
    // --------------------------------------------------------------------------------------------------------------
    
    /**
     * Public interface for the CSP solver in which the number of meetings,
     * range of allowable dates for each meeting, and constraints on meeting
     * times are specified.
     * @param nMeetings The number of meetings that must be scheduled, indexed from 0 to n-1
     * @param rangeStart The start date (inclusive) of the domains of each of the n meeting-variables
     * @param rangeEnd The end date (inclusive) of the domains of each of the n meeting-variables
     * @param constraints Date constraints on the meeting times (unary and binary for this assignment)
     * @return A list of dates that satisfies each of the constraints for each of the n meetings,
     *         indexed by the variable they satisfy, or null if no solution exists.
     */
    public static List<LocalDate> solve (int nMeetings, LocalDate rangeStart, LocalDate rangeEnd, Set<DateConstraint> constraints) {
        // [!] TODO!
    
    	//List<MeetingDomain> domainForEachMeeting = new ArrayList<>();
    	//MeetingDomain domain =  new MeetingDomain(rangeStart, rangeEnd);
    	List<LocalDate> meetings = new ArrayList<>();
    	List<MeetingDomain> domains = CSPTests.generateDomains(nMeetings, rangeStart, rangeEnd);
        return backTrack(domains, meetings, nMeetings, constraints );
    }
    
    //recursiveBacktracking
    private static List<LocalDate> backTrack (List<MeetingDomain> domainForEachMeeting, List<LocalDate> meetingsAssigned,int nMeetings, Set<DateConstraint> constraints){
    	if(meetingsAssigned.size() == nMeetings && isConsistent(constraints, meetingsAssigned)) {
    		return meetingsAssigned;
    	}	
    	for(MeetingDomain thisMeeting : domainForEachMeeting) {
	    	for(LocalDate domainValue : thisMeeting.domainValues) {
	    		meetingsAssigned.add(domainValue);
	    		if(isConsistent(constraints, meetingsAssigned)) {
	    				//add first and then check the consistent, if true, rest of pseudo is the same
	    				//else, remove
	    				//meetingsAssigned.add(domainValue);
	    			List<LocalDate> result = backTrack(domainForEachMeeting, meetingsAssigned, nMeetings, constraints);
	    			if(result != null) {
	    				return result;
	    			}	
	    		}
	    		meetingsAssigned.remove(meetingsAssigned.size() - 1);
	    	}
    	}
    	return null;	
    }
    /**
     * Private method to check consistency of our meeting with the constraints 
     * @param constraints
     * @param meeting
     * @return
     */
    private static boolean isConsistent(Set<DateConstraint> constraints, List<LocalDate> meeting) {
    	for(DateConstraint constraint : constraints) {
    		//if Urnary
    		if(constraint.ARITY == 1) {
    			UnaryDateConstraint newUnaryConst = (UnaryDateConstraint)constraint;
    			LocalDate r = newUnaryConst.R_VAL; 
    			int l = constraint.L_VAL;
    			//assignment doesn't satisfy constraints
    			if(meeting.size() > l) {
	    			if(!(constraint.isSatisfiedBy(meeting.get(l), r))) {
	    	
	    				return false;
	    			}
    			}
    		}
    		//if Binary
    		if(constraint.ARITY == 2) {
    			BinaryDateConstraint newBinaryConst = (BinaryDateConstraint)constraint;
    			int r = newBinaryConst.R_VAL;
    			int l = constraint.L_VAL;
    			//assignment doesn't satisfy constraints
    			if(meeting.size() > r && meeting.size() >l) {
	    			if(!(constraint.isSatisfiedBy(meeting.get(l), meeting.get(r)))) {
	    				return false;
	    			}
    				
    			}
    		}		
    	}
    	return true;
    }
    
    // Filtering Operations
    // --------------------------------------------------------------------------------------------------------------
    
    /**
     * Enforces node consistency for all variables' domains given in varDomains based on
     * the given constraints. Meetings' domains correspond to their index in the varDomains List.
     * @param varDomains List of MeetingDomains in which index i corresponds to D_i
     * @param constraints Set of DateConstraints specifying how the domains should be constrained.
     * [!] Note, these may be either unary or binary constraints, but this method should only process
     *     the *unary* constraints! 
     */
    public static void nodeConsistency (List<MeetingDomain> varDomains, Set<DateConstraint> constraints) {
    	Set<LocalDate> result = new HashSet<>();
    	for(DateConstraint constraint : constraints) {
    		if(constraint.ARITY == 1) {
    		UnaryDateConstraint unaryDateConstraint = (UnaryDateConstraint)constraint;
    		LocalDate r = unaryDateConstraint.R_VAL; 
			int l = unaryDateConstraint.L_VAL;
    		MeetingDomain newDomain = varDomains.get(l);
	    	for(LocalDate date : newDomain.domainValues ) {
	    		if((unaryDateConstraint.isSatisfiedBy(date, r))) {
	    			result.add(date);
	    		}	
	    	} 
    		
	    	newDomain.domainValues = result;
	    	result = new HashSet<>();
    		}
    	}
    }
    
    /**
     * Enforces arc consistency for all variables' domains given in varDomains based on
     * the given constraints. Meetings' domains correspond to their index in the varDomains List.
     * @param varDomains List of MeetingDomains in which index i corresponds to D_i
     * @param constraints Set of DateConstraints specifying how the domains should be constrained.
     * [!] Note, these may be either unary or binary constraints, but this method should only process
     *     the *binary* constraints using the AC-3 algorithm! 
     */
    public static void arcConsistency (List<MeetingDomain> varDomains, Set<DateConstraint> constraints) {
        
    	Set<Arc> queueOfArc = new HashSet<>();
    	//Generate arcs for each date constraint
    	for(DateConstraint constraint:constraints) {
    		if(constraint.ARITY == 2) {
    			BinaryDateConstraint newBinaryConstraint = (BinaryDateConstraint)constraint;
    			int l = newBinaryConstraint.L_VAL;
    			int r = newBinaryConstraint.R_VAL;
    			Arc arc1 = new Arc(l, r, newBinaryConstraint);
    			Arc arc2 = new Arc(r, l, newBinaryConstraint);
    			queueOfArc.add(arc1);
    			queueOfArc.add(arc2);	
    		}
    	}
    	while (!queueOfArc.isEmpty()) {
			Arc removedItem = queueOfArc.iterator().next();
			queueOfArc.remove(removedItem);	
	    	if(removeInconsistentValues(varDomains, removedItem)){
		    	for(DateConstraint constraint : constraints) {
		    		if(constraint.ARITY == 2) {
    					BinaryDateConstraint binaryConst = (BinaryDateConstraint)constraint;
    					if(binaryConst.L_VAL == removedItem.TAIL || binaryConst.R_VAL == removedItem.TAIL){
    						Arc newArc = new Arc(binaryConst.L_VAL, binaryConst.R_VAL, constraint);
    						queueOfArc.add(newArc);
    					}	
		    		}	
		    	}
	    	}
    	}  
    }
    
    /*
     * Helper method
     */
    private static boolean removeInconsistentValues (List<MeetingDomain> domains, Arc arc) {
    	Set<LocalDate> result = new HashSet<>();
    	MeetingDomain tailDomain = domains.get(arc.TAIL);
    	MeetingDomain headDomain = domains.get(arc.HEAD);
    	DateConstraint constraint = arc.CONSTRAINT;
    	for(LocalDate tailVal : tailDomain.domainValues) {
    		for(LocalDate headVal : headDomain.domainValues) {
    			if(constraint.ARITY == 2) {
    			BinaryDateConstraint newBinaryConstraint = (BinaryDateConstraint)constraint;
	    			if(newBinaryConstraint.isSatisfiedBy(tailVal, headVal)) {
	    				result.add(tailVal);
	    				break;
	    			}
    			}
    		}
    	}
    	boolean changed = result.size() != tailDomain.domainValues.size();
    	tailDomain.domainValues = result;
    	return changed;	
    }
    /**
     * Private helper class organizing Arcs as defined by the AC-3 algorithm, useful for implementing the
     * arcConsistency method.
     * [!] You may modify this class however you'd like, its basis is just a suggestion that will indeed work.
     */
    private static class Arc {
        
        public final DateConstraint CONSTRAINT;
        public final int TAIL, HEAD;
        
        /**
         * Constructs a new Arc (tail -> head) where head and tail are the meeting indexes
         * corresponding with Meeting variables and their associated domains.
         * @param tail Meeting index of the tail
         * @param head Meeting index of the head
         * @param c Constraint represented by this Arc.
         * [!] WARNING: A DateConstraint's isSatisfiedBy method is parameterized as:
         * isSatisfiedBy (LocalDate leftDate, LocalDate rightDate), meaning L_VAL for the first
         * parameter and R_VAL for the second. Be careful with this when creating Arcs that reverse
         * direction. You may find the BinaryDateConstraint's getReverse method useful here.
         */
        public Arc (int tail, int head, DateConstraint c) {
            this.TAIL = tail;
            this.HEAD = head;
            this.CONSTRAINT = c;
        }
        
        @Override
        public boolean equals (Object other) {
            if (this == other) { return true; }
            if (this.getClass() != other.getClass()) { return false; }
            Arc otherArc = (Arc) other;
            return this.TAIL == otherArc.TAIL && this.HEAD == otherArc.HEAD && this.CONSTRAINT.equals(otherArc.CONSTRAINT);
        }
        
        @Override
        public int hashCode () {
            return Objects.hash(this.TAIL, this.HEAD, this.CONSTRAINT);
        }
        
        @Override
        public String toString () {
            return "(" + this.TAIL + " -> " + this.HEAD + ")";
        }
        
    }
    
}
