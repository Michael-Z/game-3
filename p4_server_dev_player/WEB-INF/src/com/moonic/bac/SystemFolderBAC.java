package com.moonic.bac;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import server.common.Tools;
import server.config.ServerConfig;

import com.ehc.dbc.BaseActCtrl;

public class SystemFolderBAC extends BaseActCtrl {
	private static SystemFolderBAC instance;

	public SystemFolderBAC() {
		super.setTbName("");
		setDataBase(ServerConfig.getDataBase());
	}

	public static SystemFolderBAC getInstance() {
		if (instance == null) {
			instance = new SystemFolderBAC();
		}
		return instance;
	}

	public void opFile(PageContext pageContext) {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		String path = request.getParameter("path");
		if (path == null) {
			return;
		}
		File file = new File(path);
		if (file.exists()) {
			try {
				if (file.isFile()) {
					file.delete();
				} else if (file.isDirectory()) {
					if (file.listFiles() == null || file.listFiles().length == 0) {
						file.delete();
					} else {
						// ɾ��Ŀ¼�µ��ļ�
						File[] subfiles = file.listFiles();
						for (int i = 0; subfiles != null && i < subfiles.length; i++) {
							if (subfiles[i].isFile()) {
								subfiles[i].delete();
							} else if (subfiles[i].isDirectory()) {
								if (subfiles[i].listFiles() == null || subfiles[i].listFiles().length == 0) {
									subfiles[i].delete();
								}
							}
						}
					}
				}
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
	}

	public void zipFolderAndDownload(HttpServletResponse response, String path) {
		Vector fileVC = new Vector();
		File file = new File(path);
		if (file.exists()) {
			Tools.getFolderFiles(file, fileVC, null, null);
			if (fileVC.size() > 0) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ZipOutputStream zos = new ZipOutputStream(baos);
				try {
					for (int i = 0; i < fileVC.size(); i++) {
						File subFile = (File) fileVC.elementAt(i);
						String entryName = subFile.getAbsolutePath().substring(path.length() + 1);
						entryName = entryName.replace("\\", "/");
						byte[] fileBytes = Tools.getBytesFromFile(subFile);
						if (fileBytes != null && fileBytes.length > 0) {
							ZipEntry zipEntry = new ZipEntry(entryName);
							zipEntry.setMethod(ZipEntry.DEFLATED);

							// ��ѹ�����㷨
							/*
							 * zipEntry.setMethod(ZipEntry.STORED); CRC32 crc =
							 * new CRC32(); crc.update(fileBytes);
							 * zipEntry.setCrc(crc.getValue());
							 */

							zipEntry.setSize(fileBytes.length);
							zos.putNextEntry(zipEntry);
							zos.write(fileBytes);
							zos.closeEntry();
						}
					}
					zos.close();
					byte[] outbytes = baos.toByteArray();

					response.reset();
					response.setContentType("application/octet-stream");
					response.setContentLength(outbytes.length);
					response.setHeader("Content-disposition",new String(("attachment;filename=" + file.getName() + ".zip").getBytes(), "ISO-8859-1"));
					OutputStream os = response.getOutputStream();
					os.write(outbytes);
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void getFile(PageContext pageContext) {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();

		String path = request.getParameter("path");

		if (path == null) {
			return;
		}
		File file = new File(path);
		if (file.exists()) {
			try {
				byte[] fileBytes = Tools.getBytesFromFile(file);
				// System.out.println("�ļ�"+path+" ����="+fileBytes.length);
				response.reset();
				response.setContentType("application/octet-stream");
				response.setContentLength(fileBytes.length);
				response.setHeader("Content-disposition", new String(("attachment;filename=" + file.getName()).getBytes(),"ISO-8859-1"));
				OutputStream os = response.getOutputStream();
				os.write(fileBytes);
				os.close();
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}

	}

	public File[] getSubFiles(File folder) {
		File[] subFiles = folder.listFiles();
		if (subFiles != null) {
			return sort(subFiles);
		} else {
			return null;
		}
	}

	public static File[] sort(File[] files) {
		if (files == null) {
			return null;
		}
		int n = files.length;
		int pointer; // ���д����λ��
		int dataLength = n / 2; // ��ʼ���ϼ������
		while (dataLength != 0) {// �����Կɽ��зָ�
									// �Ը������Ͻ��д���
			for (int i = dataLength; i < n; i++) {
				File temp = null;

				temp = files[i]; // �ݴ�Data[i]��ֵ,������ֵʱ��

				pointer = i - dataLength; // ������д����λ��

				while (files[i].getName().compareTo(files[pointer].getName()) < 0 && pointer >= 0 && pointer < n) {
					files[pointer + dataLength] = files[pointer];
					// ������һ�������д����λ��
					pointer = pointer - dataLength;
					if (pointer < 0 || pointer >= n) {
						break;
					}
				}
				// �����Ľ���
				files[pointer + dataLength] = temp;
			}
			dataLength = dataLength / 2; // �����´ηָ�ļ������
		}
		return files;
	}
}
