package com.stone.javaweb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

@WebServlet("/UploadServlet")
public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 1.得到FileItem的集合
		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();

		// 设置内存中最多可以存放的上传文件的大小, 若超出则把文件写到一个临时文件夹中. 以 byte 为单位
		factory.setSizeThreshold(1024 * 500);
		// 设置那个临时文件夹
		File temp = new File("d:\\temp");
		factory.setRepository(temp);

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// 设置上传文件的总的大小. 也可以设置单个文件的大小.
		upload.setSizeMax(1024 * 1024 * 5);

		// Parse the request
		try {
			List<FileItem> /* FileItem */ items = upload.parseRequest(request);
			for (FileItem item : items) {
				// 2.遍历items：若是一个一般的表单域，打印信息
				if (item.isFormField()) {
					String name = item.getFieldName();
					String value = item.getString();
					System.out.println(name + ":" + value);
				} else {
					// 若是文件域，则把文件保存到d:\\code目录下
					String fieldName = item.getFieldName();
				    String fileName = item.getName();
				    String contentType = item.getContentType();
				    long sizeInBytes = item.getSize();
				    
				    System.out.println(fieldName);
				    System.out.println(fileName);
				    System.out.println(contentType);
				    System.out.println(sizeInBytes);
				    
				    InputStream in = item.getInputStream();
				    byte[] buffer = new byte[1024];
				    int len = 0;
				    
				    System.out.println(fileName);
				    int ind = fileName.lastIndexOf("\\");
	                if (ind != -1) {
	                    fileName = fileName.substring(ind + 1);
	                }
	                fileName = "d:\\code\\" + fileName;
				    System.out.println(fileName);
				    
				    FileOutputStream out = new FileOutputStream(fileName);
				    while ((len = in.read(buffer)) != -1) {
						out.write(buffer, 0, len);
					}
				    
				    out.close();
				    in.close();
				}
			}
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
	}
}