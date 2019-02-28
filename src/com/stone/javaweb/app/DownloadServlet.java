package com.stone.javaweb.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.stone.javaweb.dao.UploadFileDao;

@WebServlet("/downloadServlet")
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private UploadFileDao dao = new UploadFileDao();

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/x-msdownload");
		String idStr = request.getParameter("id");
		int id = Integer.parseInt(idStr);
		FileUploadBean file = dao.getFile(id);
		System.out.println(file.getFileName());
		System.out.println(file.getFilePath());
		response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getFileName(),"UTF-8"));
		
		ServletOutputStream out = response.getOutputStream();
		InputStream in = new FileInputStream(file.getFilePath());

		byte [] buffer = new byte[1024];
		int len = 0;

		while((len = in.read(buffer)) != -1){
			out.write(buffer, 0, len);
		}

		in.close();
	}
}