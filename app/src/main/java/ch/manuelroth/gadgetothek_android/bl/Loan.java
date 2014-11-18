package ch.manuelroth.gadgetothek_android.bl;

import java.util.Calendar;
import java.util.Date; 
import java.util.UUID;

public class Loan {
	
	private String id;
	private Gadget gadget;
	private Date pickupDate, returnDate;
	private final static int DAYS_TO_RETURN= 7;

	public Loan()
	{
		
	}

	public boolean isLent() {
		return returnDate == null;
	}

	public Date getPickupDate() {
		return pickupDate;
	}

	public Date getReturnDate() {
		return returnDate;
	}
	
	public String getLoanId() {
		return id;
	}
 
	public boolean isOverdue() {
		if ( !isLent() )
			return false;		
		return overDueDate().after(new Date());		
	}
	
	public Date overDueDate() {		
		Calendar cal = Calendar.getInstance();  
		cal.setTime(pickupDate);  
		cal.add(Calendar.DATE, DAYS_TO_RETURN); // add DAYS_TO_RETURN days
		return cal.getTime();		
	}

    public Gadget getGadget() {
        return gadget;
    }

    public void setGadget(Gadget gadget) {
        this.gadget = gadget;
    }
}
