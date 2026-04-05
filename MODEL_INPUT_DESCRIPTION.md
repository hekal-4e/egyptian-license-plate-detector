# License Plate Detection Model - Input Description

## Kaggle Competition Format Summary

### Dataset Structure
- **train_images/**: Training set images
- **test_images/**: Testing set images  
- **train_labels.csv**: Labels for training images
- **train_bbxs/**: Bounding boxes for characters (not all images have them)

### Label Format (train_labels.csv)
- **img_name**: Image filename
- **label**: License plate text (read from RIGHT to LEFT)
  - Mix of English and Arabic characters
  - Editor display may differ from actual reading order

### Bounding Box Format (train_bbxs/*.txt)
- Each .txt file corresponds to one training image
- Format: N rows where N = number of characters in the license plate
- Each row: [xmin, ymin, xmax, ymax] - corner coordinates of character bounding box

## Model Requirements for Android Implementation

### Input Specifications
1. **Image Size**: 640x640 pixels (configurable in YoloDetector.kt)
2. **Color Format**: RGB
3. **Normalization**: 0-255 → 0-1 (NormalizeOp(0f, 255f))
4. **Resize Method**: Bilinear interpolation

### Expected Output Formats
The detector supports multiple YOLO output formats:

#### Format 1: [1, detections, features]
- Common format for custom-trained models
- Features typically: [x, y, w, h, confidence, ...classes]

#### Format 2: [1, features, detections] 
- YOLOv8/v11 standard format
- Features row contains: [x_coords, y_coords, widths, heights, confidences]

#### Format 3: [detections, features]
- Simplified 2D format
- Direct access without batch dimension

### Coordinate Systems
- **Input**: Normalized (0-1) or absolute pixel coordinates
- **Output**: Should be converted to original image coordinates
- **Format**: Center format (cx, cy, w, h) or corner format (x1, y1, x2, y2)

### Preprocessing Pipeline
1. **Resize** to model input size (640x640)
2. **Normalize** pixel values (0-255 → 0-1)
3. **Convert** to TensorImage with FLOAT32 datatype

### Postprocessing Pipeline
1. **Filter** detections by confidence threshold (default: 0.20)
2. **Convert** coordinates back to original image scale
3. **Apply** padding to capture full plate edges
4. **Filter** small detections (min: 50x20 pixels)
5. **Return** largest detection (assumed to be license plate)

### OCR Enhancement
1. **Grayscale conversion** for better text recognition
2. **Contrast enhancement** to improve character clarity
3. **Text normalization** to handle common OCR errors
4. **Digit extraction** focusing on 3-4 digit sequences

## Training Data Considerations
- Egyptian license plates with mixed Arabic/English characters
- Variable lighting conditions
- Different angles and distances
- Character-level bounding boxes available for fine-tuning

## Model Performance Tips
- Use confidence threshold 0.15-0.25 for challenging conditions
- Apply image augmentation during training (rotation, brightness, contrast)
- Consider multi-scale training for different camera distances
- Implement Non-Maximum Suppression (NMS) for multiple detections
