#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include "logs.h"
#include "detector.h"
#include "skinseg.h"
#include "rppg.h"
#include "onnxruntime_inference.h"
#include "onnxruntime_bloodpresure.h"
#include <jni.h>

// 변수 선언을 한 후에 값을 비교해야함.
// * debug 모드는 괜찮은데, release 모드에서 에러
// AndroidBitmap_getInfo(env, obj_bitmap, &bitmapInfo) == ANDROID_BITMAP_RESULT_SUCCESS  -> err

bool BitmapToMatrix(JNIEnv *env, jobject obj_bitmap, cv::Mat &mat_bmp) {
    /* https://jamssoft.tistory.com/113
     **/
    bool ret = JNI_FALSE;
//    void *pBmp = NULL;               // receive the pixel data
    uint8_t* pBmp = NULL;
    AndroidBitmapInfo bitmapInfo;    // receive the bitmap info

    auto getInfo = AndroidBitmap_getInfo(env, obj_bitmap, &bitmapInfo);

    assert(obj_bitmap);
    assert( getInfo == ANDROID_BITMAP_RESULT_SUCCESS);  // Get bitmap info
//    assert( bitmapInfo.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
//            bitmapInfo.format == ANDROID_BITMAP_FORMAT_RGB_565);

    auto lock = AndroidBitmap_lockPixels(env, obj_bitmap, (void**)&pBmp);

    assert( lock == ANDROID_BITMAP_RESULT_SUCCESS );     // Get pixel data (lock memory block)
    assert( pBmp );

    if (bitmapInfo.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        cv::Mat _tmp(bitmapInfo.height, bitmapInfo.width, CV_8UC4, pBmp);    // Establish temporary mat
        _tmp.copyTo(mat_bmp);                                                  // Copy to target matrix
        ret = JNI_TRUE;
    }

    if (pBmp) {
        AndroidBitmap_unlockPixels(env, obj_bitmap);            // Java에서 쓸 수 있도록 unlock
    }

    return ret;
}

jobject Mat2Bitmap(JNIEnv * env, cv::Mat & src, bool needPremultiplyAlpha, jobject bitmap_config){

    jclass java_bitmap_class = (jclass)env->FindClass("android/graphics/Bitmap");
    jmethodID mid = env->GetStaticMethodID(java_bitmap_class,
                                           "createBitmap", "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

    jobject bitmap = env->CallStaticObjectMethod(java_bitmap_class,
                                                 mid, src.size().width, src.size().height, bitmap_config);
    AndroidBitmapInfo  info;
    void* pixels = 0;

    try {
        //validate
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);

        CV_Assert(src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4);

        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);

        CV_Assert(pixels);

        //type mat
        if(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888){

            cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(src.type() == CV_8UC1){

                cvtColor(src, tmp, cv::COLOR_GRAY2RGBA);
            } else if(src.type() == CV_8UC3){
//                cvtColor(src, tmp, cv::COLOR_RGB2RGBA);
                cvtColor(src, tmp, cv::COLOR_RGB2BGRA);
            } else if(src.type() == CV_8UC4){

                if(needPremultiplyAlpha){
                    cvtColor(src, tmp, cv::COLOR_RGBA2mRGBA);
                }else{
                    src.copyTo(tmp);
                }
            }

        } else{
            cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
            if(src.type() == CV_8UC1){
                cvtColor(src, tmp, cv::COLOR_GRAY2BGR565);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, cv::COLOR_RGB2BGR565);
            } else if(src.type() == CV_8UC4){
                cvtColor(src, tmp, cv::COLOR_RGBA2BGR565);
            }
        }

        AndroidBitmap_unlockPixels(env, bitmap);

        return bitmap;
    }
