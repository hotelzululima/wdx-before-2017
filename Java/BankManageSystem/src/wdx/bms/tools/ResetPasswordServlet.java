package wdx.bms.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import wdx.bms.model.Account;
import wdx.bms.model.Employee;
import wdx.bms.model.Log;
import wdx.bms.util.FileUtil;
import wdx.bms.util.FolderUtil;
import wdx.bms.util.SearchUtil;

public class ResetPasswordServlet extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public ResetPasswordServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>修改密码操作结果</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("    This is ");
		out.print(this.getClass());
		out.println(", using the GET method");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		// 获取对应的数值，并创建对象，写入到文件中
		request.setCharacterEncoding("UTF-8");
		
		String pwd = request.getParameter("pwd");
		String number = request.getParameter("number");		
		String pwd1 = request.getParameter("pwd1");
		String identity = request.getParameter("identity");
	

		String message = "";
		String operator = "";
		String operatorType = "";
		// 获取Cookie信息
		Cookie cklist[] = request.getCookies();
		if (cklist != null && cklist.length > 0)
		{
			for(Cookie c:cklist)
			{
				if (c.getName().equals("name"))
				{
					operator = java.net.URLDecoder.decode(c.getValue(),"UTF-8");
					message += "<tr><td>操作人: </td>";
					message += "<td>" + operator + "</td></tr>";
				}
				
				if (c.getName().equals("type"))
				{
					operatorType = java.net.URLDecoder.decode(c.getValue(),"UTF-8");
					message += "<tr><td>雇员类型: </td>";
					message += "<td>" + operatorType + "</td></tr>";
				}
				
			}
		}
		
		// 获取真实路径
		String realPath = this.getServletContext().getRealPath("index.jsp");
		int end = realPath.lastIndexOf("/");
		realPath = realPath.substring(0, end+1);
				
		String folder = realPath + "EmployeeData";
				
		Log l = new Log();
		Employee e = null;

		
		
		e = SearchUtil.getEmployeeByName(operator, folder);
		
		e.setPassword(pwd1);
		String filepath = folder + "/" + e.getId();
		FileUtil.writeObject(filepath, e);
		
		message += "<tr><td>账户: " + e.getNumber() + "</td>";
		message += "<tr><td>密码: " + pwd1 + "</td></tr>";
		
		if (e != null)
		{
			l.setName(operator);
			l.setNumber(e.getNumber());
			l.setDepartment(e.getDepartment());
			l.setPosition(operatorType);
			l.setAccount(e.getNumber());
			l.setOperation("修改密码");
			l.setOpdetail("账户:" + e.getName());
			
			
			String lfolder = realPath + "LogData";
			FolderUtil.createFolder(lfolder);
			
			Calendar c = Calendar.getInstance();
			String dix = String.valueOf(c.hashCode());
			
			String lfilepath = lfolder + "/" + l.getName() + dix;
			FileUtil.writeObject(lfilepath, l);

		}
		else
		{
			message += "没有对应雇员";
		}
			

		
		
		response.setContentType("text/html");
		// 防乱码
		response.setCharacterEncoding("UTF-8");
		response.setHeader("content-type","text/html;charset=UTF-8");
		
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>修改密码操作结果</TITLE></HEAD>");
		out.println("  <BODY>");
		out.println("<table align=\"center\">");
		out.println("<tr><td colspan=\"2\"> <h1>修改密码结果</h1></td></tr>");
		out.println("<tr><td colspan=\"2\"> <h2>文件存储中的数据</h2></td></tr>");
		out.print(message);
		
		out.println("<tr><td colspan=\"2\"> <h2>数据库中的数据</h2></td></tr>");
		Connection conn=null;
        Statement stmt=null;
        ResultSet rs = null;
        
        
		try 
		{
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://localhost:3306/bms";
			conn = DriverManager.getConnection(url, "root", "wang33");
			stmt = conn.createStatement();
		
			
			// 读取输入最新的记录
			stmt.executeUpdate("update employee set password = '" + e.getPassword()  +"' where id = " + e.getId());
			rs = stmt.executeQuery("select * from employee where id = " + e.getId());
			
			rs.next();
			out.println("<tr><td>密码:</td>");
			out.println("<td>" + rs.getString("password") +  "</td></tr>");
			 
			
			Calendar c = Calendar.getInstance();
			// 写入日志
			stmt.execute("insert into log values (" + c.hashCode()+ ",'" + l.getName() + "','" + l.getNumber() + "','" + l.getDepartment() + "','" + l.getPosition() + "','" + l.getAccount() + "','" + l.getOperation() + "','" + l.getOpdetail()  + "')");
		} 
		catch (SQLException ee) 
		{
		   // TODO Auto-generated catch block
			out.println("SQLException"+ ee.toString()+ "<br/>");
		}
		catch(ClassNotFoundException ce)
        {
			out.println("加载数据库驱动失败<br/>");
			out.println(ce.toString()+ "<br/>");
        }
		
		
		
		out.println("<tr><td colspan=\"2\"><input type=\"button\" value=\"返回\" onclick=\"javascript:history.go(-1);\"/></td></tr>");
		out.println("</table>");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
