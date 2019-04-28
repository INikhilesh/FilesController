package com.assignment.cloud;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class FilesController {

	// Section1 - Using DropBox SDk
	private static final String ACCESS_TOKEN = "5Qh5Fqn1XlAAAAAAAAAAEE-tnNyyArr9am78jcVymDao_IS3Ud_xbxWrJ8NM4qbo";
	private String BASE_URL = "https://staging.cloud-elements.com/elements/api-v2/files";
	private String DOWNLOAD_BASE_URL = "https://staging.cloud-elements.com/elements/api-v2/files/links";

	// 1.Get files
	@RequestMapping("/api/getfiles")
	@ResponseBody
	public ListFolderResult get() throws DbxException {
		DbxRequestConfig config = DbxRequestConfig.newBuilder("").build();
		DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
		return client.files().listFolder("");
	}

	// 2.Download files
	@RequestMapping("/api/download/{fileName}")
	@ResponseBody
	public String download(@PathVariable("fileName") String name) throws DbxException, IOException {
		String downloadFileName = "/" + name;
		try {
			OutputStream downloadPath = new FileOutputStream("/Users/N/Downloads/" + downloadFileName);
			DbxRequestConfig config = DbxRequestConfig.newBuilder("").build();
			DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
			client.files().downloadBuilder(downloadFileName).download(downloadPath);
		} catch (Exception e) {
			if (e.getMessage().contains("not_found"))
				;
			{
				return ("File not found");
			}
		}
		return (name + " Downloaded successfully!!");
	}

	// 3.Upload files
	@RequestMapping("/api/upload/{fileName}")
	@ResponseBody
	public String uploadFile(@PathVariable("fileName") String name) throws IOException, DbxException {
		String uploadFileName = "/" + name;
		try {
			InputStream uploadPath = new FileInputStream("/Users/N/Downloads" + uploadFileName);
			DbxRequestConfig config = DbxRequestConfig.newBuilder("").build();
			DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
			client.files().uploadBuilder(uploadFileName).uploadAndFinish(uploadPath);
		} catch (Exception e) {
			return (e.getMessage());

		}
		return (name + " Uploaded successfully!!");
	}

	// Upload files to folder
	@RequestMapping("/api/upload/{folderName}/{fileName}")
	@ResponseBody
	public String uploadFileToFolder(@PathVariable("folderName") String folder, @PathVariable("fileName") String file)
			throws IOException, DbxException {
		String uploadTo = "/" + folder;
		String uploadFile = "/" + file;
		try {
			InputStream Path = new FileInputStream("/Users/N/Downloads" + uploadFile);
			DbxRequestConfig config = DbxRequestConfig.newBuilder("").build();
			DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
			client.files().uploadBuilder(uploadTo + uploadFile).uploadAndFinish(Path);
		} catch (Exception e) {
			return (e.getMessage());
		}
		return (file + " uploaded to " + folder + " succesfully");

	}

	// Section-2 - Enhancement

//    Get files
	@RequestMapping("/apic/getfiles")
	@ResponseBody
	public void v2Get(@RequestParam("filename") String fileName) throws IOException {
		String query = "path=/" + fileName;
		URL obj = new URL(BASE_URL + "?" + query);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestProperty("Authorization",
				"User W8fZ7tCe1FQ6n4ZIkPkB2IEpb+tE/xMpVMjTymV9jlA=, Organization 8a87741fc867d325fd57296761de1ad8");
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code " + responseCode);
	}

	// Download files
	@RequestMapping("/apic/download")
	@ResponseBody
	public String v2download(@RequestParam("fileName") String fileName) throws IOException {
		String query = "path=/" + fileName;
		URL obj = new URL(DOWNLOAD_BASE_URL + "?" + query);
		HttpURLConnection linkCon = (HttpURLConnection) obj.openConnection();
		linkCon.setRequestMethod("GET");
		linkCon.setRequestProperty("Accept", "application/json");
		linkCon.setRequestProperty("Authorization",
				"User W8fZ7tCe1FQ6n4ZIkPkB2IEpb+tE/xMpVMjTymV9jlA=, Organization 8a87741fc867d325fd57296761de1ad8, Element tJhGNMoqr6ZEi6AeKSHYjPHeBWY2/MKXKGWsXRJ6RlE=");
		int responseCode = linkCon.getResponseCode();
		if (responseCode == 200) {

			BufferedReader in = new BufferedReader(new InputStreamReader(linkCon.getInputStream()));
			String inputLine;
			StringBuffer linkResponse = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				linkResponse.append(inputLine);
			}

			Map<String, Object> map = new HashMap<String, Object>();
			ObjectMapper mapper = new ObjectMapper();
			String downloadAbleUrl = "";
			try {
				map = mapper.readValue(linkResponse.toString(), new TypeReference<HashMap<String, Object>>() {
				});
				downloadAbleUrl = map.get("cloudElementsLink").toString();

			} catch (Exception e) {
				e.printStackTrace();
			}
			in.close();

			OutputStream downloadPath = new FileOutputStream("/Users/<user>/Downloads/downloaded/" + fileName);
			DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(downloadPath));
			if (downloadAbleUrl != "") {
				URL downloadObj = new URL(downloadAbleUrl);
				HttpURLConnection downloadCon = (HttpURLConnection) downloadObj.openConnection();
				downloadCon.setRequestMethod("GET");
				InputStream inputStream = downloadCon.getInputStream();
				int bytesRead = -1;
				byte[] buffer = new byte[4096];
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outStream.write(buffer, 0, bytesRead);
				}
//
				outStream.close();
				inputStream.close();

			}
		}
		System.out.println("GET Response Code " + responseCode);
		return (fileName);
	}

	// Upload files
	@RequestMapping("/apic/upload")
	@ResponseBody
	public void v2Upload(@RequestParam("folderName") String folder, @RequestParam("fileName") String file)
			throws IOException {
		String uploadTo = "path=/" + folder;
		String uploadFile = "/" + file;
		int i = 0;
		InputStream uploadPath = new FileInputStream("/Users/<user>/Downloads/" + uploadFile);
		URL obj = new URL(BASE_URL + "?" + uploadTo + uploadFile);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestProperty("Authorization",
				"User W8fZ7tCe1FQ6n4ZIkPkB2IEpb+tE/xMpVMjTymV9jlA=, Organization 8a87741fc867d325fd57296761de1ad8, Element tJhGNMoqr6ZEi6AeKSHYjPHeBWY2/MKXKGWsXRJ6RlE=");
		con.setRequestProperty("Content-Type", "multipart/form-data");

		String content = "";
		while ((i = uploadPath.read()) != -1) {
			content = content + (char) i;
		}
		uploadPath.close();

		con.setDoOutput(true);
		con.getOutputStream().write(content.getBytes("UTF-8"));

		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code " + responseCode);

	}
}
