
public class Billing {
    public String creditCardNumber;
    public String firstName;
    public String lastName;
    public String creditCardExpiration;

    public Billing(String num, String firstname, String lastname, String date)
    {
    	creditCardNumber = num;
    	firstName = firstname;
    	lastName = lastname;
    	creditCardExpiration = date;
    }

    public String getNameOnCard() { return firstName; }
 
    public String getCreditCardNumber() { return creditCardNumber; }

    public String getCreditCardExpiration(){ return creditCardExpiration; }
   
}
