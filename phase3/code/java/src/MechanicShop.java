/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.InputStream;
import java.io.FileInputStream;
/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		MechanicShop esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new MechanicShop (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. AddCustomer");
				System.out.println("2. AddMechanic");
				System.out.println("3. AddCar");
				System.out.println("4. InsertServiceRequest");
				System.out.println("5. CloseServiceRequest");
				System.out.println("6. ListCustomersWithBillLessThan100");
				System.out.println("7. ListCustomersWithMoreThan20Cars");
				System.out.println("8. ListCarsBefore1995With50000Milles");
				System.out.println("9. ListKCarsWithTheMostServices");
				System.out.println("10. ListCustomersInDescendingOrderOfTheirTotalBill");
				System.out.println("11. < EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddCustomer(esql); break;
					case 2: AddMechanic(esql); break;
					case 3: AddCar(esql); break;
					case 4: InsertServiceRequest(esql); break;
					case 5: CloseServiceRequest(esql); break;
					case 6: ListCustomersWithBillLessThan100(esql); break;
					case 7: ListCustomersWithMoreThan20Cars(esql); break;
					case 8: ListCarsBefore1995With50000Milles(esql); break;
					case 9: ListKCarsWithTheMostServices(esql); break;
					case 10: ListCustomersInDescendingOrderOfTheirTotalBill(esql); break;
					case 11: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
	public static void AddCustomer(MechanicShop esql){//1

		//Scanner in  = new Scanner(System.in);
		try{
			System.out.print("Enter Customer id: ");
			int id = Integer.parseInt(in.readLine());
			System.out.print("\nEnter Customer first name: ");
			String fname = in.readLine();
			
			System.out.print("\nEnter Customer last name: ");
			String lname = in.readLine();
			
			System.out.print("\nEnter Customer phone number: ");
			String  phone = in.readLine();

			System.out.print("\nEnter Customer address: ");
			String address = in.readLine();


			//if(id!=null && fname!=null && lname!=null && phone!=null && address!=null) {
			//Statement stmt = _connection.createStatement();
			String query = String.format("INSERT INTO Customer(id,fname,lname,phone,address) VALUES (%d ,'%s','%s','%s','%s')",id,fname,lname,phone,address);
			esql.executeUpdate(query);
		}catch(Exception e){
			System.out.println("Query couldn't execute");
		}
}
	
	public static void AddMechanic(MechanicShop esql){//2
try{
			System.out.print("Enter Mechanic id: ");
			int id = Integer.parseInt(in.readLine());
			System.out.print("\nEnter Mechanic first name: ");
			String fname = in.readLine();
			
			System.out.print("\nEnter Mechanic last name: ");
			String lname = in.readLine();
			
			System.out.print("\nEnter years of experience");
			int exp = Integer.parseInt(in.readLine());
			

			String query = String.format("INSERT INTO Mechanic(id,fname,lname,experience) VALUES (%d ,'%s','%s',%d)",id,fname,lname,exp);
			esql.executeUpdate(query);
		}
		catch(Exception e){
			System.out.println("Query couldn't execute");
		}

	}		
	
	
	public static void AddCar(MechanicShop esql){//3
		try{
			System.out.print("Enter car VIN number: ");
			String vin = in.readLine();

			System.out.print("\nEnter car make: ");
			String make = in.readLine();
			
			System.out.print("\nEnter car model: ");
			String model = in.readLine();
			
			System.out.print("\nEnter car year: ");
			int year = Integer.parseInt(in.readLine());
			

			String query = String.format("INSERT INTO Car(vin,make,model,year) VALUES ('%s' ,'%s','%s', %d)",vin,make,model,year);
			esql.executeUpdate(query);
		}
		catch(Exception e){
			System.out.println("Query couldn't execute");
		}
	}
	
	public static void InsertServiceRequest(MechanicShop esql){//4
		try{
        		String todaysdate = "MM/dd/yyyy";
			String cust_ID="";
			String car_ID="";
			System.out.print("Enter the last name of the customer: ");
                        String lastName = in.readLine();
			String query = "SELECT * FROM Customer WHERE lname='";
			query += lastName + "';";

			int customerExists = esql.executeQuery(query); 
			if (customerExists != 0){ 
				System.out.println("Enter the customer ID: ");
				cust_ID = in.readLine();
			}
			else{ 
				System.out.println("There are no customers with that last name. Please add a new customer.");
				AddCustomer(esql);
				System.out.println("Reenter the customer ID: ");
				cust_ID = in.readLine();
			}	
			
			
			query = "SELECT car_vin FROM Owns WHERE customer_id='";
			query += cust_ID + "';";
				
			int carExists = esql.executeQuery(query); 
				
			if (carExists !=0){
				System.out.println("Enter the VIN: ");
				car_ID = in.readLine();
			}
			else { 
				System.out.println("The customer doesn't own a car. Please add a new car.");
				AddCar(esql);
				System.out.println("Reenter the VIN: ");
				car_ID = in.readLine();
			}	
			
			query = "SELECT * FROM Owns WHERE car_vin='";
			query += car_ID + "' AND customer_id='";
			query += cust_ID + "';";
				
			int owns = esql.executeQuery(query);
			if (owns != 0){
				query = "INSERT INTO Service_Request(rid, customer_id, car_vin, date, odometer, complain) VALUES ('";
				System.out.println("Enter the Service Request ID: ");
				int rid = Integer.parseInt(in.readLine());
				query += rid + "', '";
				query += cust_ID + "', '" + car_ID + "', " + todaysdate + ", '";
				System.out.println("Enter the odometer reading: ");
				String odometer = in.readLine();
				query += odometer + "', '";
				System.out.println("What is the issue? ");
				String complain = in.readLine();
				query += complain + "');";
						
				esql.executeUpdate(query);
		
				System.out.println("------------------------------------------------");
				System.out.println("New service request created.");
				query = "SELECT * FROM Service_Request WHERE rid='";
				query+= rid + "';";
				System.out.println("------------------------------------------------");
	
			}
			else { 
				System.out.println("This customer doesn't own this car");
			}
		} catch(Exception e){
				System.err.println(e.getMessage());
		}

	}
	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		try{

			esql.executeQueryAndPrintResult("SELECT date, comment, bill FROM Closed_Request WHERE bill < 100 GROUP BY date, comment, bill");		
		}
		catch(Exception e){
			System.out.println("Query could not execute");
		}
	}

	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
		try{
			esql.executeQueryAndPrintResult("SELECT C.fname, C.lname FROM Customer AS C WHERE 20 < (SELECT COUNT(*) FROM Owns O WHERE C.id = O.customer_id)");
		}
		
		catch(Exception e){
			System.out.println("Query couldn't execute");
		}	
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		try{
			esql.executeQueryAndPrintResult("SELECT C.make, C.model, C.year FROM Car AS C, Service_Request AS S WHERE C.vin = S.car_vin AND S.odometer < 50000 AND C.year < 1995");
		}
		catch(Exception e){
			System.out.println("Query couldn't execute.");
		}	
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		//
		try{
	System.out.print("Enter a value, k, greater than 0, for which the first k values with the highest number of service requests will be shown: ");
	int x = Integer.parseInt(in.readLine());
	esql.executeQueryAndPrintResult("SELECT make, model, a.num_requests FROM Car c, (SELECT car_vin, COUNT(rid) AS num_requests FROM Service_Request GROUP BY car_vin ) AS a WHERE a.car_vin = c.vin ORDER BY a.num_requests DESC LIMIT " + x);
}
catch(Exception e){
	System.out.println("Query couldn't execute");
}
		
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//9
		//
		try{
			esql.executeQueryAndPrintResult("SELECT C.fname, C.lname, total FROM Customer AS C,(SELECT SR.customer_id, SUM(CR.bill) AS total FROM Closed_Request AS CR, Service_Request AS SR WHERE CR.rid = SR.rid GROUP BY SR.customer_id) AS B WHERE C.id=B.customer_id ORDER BY B.total DESC;"); // GROUP BY C.fname, C.lname, SUM(CR.bill) ORDER BY SUM(CR.bill) DESC;");
}catch(Exception e){
		System.out.println("Query could not exeute");
}
}	
}
