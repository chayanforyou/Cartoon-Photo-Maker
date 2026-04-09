# AnimeGANv2-ONNX-Android

AnimeGANv2 ONNX inference sample for Android using Jetpack Compose.  
This app converts photos into anime-style images using pre-trained ONNX models.

![Sample](app/src/main/res/drawable/sample.png)

## Features

- 🎨 Three anime style models (Hayao, Shinkai, Face Paint)
- 📱 Modern UI built with Jetpack Compose
- 🔄 Before/After comparison slider
- 💾 Save processed images to gallery
- ⚡ Fast on-device inference with ONNX Runtime

## Models

The app includes three pre-trained ONNX models located in `app/src/main/assets/`:

| Model      | File Name         | Style                |
|------------|-------------------|----------------------|
| Hayao      | `hayao.onnx`      | Hayao Miyazaki style |
| Shinkai    | `shinkai.onnx`    | Makoto Shinkai style |
| Face Paint | `face_paint.onnx` | Face painting style  |

## Usage

1. Launch the app
2. Select a style (Hayao, Shinkai, or Face Paint)
3. Choose an image from your gallery
4. Wait for processing to complete
5. Use the slider to compare before/after
6. Save the result to your gallery

## Project Structure

```
├── MainActivity.kt                    # Main activity
├── processor/
│   ├── CartoonProcessor.kt           # Base processor interface
│   ├── CartoonProcessorNCHW.kt       # NCHW format processor
│   ├── CartoonProcessorNHWC.kt       # NHWC format processor
│   └── ModelType.kt                  # Model type enum
├── ui/
│   ├── components/
│   │   └── BeforeAfterSlider.kt      # Comparison slider component
│   ├── screens/
│   │   ├── HomeScreen.kt             # Home screen with model selection
│   │   └── ResultScreen.kt           # Result screen with comparison
│   └── theme/                        # App theme files
└── utils/
    ├── ImageSaver.kt                 # Image saving utility
    └── PermissionHelper.kt           # Permission handling
```

## Reference

- [TachibanaYoshino/AnimeGANv2](https://github.com/TachibanaYoshino/AnimeGANv2) - AnimeGANv2
- [Kazuhito00/AnimeGANv2-ONNX-Sample](https://github.com/Kazuhito00/AnimeGANv2-ONNX-Sample) - Python
  ONNX inference sample
- [microsoft/onnxruntime](https://github.com/microsoft/onnxruntime) - ONNX Runtime for
  cross-platform

## Author

Chayan Ranasinghe ([@chayanforyou](https://github.com/chayanforyou))

## License

AnimeGANv2-ONNX-Android is under [MIT License](LICENSE).
