package com.stone.javaweb.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.stone.javaweb.dao.UploadFileDao;

@WebServlet("/ListFiles")
public class ListFiles extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private UploadFileDao dao = new UploadFileDao();

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		List<FileUploadBean> beans = dao.getFiles();
		request.setAttribute("files", beans);
		request.getRequestDispatcher("/app/download.jsp").forward(request, response);
	}
}