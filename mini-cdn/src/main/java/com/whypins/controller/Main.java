package com.whypins.controller;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.port;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;

import spark.Request;

/**
 * Main class to start the micro service as CDN.
 * 
 * @author Deb 21 November 2017
 *
 */
public class Main {

	private static String BASE_DIRECTORY = System.getProperty("user.dir");

	public static void main(String[] args) {

		port(8092);

		/**
		 * For SSL configuration
		 */

		// String keyStoreLocation = "keystore.jks";
		// String keyStorePassword = "password";
		// secure(keyStoreLocation, keyStorePassword, null, null);

		/**
		 * Filter for all response.
		 */
		before((request, response) -> {
			response.header("Content-Encoding", "gzip");
		});

		/**
		 * For test
		 */
		get("/test/:name", (request, response) -> {
			return "Test data : " + request.params(":name") + " Success/Failed.";
		});

		/**
		 * Will upload the file.
		 */
		post("/cdn/upload/:name", (request, response) -> {
			Path path = Paths.get(BASE_DIRECTORY + File.pathSeparator + request.params(":name"));
			if (!Files.exists(path)) {
				Files.createFile(path);
			}
			writeToFile(request, path);
			return Optional.ofNullable("File uploaded.");
		});

		/**
		 * Will read and stream the file name
		 */
		get("/cdn/read/:name", (request, response) -> {
			Path path = Paths.get(BASE_DIRECTORY + File.pathSeparator + request.params(":name"));
			List<String> data = readFile(path);
			StringBuilder stringBuilder = new StringBuilder();
			data.forEach(stringBuilder::append);
			return Optional.ofNullable(stringBuilder.toString());
		});

		/**
		 * Will delete the file
		 */
		get("/cdn/delete/:name", (request, response) -> {
			Path path = Paths.get(BASE_DIRECTORY + File.pathSeparator + request.params(":name"));
			Files.delete(path);
			return Optional.ofNullable("Deleted.");
		});
	}

	private static void writeToFile(Request request, Path path) throws IOException, ServletException {
		request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
		try (InputStream is = request.raw().getPart("uploaded_file").getInputStream()) {
			Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<String> readFile(Path path) {
		List<String> list = new ArrayList<>();
		try (BufferedReader br = Files.newBufferedReader(path)) {
			list = br.lines().collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
}
