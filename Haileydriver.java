
import java.beans.Statement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class NEW {
	public static void main(String[] args) {
		// TODO code application logic here

		String url = "jdbc:mysql://projgw.cse.cuhk.edu.hk:2633/group4";
		String username = "Group4";
		String password = "3170group4";

		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			// handle any errors
			System.out.println("[ERROR] Java MySQL DBDriver not found!!");
			System.exit(0);
		} catch (SQLException ex) {
			System.out.println(ex);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	public static void main_menu(Connection conn) throws SQLException {
		System.out.println("Welcome! Who are you?");
		System.out.println("1. An administrator");
		System.out.println("2. A passenger");
		System.out.println("3. A driver");
		System.out.println("4. A manager");
		System.out.println("5. None of the above");

		while (true) {
			Scanner scan = new Scanner(System.in);
			int input;
			System.out.print("Please enter [1-4]");
			input = scan.nextInt();
			if (input == 1) {
				admin_operation(conn);
			} else if (input == 2) {
				passenger_operation(conn);
			} else if (input == 3) {
				driver_operation(conn);
			} else if (input == 4) {
				manager_operation(conn);
			} else if (input == 5) {
				System.out.println("Good bye :)");
				System.exit(1);
			} else
				System.out.println("[ERROR] Invalid input");

		}
	}

	private static void admin_operation(Connection conn) {
		// TODO Auto-generated method stub

	}

	private static void passenger_operation(Connection conn) {
		// TODO Auto-generated method stub

	}

	private static void driver_operation(Connection conn) throws SQLException {

		System.out.println("Driver, what would you like to do?");
		System.out.println("1. Search requests");
		System.out.println("2. Take a request");
		System.out.println("3. Finish a trip");
		System.out.println("4. Go back");

		while (true) {
			Scanner scan = new Scanner(System.in);
			int input;
			System.out.print("Please enter [1-4]");
			input = scan.nextInt();
			if (input == 1) {
				searchrequests(conn);
			} else if (input == 2) {
				takearequest(conn);
			} else if (input == 3) {
				finishatrip(conn);
			} else if (input == 4) {
				main_menu(conn);
			} else
				System.out.println("[ERROR] Invalid input");
		}
	}

	private static void searchrequests(Connection conn) throws SQLException {

		Scanner scan = new Scanner(System.in);
		System.out.println("Please enter your ID.");
		int driverid = scan.nextInt();
		System.out.println("Please enter the coordinates of your location.");
		int CX = scan.nextInt();
		int CY = scan.nextInt();
		System.out.println("Please enter the maximum distance from you to the passenger.");
		int Wdistance = scan.nextInt();

		/*
		 * Check if the driver should be qualified for the request + Check if the
		 * distance between the requested start location and the driver's location
		 * should be less than or equal to the specified distance
		 */

		PreparedStatement driverQ = conn.prepareStatement(
				"SELECT reqeust.id, passenger.name, request.passengers, request.start_location, request.destination"
						+ "FROM request, driver, vehicle, taxi_stop, passenger"
						+ "WHERE driver.vehicle_id = vehicle.id AND passenger.id = request.passenger_id AND"
						+ "taxi_stop.name = request.start_location AND taxi_stop.name = request.destination AND"
						+ "request.taken = NULL AND" 
						+ "request.passengers <= vehicle.seats AND " 
						+ "(Request.model = NULL OR Request.model = vehicle.model) AND "
						+ "(NRequest.drivingyears = null OR NRequest.drivingyears <= Drivers.Drivingyears)"
						+ "driver.id = ? AND" // userID
	// PROBLEM with below 15 lines!!!!
						+ "? < ?") ;// distance 
		
		driverQ.setInt(1, driverid);

		Statement TaxiX = (Statement) conn.createStatement();
		String TaxiX1 = "SELECT location_x FROM taxi_stop, request WHERE taxi_stop.name = request.start_location";
		ResultSet taxilocationx = ((java.sql.Statement) TaxiX).executeQuery(TaxiX1);
		Statement TaxiY = (Statement) conn.createStatement();
		String TaxiY1 = "SELECT location_y FROM TaxiStop, Request WHERE taxi_stop.name = request.start_location";
		ResultSet taxilocationy = ((java.sql.Statement) TaxiY).executeQuery(TaxiY1);
		driverQ.setInt(2, (CX - Integer.parseInt(taxilocationx)) + (CY - Integer.parseInt(taxilocationy)));
		driverQ.setInt(3, Wdistance);

		ResultSet nrs = driverQ.executeQuery();

		// Print result
		System.out.println("request ID, passenger name, num of passengers, start location, destination");

		while (nrs.next()) {
			int a = nrs.getInt("request.id");
			String b = nrs.getString("passenger.name");
			int c = nrs.getInt("reqeust.passengers");
			String d = nrs.getString("request.start_location");
			String e = nrs.getString("request.destination");
			System.out.println(a +", " + b+", " + c +", " + d +", " + e);
		}

	}

	private static void takearequest(Connection conn) throws SQLException {

		Scanner scan = new Scanner(System.in);
		System.out.println("Please enter your ID.");
		int userID = scan.nextInt();
		System.out.println("Please enter your request ID.");
		int rID = scan.nextInt();

		// mark request taken
		PreparedStatement pstmt = conn
				.prepareStatement("UPDATE request SET taken= 'Taken' WHERE request.id = ?");
		pstmt.setInt(1, rID);
		ResultSet rs = pstmt.executeQuery();

		// create new trip
		PreparedStatement CreateTrip = conn.prepareStatement(
		"INSERT INTO trip(id,driver_id, passenger_id, start_location, destination, start_time, end_time, fee FROM request WHERE request.id = ?)");
		
		//PROBLEM:  how to add trip id? - it should be incremental & increase by 1 from the previous trip id
		// if cant, should prevent duplication.

		CreateTrip.setInt(2, userID);
		
		//PROBLEM with below 20 lines... seems it has some errors. 
		
		PreparedStatement PID1 = conn.prepareStatement( "SELECT passenger_id FROM request WHERE request_id = ? ");
		PID1.setInt(1, rID);
		ResultSet PID = PID1.executeQuery();
		CreateTrip.setLong(3, PID);

		String SL1 = "SELECT start_location FROM request WHERE request_id = ? ";
		SL1.setInt(1, rID);
		Statement SL2 = (Statement) conn.createStatement();
		ResultSet SL = ((java.sql.Statement) SL2).executeQuery(SL1);
		CreateTrip.setString(4, SL);

		String DL1 = "SELECT destination FROM request WHERE request_id = ?";
		DL1.setInt(1, rID);
		Statement DL2 = (Statement) conn.createStatement();
		ResultSet DL = ((java.sql.Statement) DL2).executeQuery(DL1);
		CreateTrip.setString(5, DL);

		Date date_now = new Date(System.currentTimeMillis());
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String starttime = format.format(date_now);
		CreateTrip.setString(6, starttime);
		
		ResultSet rs1 = CreateTrip.executeQuery();
		rs1.next();
		
		// display the trip
		System.out.println("Trip ID, Passenger name, Start");
		
		Statement statement = (Statement) conn.createStatement();
		String query = "SELECT rs1.id, passenger.name, rs1.start_time FROM rs1 WHERE rs1.passenger_id = passenger.id";
		ResultSet result = ((java.sql.Statement) statement).executeQuery(query);
		while(result.next()) {
			int a = result.getInt("rs1.id");
			String b = result.getString("passenger.name");
			String c = result.getString("rs1.start_time");
			System.out.println(a +", " + b +", " + c);
		}
	
		
	}

	private static void finishatrip(Connection conn) throws SQLException, ParseException {
		Scanner scan = new Scanner(System.in);
		
		System.out.println("Please enter your ID.");
		int userID = scan.nextInt();
		
		// look for unfinished trip by this driver
		PreparedStatement pstmt = conn.prepareStatement("SELECT trip.id, passenger.name, trip.start_time FROM trip, passenger WHERE driver_id=? AND end_time = NULL AND passenger.id = trip.passenger_id");
		pstmt.setInt(1, userID);
		ResultSet result = pstmt.executeQuery();
		int tid = result.getInt("trip.id");

		// display current Trip Info
		System.out.println("Trip ID, Passenger ID, Start");
	
		while(result.next()) {
			int a = result.getInt("trip.id");
			String b = result.getString("passenger.name");
			String c = result.getString("trip.start_time");
			System.out.println(a +", " + b +", " + c);
		}
	
	

		// ask to finish the trip
		System.out.println("Do you wish to finish the trip? [y/n]");
		String finish = scan.toString();

		if (finish == "y") {
			// finish the trip and calculate fee
			PreparedStatement Finish = conn.prepareStatement("UPDATE trip SET end_time =?, fee=? WHERE id = ?");
			Finish.setInt(3, tid);

			Date date_now = new Date(System.currentTimeMillis());
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String endtime = format.format(date_now);
			Finish.setString(1, endtime);

			String ST1 = "SELECT start_time FROM trip";
			Statement ST2 = (Statement) conn.createStatement();
			ResultSet ST = ((java.sql.Statement) ST2).executeQuery(ST1);
			String starttime = ST.getString("start_time");
			
			Date d1 = (Date) format.parse(starttime);
			Date d2 = (Date) format.parse(endtime);
			
			Long difference = d2.getTime() - d1.getTime();
			Long fee = difference/ (60 * 1000);
			Finish.setLong(2, fee);
			
			ResultSet result2 = pstmt.executeQuery();
			result2.next();

			System.out.println("Trip ID, Passenger name, Start, End, Fee");
			// display finished trip info
			
			
			PreparedStatement pstmt1 = conn.prepareStatement("SELECT trip.id, passenger.name, trip.start_time, trip.end_time, trip.fee FROM trip, passenger WHERE trip_id=? AND passenger.id = trip.passenger_id");
			pstmt1.setInt(1, tid);
			ResultSet result1 = pstmt1.executeQuery();

			// display current Trip Info
			System.out.println("Trip ID, Passenger ID, Start");
			while(result1.next()) {
				int a = result1.getInt("trip.id");
				String b = result1.getString("passenger.name");
				String c = result1.getString("trip.start_time");
				String d = result1.getString("trip.end_time");
				String e = result1.getString("trip.fee");
				System.out.println(a +", " + b +", " + c+", " +d+", " +e);
			}
			
			
		} else if (finish == "n") {
			driver_operation(conn);
		}

	}

	private static void manager_operation(Connection conn) {
		// TODO Auto-generated method stub

	}

}
