package com.hamdiBouallegue.vision.controller;

import com.google.cloud.vision.v1.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.vision.CloudVisionTemplate;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.Feature;


import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.WebDetection;



@RestController
@RequestMapping(value = "/api")
public class VisionController {

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private CloudVisionTemplate cloudVisionTemplate;

	@RequestMapping("/detect-web-detections")
	public void detectWebDetectionsGcs() throws IOException {
		// TODO: 사용할 이미지 파일의 GCS 경로를 지정합니다.
		String gcsPath = "gs://webdetection-bucket/sonocafe.jpeg";
		detectWebDetectionsGcs(gcsPath);
	}

	// 웹 감지 수행 (GCS 경로 사용)
	private void detectWebDetectionsGcs(String gcsPath) throws IOException {
		List<AnnotateImageRequest> requests = new ArrayList<>();

		ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
		Image img = Image.newBuilder().setSource(imgSource).build();
		Feature feat = Feature.newBuilder().setType(Type.WEB_DETECTION).build();
		AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
		requests.add(request);

		// 클라이언트 초기화
		try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
			BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
			List<AnnotateImageResponse> responses = response.getResponsesList();

			for (AnnotateImageResponse res : responses) {
				if (res.hasError()) {
					System.out.format("Error: %s%n", res.getError().getMessage());
					return;
				}

				// 웹 감지 결과 출력
				WebDetection annotation = res.getWebDetection();
				System.out.println("Entity:Id:Score");
				System.out.println("===============");
				for (WebDetection.WebEntity entity : annotation.getWebEntitiesList()) {
					System.out.println(
							entity.getDescription() + " : " + entity.getEntityId() + " : " + entity.getScore());
				}
				for (WebDetection.WebLabel label : annotation.getBestGuessLabelsList()) {
					System.out.format("%nBest guess label: %s", label.getLabel());
				}
				System.out.println("%nPages with matching images: Score%n==");
				for (WebDetection.WebPage page : annotation.getPagesWithMatchingImagesList()) {
					System.out.println(page.getUrl() + " : " + page.getScore());
				}
				System.out.println("%nPages with partially matching images: Score%n==");
				for (WebDetection.WebImage image : annotation.getPartialMatchingImagesList()) {
					System.out.println(image.getUrl() + " : " + image.getScore());
				}
				System.out.println("%nPages with fully matching images: Score%n==");
				for (WebDetection.WebImage image : annotation.getFullMatchingImagesList()) {
					System.out.println(image.getUrl() + " : " + image.getScore());
				}
				System.out.println("%nPages with visually similar images: Score%n==");
				for (WebDetection.WebImage image : annotation.getVisuallySimilarImagesList()) {
					System.out.println(image.getUrl() + " : " + image.getScore());
				}
			}
		}
	}


// 라벨 감지

//	@RequestMapping("/getLabelDetection")
//	public String getLabelDetection() {
//		// 나중에 상대경로로 변경해야함
//		String imageUrl = "https://img.smlounge.co.kr/upload/arena/article/202108/thumb/48940-464417-sampleM.jpg";
//		Resource imageResource = this.resourceLoader.getResource(imageUrl);
//		AnnotateImageResponse response = this.cloudVisionTemplate.analyzeImage(imageResource,
//				Feature.Type.LABEL_DETECTION);
//		return response.getLabelAnnotationsList().toString();
//
//	}


// 랜드마크 감지

//	@GetMapping("/getLandmarkDetection")
//	public String getLandmarkDetection() {
//		// 나중에 상대경로로 변경해야함
//		String imageUrl = "https://www.arenakorea.com/upload/arena/article/202108/thumb/48940-464417-sampleM.jpg";
//		Resource imageResource = this.resourceLoader.getResource(imageUrl);
//		AnnotateImageResponse response = this.cloudVisionTemplate.analyzeImage(imageResource,
//				Feature.Type.LANDMARK_DETECTION);
//
//		return response.getLandmarkAnnotationsList().toString();
//	}

//	@GetMapping("/extractTextFromImage")
//	public String extract() {
//		String imageUrl = "https://cloud.google.com/vision/docs/images/sign_text.png";
//		return this.cloudVisionTemplate.extractTextFromImage(this.resourceLoader.getResource(imageUrl));
//	}



}
