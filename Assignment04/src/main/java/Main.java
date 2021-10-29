import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Main
 */
@WebServlet("/MainServlet")
public class Main extends HttpServlet {
	static final long serialVersionUID = 1;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Main() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		// Show Search / Register / Hint on the basis of Selection on the Index Page
		if (request.getParameter("submitBtnMainPage") != null) {
			this.chooseDisplayasPerPageAction(request, out);
		}
		// Query the Database since User tried Registering
		else if (request.getParameter("submitBtnRegister") != null) {		
			String courseId = request.getParameter("courseId");
			String semester = request.getParameter("semester");
			ResultSet results = this.runQuery(this.getRegisterQuery(courseId, semester));
			this.showRegistrationResults(out, results);
		}
		// Query the Database since User Tried Searching for Courses
		else if (request.getParameter("submitBtnSearch") != null) {
			String semesterValue = request.getParameter("semesterDrpDown");
			ResultSet results = this.runQuery(this.getSearchQuery(semesterValue));
			this.showSearchResults(out, results);
		}
	}

	private void chooseDisplayasPerPageAction(HttpServletRequest request, PrintWriter out) {
		// listen to form submission on the main page
		String selectedItem = request.getParameter("selector");
		// Check selected Item from drop-down and take apt action
		if (selectedItem != null && selectedItem.contentEquals("cSrchId")) {
			this.showSearch(out);
		}
		else if (selectedItem != null && selectedItem.contentEquals("cRegId")) {
			this.showRegisteration(out);
		}
		else {
			String addedWarn = this.getBaseHTML()+
					"<p> Please choose an option to proceed! </p>";
			out.print(this.wrapHTMLBody(addedWarn));
		}
	}
	
	// Takes in Query and Runs it and return the results, null in case of exceptions
	private ResultSet runQuery (String query) {
		ResultSet resultSet = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
			Connection con = 
					DriverManager.getConnection("jdbc:mysql://localhost/njit?user=root&password=Marib@123");
			Statement stmt = con.createStatement();
			resultSet = stmt.executeQuery(query);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultSet;
	}
	
	// Takes in arguments and returns the Query for the Registration
	private String getRegisterQuery (String courseId, String semester) {
		String query =  "Select COURSENO, SEMESTER, COURSENAME from COURSES";
		if (semester!=null && courseId != null) {
			query = query + " where SEMESTER=\""+ semester.replaceAll("\\s+","")+"\"";
			query = query + " and COURSENO=\""+ courseId.replaceAll("\\s+","")+"\"";
		}
		// Above query strips off the whites-paces but will crash if any input is null
		else {
			query = query + " where SEMESTER="+ semester;
			query = query + " and COURSNO="+ courseId;
		}
		return query;
	}
	
	// Displays the Registration results depending upon the match
	private void showRegistrationResults(PrintWriter out, ResultSet results) {
		String registrationResultStr = "";
		int COURSENAME_COL_INDEX = 3;
		int SEMESTER_COL_INDEX = 2;
		try {
			// If there is valid result getRow will return 1
			if (results.next()) {
				registrationResultStr = "<hr><h5 style=\"color:White; background-color:green;\"> You are registered in "+ 
						results.getString(COURSENAME_COL_INDEX) +
						" for "+  results.getString(SEMESTER_COL_INDEX) + "</h5>";
			} 
			else {
				registrationResultStr = 
						"<hr><h4 style=\"color:White; background-color:#CC6600;\"> The course is not offered! </h4>";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String baseStr = this.getBaseHTML()+this.getRegistrationHTMLBlock()+ registrationResultStr;
		out.print(this.wrapHTMLBody(baseStr));
	}

	// Takes in arguments and returns the query for the search
	private String getSearchQuery (String semester) {
		String query =  "Select COURSENO, SEMESTER, COURSENAME from COURSES";
		if (semester!=null) {
			query = query + " where SEMESTER=\""+semester.replaceAll("\\s+","")+"\"";
		}
		return query; 
	}
		
	// Displays the search results in a table
	private void showSearchResults(PrintWriter out, ResultSet results) {
		String baseStr = this.getBaseHTML()+this.getSearchHTMLBlock()+this.generateTableForResults(results);
		out.print(this.wrapHTMLBody(baseStr));
	}

	// Fetches the HTML and Displays the Search block along with Base page
	private void showSearch(PrintWriter out) {
		String baseStr = this.getBaseHTML()+this.getSearchHTMLBlock();	
		out.print(this.wrapHTMLBody(baseStr));
	}
	
	// Fetches the HTML and Displays the Registration block along with Base page
	
	private void showRegisteration(PrintWriter out) {
		String baseStr = this.getBaseHTML() + this.getRegistrationHTMLBlock();
		out.print(this.wrapHTMLBody(baseStr));
	}
	
	private String generateTableForResults (ResultSet results) {
		String resultsTable = "";
		ResultSetMetaData resultMetaData;
		try {
			resultMetaData = results.getMetaData();
			resultsTable ="<table>";
		    while (results.next()) {
		    	int colCount = resultMetaData.getColumnCount();
		    	resultsTable +="<tr>";
		        for (int i = 1; i <= colCount; i++) {
		        	resultsTable +="<td>"+results.getString(i)+"</td>";          
		        }
		        resultsTable +="</tr>";
		    }
		    resultsTable+="</table>";

		} catch (SQLException e) {
			resultsTable = this.getBaseHTML()+this.getSearchHTMLBlock()+e;
		}
		return resultsTable;
	}
	
	// Returns the Search Block which shows the form for Search
	private String getSearchHTMLBlock () {
		return "<h3>Course Search </h3><br>"+
				"<form action=\"http://localhost:8080/Assignment04/MainServlet\">"+
					"<label>Semester &nbsp</label>"+
					"<select name=\"semesterDrpDown\" id=\"semDrpDown\">"+
					"<option>Select Semester</option>"+
					"<option value=\"Fall2021\">Fall 2021</option>"+
					"<option value=\"Spring2022\">Spring 2022</option>"+
					"</select>"+
					"&nbsp<input type=\"submit\" value=\"Submit\" name=\"submitBtnSearch\">"+
			    "</form>";
	}
	
	// Returns the HTML block which shows registration form
	private String getRegistrationHTMLBlock () {
		return "<h3>Registration </h3><br>"+
				"<form action=\"http://localhost:8080/Assignment04/MainServlet\">"+
					"<p>Please Enter the Course Id and Semester to register! </p>"+
					"<label>Course Id: </label>"+
					"<input type=\"text\" name=\"courseId\" style='text-transform:uppercase' size=\"20\"><br><br>"+
					"<label>Semester: </label>"+
					"<input type=\"text\" name=\"semester\" style='text-transform:uppercase' size=\"20\"><br><br>"+
					"<input type=\"submit\" value=\"Submit\" name=\"submitBtnRegister\">"+
			    "</form>";
	}
	
	// Returns the Basic HTML which is shown for everyPage
	
	private String getBaseHTML() {
		return 
		"<title>NJIT Registration System</title>"+
		"</head>"+
		"<style>"+
		"th, td {"+
			  "padding: 8px;"+
			"}"+
		"</style>"+
		"<body style=\"background-color:#C1C6C8;\">" +
		"<h1 style=\"background-color:#D22630; color:white\">&#160; New Jersey Institute of Technology </h1>"+
		"<h3>Welcome to Course Registration site of NJIT</h3>"+
		"<hr>"+
		"<form action=\"http://localhost:8080/Assignment04/MainServlet\">"+
			"<br>"+
			"<input type=\"radio\" name=\"selector\" value=\"cSrchId\" />"+
			"<label>Course Search</label> <br> <br>"+
			"<input type=\"radio\" name=\"selector\" value=\"cRegId\" />"+
			"<label>Course Registration</label><br><br>"+
			"<input type=\"submit\" value=\"Submit\" name=\"submitBtnMainPage\">"+
		"</form>"+
		"<hr>";
	}

	// To wrap the HTML Blocks common for all
	
	private String wrapHTMLBody(String basePageHTML) {
		return "<!DOCTYPE html>"+"<html>"+"<head>"+
				basePageHTML+
				"</body>"+"</html>";
	}
}
