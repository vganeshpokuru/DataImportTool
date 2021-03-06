package org.openmf.mifos.dataimport.web;

import java.io.IOException; 
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.openmf.mifos.dataimport.dto.Office;
import org.openmf.mifos.dataimport.handler.Result;
import org.openmf.mifos.dataimport.populator.WorkbookPopulator;
import org.openmf.mifos.dataimport.populator.WorkbookPopulatorFactory;

import com.google.gson.Gson;

@WebServlet(name = "DownloadServiceServlet", urlPatterns = {"/download"}, loadOnStartup=1)
public class DownloadServiceServlet extends HttpServlet {
	
	private static final long serialVersionUID = 2L;
	  
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String fileName = request.getParameter("template");
		try{
			String parameter = null;
			String officeId = request.getParameter("office");
			if(request.getParameter("template").equals("client"))
			    parameter = request.getParameter("clientType");
			WorkbookPopulator populator = WorkbookPopulatorFactory.createWorkbookPopulator(parameter, fileName, officeId, getSelectedOfficeContent(officeId));
			Workbook workbook = new HSSFWorkbook();
	        Result result = downloadAndPopulate(workbook, populator);
			fileName=fileName+".xls";
			writeToStream(workbook, result, response, fileName);
		}catch(Exception e){
			e.printStackTrace();
			throw new ServletException("Cannot download template - " + fileName, e);
		}
	}
	
	Result downloadAndPopulate(Workbook workbook, WorkbookPopulator populator) throws IOException {
        Result result = populator.downloadAndParse();
        if(result.isSuccess()) {
          result = populator.populate(workbook);
        }
        return result;
    }
	
	 void writeToStream(Workbook workbook, Result result, HttpServletResponse response, String fileName) throws IOException {
		 OutputStream stream = response.getOutputStream();
		 if(result.isSuccess()) {
			     response.setContentType("application/vnd.ms-excel");
				 response.setHeader("Content-Disposition", "attachment;filename="+fileName);
	             workbook.write(stream);
	             stream.flush();
	 	         stream.close();
		 }
		 else {
			 OutputStreamWriter out = new OutputStreamWriter(stream,"UTF-8");
			 for(String e : result.getErrors()) {
		            out.write(e);
		        }
			 out.flush();
			 out.close();
		 }
	  }
	 
	 private String getSelectedOfficeContent(String officeId){
		 List<Office> offices = (List<Office>)getServletContext().getAttribute("offices");
		 Office officesContent = null;
		 for (Office office : offices) {
			if(office.getId() == Integer.parseInt(officeId)){
				officesContent = office;
				break;
			}
		}
		return new Gson().toJson(officesContent);
	 }
}
