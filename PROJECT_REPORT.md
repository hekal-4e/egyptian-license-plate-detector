# Project Status Report: Egyptian License Plate Detector
**Date:** April 6, 2026
**Project Type:** Android Graduation Project
**Repository:** [egyptian-license-plate-detector](https://github.com/hekal-4e/egyptian-license-plate-detector)

---

## 1. Project Overview
The **Egyptian License Plate Detector** is a specialized Android application designed to detect, crop, and read Egyptian vehicle license plates using the device's camera. The app leverages advanced Machine Learning (ML) models and Optical Character Recognition (OCR) to process images in real-time, providing immediate feedback and archiving the read plates into a local database for future reference.

Given the unique layout of Egyptian license plates—which consistently feature a specific set of Arabic letters alongside numbers—the app is fine-tuned to recognize this exact configuration, reducing noise and false positives.

---

## 2. Core Technologies & Architecture

### **Architecture**
The project strictly follows **Clean Architecture** patterns combined with **MVVM (Model-View-ViewModel)**. 
- **UI Layer:** Built entirely using **Jetpack Compose**, implementing a reactive, state-driven UI.
- **Dependency Injection:** Handled by **Hilt** to keep components decoupled (though standard ViewModel factories are currently mapped).
- **Data Layer:** Uses **Room Database** for local, offline storage of detected plates.
- **Asynchronous Execution:** Utilizes **Kotlin Coroutines and Flow** to handle heavy ML/OCR processing off the main UI thread.

### **Machine Learning & OCR Pipeline**
The detection pipeline is split into two distinct models to maximize accuracy:
1. **YOLO (You Only Look Once):**
   - **File:** `best.tflite`
   - **Purpose:** Acts as an Object Detection model tailored to find the exact bounding box of a license plate within a larger image frame.
   - **Action:** Locates the plate, allowing the app to aggressively crop the image. This removes background noise (cars, streets, people) so the OCR engine only sees the license plate.
2. **Tesseract OCR:**
   - **File:** `ara.traineddata` (Arabic language pack)
   - **Purpose:** Extracts the actual text (letters and numbers) from the cropped plate image.
   - **Configuration:** Strictly whitelisted to only output numbers (`0-9`, `٠-٩`) and the specific Arabic letters used in Egyptian plates (`أبجدرسصطعفقكلمنهوي`).

---

## 3. How the Application Works (Step-by-Step)
1. **Image Capture:** 
   The user opens the camera screen (powered by **CameraX**). A neon overlay guides the user to position the license plate.
2. **Plate Detection (YOLO):** 
   Once a photo is captured, the `LicensePlateAnalyzer` passes the `Bitmap` to the YOLO detector. If no plate is found, the system gracefully halts and alerts the user: *"لم يتم العثور على لوحة (حاول الاقتراب)"* (No plate found, try getting closer).
3. **Cropping & Preprocessing:** 
   The bounding box returned by YOLO is used to crop the image. The image is then passed through an aggressive pipeline: scaled up to *1200x400*, converted to Grayscale, and heavily contrasted. This makes the license plate text pop out for the OCR engine.
4. **Character Recognition (Tesseract):** 
   The prepared crop is scanned. The Tesseract engine attempts to match the visual data against its Arabic/numeric dictionary. 
5. **Text Filtering & Formatting:** 
   The raw OCR text is filtered. Any non-Egyptian plate character is discarded. Arabic numbers are mapped to English numbers for database consistency. Finally, it constructs a clean string resembling authentic Egyptian plate formatting (e.g., "أ ب ج 1 2 3").
6. **User Confirmation & Storage:**
   A Bottom Sheet (`PlateResultBottomSheet`) slides up, showing the cropped picture and the final text. The user is allowed to manually correct the text if the OCR missed a letter, then save it. The record is permanently saved into the **Room Database** and appears on the History Screen.

---

## 4. Current Results & Recent Milestones

The project has reached a highly stable and functional milestone:
* **Successful GitHub Deployment:** The entire source code has been successfully pushed to the main branch.
* **Tesseract Engine Rescue:** A major bug was resolved where the Tesseract character whitelist had become corrupted due to character encoding issues, resulting in Tesseract failing to output Arabic strings. It has been successfully repaired and optimized for Egyptian letters.
* **Localized Error Handling:** Hard-coded English error messages inside the detector class were localized to Arabic, vastly improving the UX.
* **Memory & Storage Validated:** The application’s raw folder size (~500MB) was audited. It was determined that the majority of space belongs to temporary build files (~377MB) and the `best.tflite` model (~38MB). Build files are structurally ignored by `.gitignore`, maintaining a clean, lightweight remote repository footprint.

## 5. Next Steps & Recommendations
* **Accuracy Tuning:** Depending on field tests, the Tesseract contrast parameters (Scale `2.5f`, Translate `-120f`) may need slight adjustments for plates captured in nighttime or low-light scenarios.
* **Real-time Inference (Optional):** Currently, the app captures a photo and then analyzes it. Transitioning the ML pipeline from `ImageCapture` to CameraX `ImageAnalysis` could allow for real-time live detection without pressing a capture button.