import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * A utility that downloads a file from a URL.
 * 
 * @author www.codejava.net
 *
 */
public class HttpDownloadUtility {
	private final int BUFFER_SIZE = 4096;
	private static HttpURLConnection httpConn;
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36";

	/**
	 * Downloads a file from a URL
	 * 
	 * @param fileURL
	 *            HTTP URL of the file to be downloaded
	 * @param saveDir
	 *            path of the directory to save the file
	 * @throws IOException
	 */
	private List<String> cookies;

	private String getPageContent(String url) throws Exception {

		URL obj = new URL(url);
		httpConn = (HttpURLConnection) obj.openConnection();

		// default is GET
		httpConn.setRequestMethod("GET");

		httpConn.setUseCaches(false);

		// act like a browser
		httpConn.setRequestProperty("User-Agent", USER_AGENT);
		httpConn.setRequestProperty("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		httpConn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		if (cookies != null) {
			for (String cookie : cookies) {
				String[] strArray1 = cookie.split(";");
				String c = strArray1[0];
				httpConn.addRequestProperty("Cookie", c);
			}
		}
		int responseCode = httpConn.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				httpConn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		Map<String, List<String>> m = httpConn.getHeaderFields();

		for (Map.Entry<String, List<String>> entry : m.entrySet()) {
			System.out.println(entry.getKey() + " / "
					+ entry.getValue().toString());
		}

		// Get the response cookies
		List<String> cookies = httpConn.getHeaderFields().get("Set-Cookie");
		if (cookies != null) {
			setCookies(cookies);
		}

		return response.toString();

	}

	public void setCookies(List<String> cookies) {
		this.cookies = cookies;
	}

	public void downloadFile(String refererUrl, String fileURL, String saveDir)
			throws IOException {
		URL url = new URL(fileURL);
		httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setRequestProperty("Host", "www.tcfsh.tc.edu.tw");
		httpConn.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0");
		httpConn.setRequestProperty("Accept",
				"image/png,image/*;q=0.8,*/*;q=0.5");
		httpConn.setRequestProperty("Accept-Language",
				"zh-tw,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		httpConn.setRequestProperty("Accept-Encoding", "gzip, deflate");
		httpConn.setRequestProperty("Cache-Control", "max-age=0");
		httpConn.setRequestProperty("Referer", refererUrl);

		httpConn.setRequestProperty("Connection", "keep-alive");
		httpConn.setRequestProperty("If-Modified-Since",
				"Thu, 08 Jan 2009 10:13:22 GMT");

		if (this.cookies != null) {
			for (String cookie : this.cookies) {
				String[] strArray1 = cookie.split(";");
				String c = strArray1[0];
				httpConn.addRequestProperty("Cookie", c);
			}
		}

		int responseCode = httpConn.getResponseCode();

		BufferedReader in = new BufferedReader(new InputStreamReader(
				httpConn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		Map<String, List<String>> m = httpConn.getHeaderFields();

		for (Map.Entry<String, List<String>> entry : m.entrySet()) {
			System.out.println("Header: " + entry.getKey() + " / "
					+ entry.getValue().toString());
		}

		// always check HTTP response code first
		if (responseCode == HttpURLConnection.HTTP_OK) {
			String fileName = "";
			String disposition = httpConn.getHeaderField("Content-Disposition");
			String contentType = httpConn.getContentType();
			int contentLength = httpConn.getContentLength();

			if (disposition != null) {
				// extracts file name from header field
				int index = disposition.indexOf("filename=");
				if (index > 0) {
					fileName = disposition.substring(index + 9,
							disposition.length());
					fileName = fileName.trim();
				}
			} else {
				// extracts file name from URL
				fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
						fileURL.length());
			}

			System.out.println("Content-Type = " + contentType);
			System.out.println("Content-Disposition = " + disposition);
			System.out.println("Content-Length = " + contentLength);
			System.out.println("fileName = " + fileName);

			// opens input stream from the HTTP connection
			InputStream inputStream = httpConn.getInputStream();
			String saveFilePath = saveDir + File.separator + fileName;

			// opens an output stream to save into file
			FileOutputStream outputStream = new FileOutputStream(saveFilePath);

			int bytesRead = -1;
			byte[] buffer = new byte[BUFFER_SIZE];
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			outputStream.close();
			inputStream.close();

			System.out.println("File downloaded");
		} else {
			System.out
					.println("No file to download. Server replied HTTP code: "
							+ responseCode);
		}
		httpConn.disconnect();
	}

	public static void main(String[] args) {
		String refererUrl = "http://www.tcfsh.tc.edu.tw/news/u_news_v2.asp?id={F846CE17-E78D-4182-A023-B7A1FA203E1E}&newsid=8350";
		String fileURL = "http://www.tcfsh.tc.edu.tw/ylbin/filedown.asp?filename=filename1";
		String saveDir = "E:/Download";
		try {
			HttpDownloadUtility httpDownloadUtility = new HttpDownloadUtility();
			try {
				httpDownloadUtility.getPageContent(refererUrl);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			httpDownloadUtility.downloadFile(refererUrl, fileURL, saveDir);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}