//    catch(cv::Exception e){
//        AndroidBitmap_unlockPixels(env, bitmap);
//        jclass je = env->FindClass("org/opencv/core/CvException");
//        if(!je) je = env->FindClass("java/lang/Exception");
//        env->ThrowNew(je, e.what());
//        return bitmap;
//    }
    catch (...){
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return bitmap;
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_kr_smu_prlab_Prlab_getSkinDataArray(JNIEnv *env, jobject thiz, jlong self_addr,
                                         jobject input) {
    // TODO: implement getSkinDataArray()
    cv::Mat pixel_data;
    bool ret = BitmapToMatrix(env, input, pixel_data);

    if (ret != JNI_TRUE) {
        return 0;
    }
    else{
        cv::Mat bgr;

        std::vector<float> output(3);
        cv::cvtColor(pixel_data, bgr, cv::COLOR_RGBA2BGR);

        getSkinArray(bgr, output);

        // 자바 자료형 및 메소드 생성
        jclass vectorClass = env->FindClass("java/util/Vector");
        jclass floatClass = env->FindClass("java/lang/Float");
        jmethodID initMethodID = env->GetMethodID(vectorClass, "<init>", "()V");
        jmethodID addMethodID = env->GetMethodID(vectorClass, "add", "(Ljava/lang/Object;)Z");
        // 자바용 벡터 생성
        jobject jvec = env->NewObject(vectorClass, initMethodID);
        for (float f : output) {
            jmethodID floatConstructorID = env->GetMethodID(floatClass, "<init>", "(F)V");
            jobject floatValue = env->NewObject(floatClass, floatConstructorID, f);
            env->CallBooleanMethod(jvec, addMethodID, floatValue);
        }
        env->DeleteLocalRef(vectorClass);
        env->DeleteLocalRef(floatClass);

        return jvec;
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_kr_smu_prlab_Prlab_getSkinArray(JNIEnv *env, jobject thiz, jlong self_addr) {
    // TODO: implement getSkinDataArray()

    if (self_addr != 0){

        // remotePPG 수행
        auto *self = (RemotePPG *) self_addr;

//        std::vector<float> output = self->resultRGBVector;

        // 자바 자료형 및 메소드 생성
        jclass vectorClass = env->FindClass("java/util/Vector");
        jclass floatClass = env->FindClass("java/lang/Float");
        jmethodID initMethodID = env->GetMethodID(vectorClass, "<init>", "()V");
        jmethodID addMethodID = env->GetMethodID(vectorClass, "add", "(Ljava/lang/Object;)Z");
        // 자바용 벡터 생성
        jobject jvec = env->NewObject(vectorClass, initMethodID);
        for (float f : self->resultRGBVector) {
            jmethodID floatConstructorID = env->GetMethodID(floatClass, "<init>", "(F)V");
            jobject floatValue = env->NewObject(floatClass, floatConstructorID, f);
            env->CallBooleanMethod(jvec, addMethodID, floatValue);
        }
        env->DeleteLocalRef(vectorClass);
        env->DeleteLocalRef(floatClass);

        return jvec;
    }else{
        return 0;
    }
}

extern "C"
JNIEXPORT jlong JNICALL
Java_kr_smu_prlab_Prlab_nativeCreateObject(JNIEnv *env, jobject thiz, jfloat fps, jfloat duration,
                                           jint min_bpm, jint max_bpm, jfloat bpm_update_period, jint hrv_time) {
    // TODO: implement nativeCreateObject()
    // remotePPG 객체 생성
    auto *self = new RemotePPG(
            fps,
            duration,
            min_bpm, max_bpm,
            bpm_update_period,
            hrv_time
            );
    return (jlong) self;
}


extern "C"
JNIEXPORT void JNICALL
Java_kr_smu_prlab_Prlab_nativeProcessFrameWithSkinArray(JNIEnv *env, jobject thiz,
                                                        jlong m_native_obj,
                                                        jfloatArray skin_data_array, jlong time) {
    // TODO: implement nativeProcessFrameWithSkinArray()

    if (m_native_obj != 0){
        jsize size = env->GetArrayLength(skin_data_array);

        std::vector<float> input(size);
        env -> GetFloatArrayRegion(skin_data_array, 0, size, &input[0]);

        // remotePPG 수행
        auto *self = (RemotePPG *) m_native_obj;
        self->processFrame(input, time);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_kr_smu_prlab_Prlab_nativeProcessFrame(JNIEnv *env, jobject thiz, jlong m_native_obj,
                                           jobject image, jlong time) {
    // TODO: implement nativeProcessFrame()
    cv::Mat mat_bitmap;
    bool ret = BitmapToMatrix(env, image, mat_bitmap);  // Bitmap to cv::Mat

    if (!ret) {
        return ;
    }

    if (m_native_obj != 0) {
        cv::Mat bgr;
        cv::cvtColor(mat_bitmap, bgr, cv::COLOR_RGBA2BGR);
        // remotePPG 수행
        auto *self = (RemotePPG *) m_native_obj;
        self->processFrame(bgr, time);
    }
}

//extern "C"
//JNIEXPORT jobject JNICALL
//Java_kr_smu_prlab_Prlab_nativeGetSignal(JNIEnv *env, jobject thiz, jlong m_native_obj) {
//    // TODO: implement nativeGetSignal()
//    if (m_native_obj != 0) {
//        auto *rppg = (RemotePPG *) m_native_obj;
//        std::vector<float> vec = rppg->waveform;
//
//        // 자바 자료형 및 메소드 생성
//        jclass vectorClass = env->FindClass("java/util/Vector");
//        jclass floatClass = env->FindClass("java/lang/Float");
//        jmethodID initMethodID = env->GetMethodID(vectorClass, "<init>", "()V");
//        jmethodID addMethodID = env->GetMethodID(vectorClass, "add", "(Ljava/lang/Object;)Z");
//
//        // 자바용 벡터 생성
//        jobject jvec = env->NewObject(vectorClass, initMethodID);
//        for (float f : vec) {
//            jmethodID floatConstructorID = env->GetMethodID(floatClass, "<init>", "(F)V");
//            jobject floatValue = env->NewObject(floatClass, floatConstructorID, f);
//            env->CallBooleanMethod(jvec, addMethodID, floatValue);
//        }
//
//        env->DeleteLocalRef(vectorClass);
//        env->DeleteLocalRef(floatClass);
//
//        return jvec;
//    }
//    return 0;
//}
extern "C"
JNIEXPORT jint JNICALL
Java_kr_smu_prlab_Prlab_nativeGetHeartrate(JNIEnv *env, jobject thiz, jlong m_native_obj) {
    // TODO: implement nativeGetHeartrate()
    if (m_native_obj != 0) {
        auto *rppg = (RemotePPG *) m_native_obj;
        jint heartrate = rppg->avgBpm;
        return heartrate;
    }
    return 0;
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_kr_smu_prlab_Prlab_nativeGetConfidence(JNIEnv *env, jobject thiz, jlong m_native_obj) {
    // TODO: implement nativeGetHeartrate()
    if (m_native_obj != 0) {
        auto *rppg = (RemotePPG *) m_native_obj;
        jfloat confidence = rppg->resultConfidence;
        return confidence;
    }
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_kr_smu_prlab_Prlab_nativeGetFps(JNIEnv *env, jobject thiz, jlong m_native_obj) {
    // TODO: implement nativeGetFps()
    if (m_native_obj != 0) {
        auto *rppg = (RemotePPG *) m_native_obj;
        jint fps = round(rppg->fps);
        return fps;
    }
    return 0;
}
extern "C"
JNIEXPORT void JNICALL
Java_kr_smu_prlab_Prlab_nativeReset(JNIEnv *env, jobject thiz, jlong m_native_obj) {
    // TODO: implement nativeReset()
    if (m_native_obj != 0) {
        auto *rppg = (RemotePPG *) m_native_obj;

        rppg->reset();
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_kr_smu_prlab_Prlab_nativeDestroyObject(JNIEnv *env, jobject thiz, jlong m_native_obj) {
    // TODO: implement nativeDestroyObject()
    if (m_native_obj != 0) {
        auto *rppg = (RemotePPG *) m_native_obj;
//        LOGD("deleted c++ object");
        delete rppg;
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_smuprlab_camera_wrapper_Detector_newSelf(JNIEnv *env, jobject thiz,
                                                          jstring model_path,
                                                          jstring model_weight_path, jint in_width,
                                                          jint in_height, jfloat score_thresh,
                                                          jfloat iou_thresh, jboolean use_tracker) {
    // TODO: implement newSelf()
    const char *model_path_char = env->GetStringUTFChars(model_path, 0);
    const char *model_weight_path_char = env->GetStringUTFChars(model_weight_path, 0);

    auto *self = new FaceDetector(model_path_char, model_weight_path_char, in_width, in_height, score_thresh, iou_thresh, use_tracker);
    return (jlong) self;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_smuprlab_camera_wrapper_Detector_deleteSelf(JNIEnv *env, jobject thiz,
                                                             jlong self_addr) {
    // TODO: implement deleteSelf()
    if (self_addr != 0) {
        auto *self = (FaceDetector *) self_addr;
        delete self;
    }
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_smuprlab_camera_wrapper_Detector_run(JNIEnv *env, jobject thiz, jlong self_addr,
                                                      jobject input) {
    // TODO: implement run()
    if (self_addr != 0) {
        auto *self = (FaceDetector *) self_addr;

        cv::Mat pixel_data ;
        bool ret = BitmapToMatrix(env, input, pixel_data);

        if (ret != JNI_TRUE){
            return 0;
        }else{
            cv::Mat bgr;
            cv::cvtColor(pixel_data, bgr, cv::COLOR_RGBA2BGR);
            cv::Rect2i result = self->detectLargestFace(bgr);

            // 자바 자료형 및 메소드 생성
            jclass rectClass = env->FindClass("android/graphics/Rect");
            jmethodID rectCtorID = env->GetMethodID(rectClass, "<init>", "(IIII)V");
            jobject jRect = env->NewObject(rectClass, rectCtorID, result.x, result.y, result.x+result.width, result.y+result.height);
            return jRect;
        }

    }

    return 0;
}
extern "C"
JNIEXPORT jobject JNICALL
Java_kr_smu_prlab_Prlab_nateGetBandpassedSignal(JNIEnv *env, jobject thiz, jlong m_native_obj) {
    // TODO: implement nateGetBandpassedSignal()
    if (m_native_obj != 0) {
        auto *rppg = (RemotePPG *) m_native_obj;
        std::vector<float> vec = rppg->vector_bandpassed;

        if(vec.size() > (int)(rppg->fps * 5.0 * 0.9)){
            // 자바 자료형 및 메소드 생성
            jclass vectorClass = env->FindClass("java/util/Vector");
            jclass floatClass = env->FindClass("java/lang/Float");
            jmethodID initMethodID = env->GetMethodID(vectorClass, "<init>", "()V");
            jmethodID addMethodID = env->GetMethodID(vectorClass, "add", "(Ljava/lang/Object;)Z");

            // 자바용 벡터 생성
            jobject jvec = env->NewObject(vectorClass, initMethodID);
            for (float f : vec) {
                jmethodID floatConstructorID = env->GetMethodID(floatClass, "<init>", "(F)V");
                jobject floatValue = env->NewObject(floatClass, floatConstructorID, f);
                env->CallBooleanMethod(jvec, addMethodID, floatValue);
            }

            env->DeleteLocalRef(vectorClass);
            env->DeleteLocalRef(floatClass);

            return jvec;
        }
        else{
            return 0;
//            return nullptr;
        }
    }
    return 0;
//    return nullptr;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_kr_smu_prlab_Prlab_nateGetHrvBandpassedSignal(JNIEnv *env, jobject thiz, jlong m_native_obj) {
    // TODO: implement nateGetBandpassedSignal()
    if (m_native_obj != 0) {
        auto *rppg = (RemotePPG *) m_native_obj;
        std::vector<float> vec = rppg->hrv_vector_bandpassed;

        // 자바 자료형 및 메소드 생성
        jclass vectorClass = env->FindClass("java/util/Vector");
        jclass floatClass = env->FindClass("java/lang/Float");
        jmethodID initMethodID = env->GetMethodID(vectorClass, "<init>", "()V");
        jmethodID addMethodID = env->GetMethodID(vectorClass, "add", "(Ljava/lang/Object;)Z");

        // 자바용 벡터 생성
        jobject jvec = env->NewObject(vectorClass, initMethodID);
        for (float f : vec) {
            jmethodID floatConstructorID = env->GetMethodID(floatClass, "<init>", "(F)V");
            jobject floatValue = env->NewObject(floatClass, floatConstructorID, f);
            env->CallBooleanMethod(jvec, addMethodID, floatValue);
        }

        env->DeleteLocalRef(vectorClass);
        env->DeleteLocalRef(floatClass);

        return jvec;
    }
    return 0;
}
extern "C"
JNIEXPORT jint JNICALL
Java_kr_smu_prlab_Prlab_nativeGetBPStress(JNIEnv *env, jobject thiz, jlong m_native_obj) {
    // TODO: implement nativeGetBPStress()
    if (m_native_obj != 0) {
        auto *rppg = (RemotePPG *) m_native_obj;
//        int sys = rppg->SYS;
//        int dia = rppg->DIA;
        jint stress = rppg->stress;

        return stress;

//        // 자바 자료형 및 메소드 생성
//        jclass vectorClass = env->FindClass("java/util/Vector");
//        jclass floatClass = env->FindClass("java/lang/Float");
//        jmethodID initMethodID = env->GetMethodID(vectorClass, "<init>", "()V");
//        jmethodID addMethodID = env->GetMethodID(vectorClass, "add", "(Ljava/lang/Object;)Z");
//
//        // 자바용 벡터 생성
//        jobject jvec = env->NewObject(vectorClass, initMethodID);
//        for (float f : vec) {
//            jmethodID floatConstructorID = env->GetMethodID(floatClass, "<init>", "(F)V");
//            jobject floatValue = env->NewObject(floatClass, floatConstructorID, f);
//            env->CallBooleanMethod(jvec, addMethodID, floatValue);
//        }
//
//        env->DeleteLocalRef(vectorClass);
//        env->DeleteLocalRef(floatClass);
//
//        return jvec;
    }
    return 0;
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_kr_smu_prlab_Prlab_nativeGetHrvConfidence(JNIEnv *env, jobject thiz, jlong m_native_obj) {
    // TODO: implement nativeGetHrvConfidence()
    if (m_native_obj != 0) {
        auto *rppg = (RemotePPG *) m_native_obj;
        jfloat confidence = rppg->hrvResultConfidence;
        return confidence;
    }
    return 0;
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_kr_smu_prlab_Prlab_nativeGetVLF(JNIEnv *env, jobject thiz, jlong m_native_obj) {
    // TODO: implement nativeGetVLF()
    if (m_native_obj != 0) {
        auto *rppg = (RemotePPG *) m_native_obj;
        jfloat vlf = rppg->vlf;
        return vlf;
    }
    return 0;
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_kr_smu_prlab_Prlab_nativeGetLF(JNIEnv *env, jobject thiz, jlong m_native_obj) {
    // TODO: implement nativeGetLF()
    if (m_native_obj != 0) {
        auto *rppg = (RemotePPG *) m_native_obj;
        jfloat lf = rppg->lf;
        return lf;
    }
    return 0;
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_kr_smu_prlab_Prlab_nativeGetHF(JNIEnv *env, jobject thiz, jlong m_native_obj) {
    // TODO: implement nativeGetHF()
    if (m_native_obj != 0) {
        auto *rppg = (RemotePPG *) m_native_obj;
        jfloat hf = rppg->hf;
        return hf;
    }
    return 0;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_kr_smu_prlab_Prlab_newSelf(JNIEnv *env, jobject thiz, jstring model_path,
                                jint img_height, jint img_width) {
    // TODO: implement newSelf()
    std::unique_ptr<Ort::Env> environment(new Ort::Env(ORT_LOGGING_LEVEL_VERBOSE, "ONNX"));

    const char *model_path_ch = env->GetStringUTFChars(model_path, nullptr);
//    const char *label_file_path_ch = env->GetStringUTFChars(label_file_path, 0);
//Inference *self = new Inference(environment, model_path_ch, label_file_path_ch, img_height, img_width);
    Inference *self = new Inference(environment, model_path_ch, img_height, img_width);
    return (jlong) self;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_kr_smu_prlab_Prlab_newSelfByteArray(JNIEnv *env, jobject thiz, jbyteArray model_byte_data,
                                         jint model_size, jint img_height, jint img_width) {
    // TODO: implement newSelfByteArray()
    std::unique_ptr<Ort::Env> environment(new Ort::Env(ORT_LOGGING_LEVEL_VERBOSE, "ONNX"));

    Inference *self = new Inference(
            environment,
            (uint8_t*) model_byte_data,
            model_size,
            img_height, img_width
    );
    return (jlong) self;
}

extern "C"
JNIEXPORT void JNICALL
Java_kr_smu_prlab_Prlab_deleteSelf(JNIEnv *env, jobject thiz, jlong self_addr) {
    // TODO: implement deleteSelf()
    if (self_addr != 0) {
        Inference *self = (Inference *) self_addr;
        LOGE("deleted Age Gender onnx object");
        delete self;

    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_kr_smu_prlab_Prlab_run(JNIEnv *env, jobject thiz, jlong self_addr, jobject inbitmap) {
    // TODO: implement run()
    if (self_addr != 0) {
        AndroidBitmapInfo info;

        auto *self = (Inference *) self_addr;

        uint8_t *input_pixel;

        int ret;

        std::vector<float> output(2);

        if ((ret = AndroidBitmap_getInfo(env, inbitmap, &info)) < 0) {
            LOGE("Input AndroidBitmap_getInfo() failed ! error=%d", ret);
        }

        if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
            LOGE("InputBitmap format is not RGBA_8888 !");
        }

        if ((ret = AndroidBitmap_lockPixels(env, inbitmap, (void **) &input_pixel)) < 0) {
            LOGE("Input AndroidBitmap_lockPxels() failed ! error=%d", ret);
        }

//        LOGE("bitmap width %d , bitmap heigth %d, bitmap stride %d", info.width, info.height, info.stride);

        AndroidBitmap_unlockPixels(env, inbitmap);

        self->run(input_pixel, output);

        // 자바 자료형 및 메소드 생성
        jclass vectorClass = env->FindClass("java/util/Vector");
        jclass floatClass = env->FindClass("java/lang/Float");
        jmethodID initMethodID = env->GetMethodID(vectorClass, "<init>", "()V");
        jmethodID addMethodID = env->GetMethodID(vectorClass, "add", "(Ljava/lang/Object;)Z");
        // 자바용 벡터 생성
        jobject jvec = env->NewObject(vectorClass, initMethodID);
        for (float f : output) {
            jmethodID floatConstructorID = env->GetMethodID(floatClass, "<init>", "(F)V");
            jobject floatValue = env->NewObject(floatClass, floatConstructorID, f);
            env->CallBooleanMethod(jvec, addMethodID, floatValue);
        }
        env->DeleteLocalRef(vectorClass);
        env->DeleteLocalRef(floatClass);

        return jvec;
    }
}

extern "C"
JNIEXPORT jlong JNICALL
Java_kr_smu_prlab_Prlab_newSelfBP(JNIEnv *env, jobject thiz, jstring model_path) {
    // TODO: implement newSelfBP()
    std::unique_ptr<Ort::Env> environment(new Ort::Env(ORT_LOGGING_LEVEL_VERBOSE, "ONNX"));

    const char *model_path_ch = env->GetStringUTFChars(model_path, nullptr);
//    auto *self = new InferenceBloodPressure(environment, model_path_ch, signal_size);
    auto *self = new InferenceBloodPressure(environment, model_path_ch);
    return (jlong) self;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_kr_smu_prlab_Prlab_newSelfBPByteArray(JNIEnv *env, jobject thiz, jbyteArray model_byte_data,
                                           jint model_size) {
    // TODO: implement newSelfBPByteArray()
    std::unique_ptr<Ort::Env> environment(new Ort::Env(ORT_LOGGING_LEVEL_VERBOSE, "ONNX"));

    auto *self = new InferenceBloodPressure(
        environment,
        (uint8_t *) model_byte_data,
        model_size
    );

    return (jlong) self;
}

extern "C"
JNIEXPORT void JNICALL
Java_kr_smu_prlab_Prlab_deleteSelfBP(JNIEnv *env, jobject thiz, jlong self_addr) {
    // TODO: implement deleteSelfBP()
    if (self_addr != 0) {
        auto *self = (InferenceBloodPressure *) self_addr;
        LOGE("deleted BP onnx object");
        delete self;
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_kr_smu_prlab_Prlab_runBP(JNIEnv *env, jobject thiz, jlong self_addr, jfloatArray signal) {
    // TODO: implement runBP()
    if (self_addr != 0) {
        auto *self = (InferenceBloodPressure *) self_addr;

        jsize size = env ->GetArrayLength(signal);

        std::vector<float> input(size), output(2);

        env -> GetFloatArrayRegion(signal, 0, size, &input[0]);

        self->run(input, output);

        // 자바 자료형 및 메소드 생성
        jclass vectorClass = env->FindClass("java/util/Vector");
        jclass floatClass = env->FindClass("java/lang/Float");
        jmethodID initMethodID = env->GetMethodID(vectorClass, "<init>", "()V");
        jmethodID addMethodID = env->GetMethodID(vectorClass, "add", "(Ljava/lang/Object;)Z");
        // 자바용 벡터 생성
        jobject jvec = env->NewObject(vectorClass, initMethodID);
        for (float f : output) {
            jmethodID floatConstructorID = env->GetMethodID(floatClass, "<init>", "(F)V");
            jobject floatValue = env->NewObject(floatClass, floatConstructorID, f);
            env->CallBooleanMethod(jvec, addMethodID, floatValue);
        }
        env->DeleteLocalRef(vectorClass);
        env->DeleteLocalRef(floatClass);

        return jvec;
    }
}
