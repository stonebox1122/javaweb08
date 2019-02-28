package com.stone.javaweb.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.stone.javaweb.dao.UploadFileDao;
import com.stone.javaweb.exception.InvalidExtNameException;
import com.stone.javaweb.utils.FileUtils;

@WebServlet("/FileUploadServlet")
public class FileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String FILE_PATH = "/files";
	private static final String TEMP_DIR = "d:\\tmp\\upload";
	private UploadFileDao dao = new UploadFileDao();

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String path = null;
		ServletFileUpload upload = getServletFileUpload();
		
		try {
			//把需要上传的FileItem都放入到该Map中
			//键：文件的待存放的路径；值：对应的FileItem对象
			Map<String, FileItem> uploadFiles = new HashMap<String, FileItem>();
			
			//解析请求，得到FileItem的集合
			List<FileItem> items = upload.parseRequest(request);
			
			//1.构建FileUploadBean的集合，同时填充uploadFiles
			List<FileUploadBean> beans = buildFileUploadBeans(items, uploadFiles);
			
			//2.验证扩展名
			validateExtName(beans);
			
			//3.校验文件的大小：在解析时，已经校验了，只需要通过异常得到结果
			
			//4.进行文件的上传操作
			upload(uploadFiles);
			
			//5.把上传的信息保存到数据库中
			saveBeans(beans);
			
			//6.删除临时文件夹的临时文件
			FileUtils.delAllFile(TEMP_DIR);
			
			path = "/app/success.jsp";
			
		} catch (Exception e) {
			e.printStackTrace();
			path = "/app/upload.jsp";
			request.setAttribute("message", e.getMessage());
		}
		request.getRequestDispatcher(path).forward(request, response);
	}

	private void saveBeans(List<FileUploadBean> beans) {
		dao.save(beans);
	}

	/**
	 * 文件上传前的准备工作，得到filePath和InputStream
	 * @param uploadFiles
	 * @throws IOException
	 */
	private void upload(Map<String, FileItem> uploadFiles) throws IOException {
		for (Map.Entry<String, FileItem> uploadFile : uploadFiles.entrySet()) {
			String filePath = uploadFile.getKey();
			FileItem item = uploadFile.getValue();
			upload(filePath, item.getInputStream());
		}
	}

	/**
	 * 文件上传的IO方法
	 * @param filePath
	 * @param inputStream
	 * @throws IOException
	 */
	private void upload(String filePath, InputStream inputStream) throws IOException {
		OutputStream out = new FileOutputStream(filePath);
		
		byte[] buffer = new byte[1024];
		int len = 0;
		
		while ((len = inputStream.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		
		inputStream.close();
		out.close();
		System.out.println(filePath);
	}

	/**
	 * 校验扩展名是否合法
	 * @param beans
	 */
	private void validateExtName(List<FileUploadBean> beans) {
		String exts = FileUploadProperties.getInstance().getProperty("exts");
		List<String> extList = Arrays.asList(exts.split(","));
		for (FileUploadBean bean : beans) {
			String fileName = bean.getFileName();
			String extName = fileName.substring(fileName.lastIndexOf(".") + 1);
			if (!extList.contains(extName)) {
				throw new InvalidExtNameException(fileName + "文件扩展名不合法");
			}
		}
	}

	/**
	 * 利用传入的FileItem集合构建FileUploadBean集合，同时填充uploadFiles
	 * FileUploadBean对象封装了：id，fileName，filePath，fileDesc
	 * uploadFiles：Map<String, FileItem>类型，存放文件域类型的FileItem，键：待保存的文件的名字；值：FileItem对象
	 * 
	 * 构建过程：
	 * 1.对传入的List<FileItem>进行遍历，得到desc的Map，键：desc的fieldName(desc1, desc2 ... )；值：desc输入框输入值
	 * 2.对传入的List<FileItem>进行遍历，到文件域的FileItem对象。构建对应的key(desc1 ... )来获取其desc。构建FileUploadBean对象，并填充beans和uploadFiles
	 * @param items
	 * @param uploadFiles
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	private List<FileUploadBean> buildFileUploadBeans(List<FileItem> items, Map<String, FileItem> uploadFiles) throws UnsupportedEncodingException {
		
		List<FileUploadBean> beans = new ArrayList<>();
		
		//1.遍历FileItem的集合，先得到desc的Map<String, String>，其中
		//键：fieldName(desc1, desc2 ... )；值：desc输入框输入值
		Map<String, String> descs = new HashMap<>();
		for (FileItem item : items) {
			if (item.isFormField()) {
				descs.put(item.getFieldName(), item.getString("UTF-8"));
			}
		}
		
		//2.再遍历FileItem的集合，得到文件域的FileItem对象。
		//每得到一个FileItem对象都创建一个FileUploadBean对象
		//得到fileName，构建filePath，从1的Map中得到当前FileItem对应的那个desc，使用fileName后面的数字去匹配
		for (FileItem item : items) {
			if (!item.isFormField()) {
				//获取文件上传框中name="file1"中的“file1”
				String fieldName = item.getFieldName();
				//获取最后的序号“1”
				String index = fieldName.substring(fieldName.length() - 1);
				//得到该文件对应的描述“desc1”
				String fileDesc = descs.get("desc" + index);
				//获取文件名
			    String fileName = item.getName();
			    fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
			    //获取路径
			    String filePath = getFilePath(fileName);
			    //根据前面得到的字段值，生成FileUploadBean对象
			    FileUploadBean bean = new FileUploadBean(fileName, filePath, fileDesc);
			    beans.add(bean);
			    
			    uploadFiles.put(filePath, item);
			}
		}
		return beans;
	}

	/**
	 * 根据给定的文件名构建一个随机的文件名
	 * 1.文件的扩展名和给定的文件扩展名一致
	 * 2.利用getServletContext().getRealPath()方法获取了绝对路径
	 * 3.利用了Random和当前的系统时间构建了文件名
	 * @param fileName
	 * @return
	 */
	private String getFilePath(String fileName) {
		String extName = fileName.substring(fileName.lastIndexOf("."));
		Random random = new Random();
		int randomNumber = random.nextInt(10000);
		String filePath = getServletContext().getRealPath(FILE_PATH) + "\\" +  System.currentTimeMillis() + randomNumber + extName;
		return filePath;
	}

	/**
	 * 构建ServletFileUpload对象
	 * 从配置文件中读取了部分属性，用于设置约束
	 * 该方法代码来源于文档
	 * @return
	 */
	private ServletFileUpload getServletFileUpload() {
		String exts = FileUploadProperties.getInstance().getProperty("exts");
		String fileMaxSize = FileUploadProperties.getInstance().getProperty("file.max.size");
		String totalFileMaxSize = FileUploadProperties.getInstance().getProperty("total.file.max.size");

		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(1024 * 500);
		File temp = new File(TEMP_DIR);
		factory.setRepository(temp);
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(Integer.parseInt(totalFileMaxSize));
		upload.setFileSizeMax(Integer.parseInt(fileMaxSize));
		return upload;
	}

}
