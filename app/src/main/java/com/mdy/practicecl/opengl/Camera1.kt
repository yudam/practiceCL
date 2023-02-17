package com.mdy.practicecl.opengl

import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Log
import android.view.Surface

/**
 * User: maodayu
 * Date: 2022/6/20
 * Time: 11:17
 */
class Camera1(val context: Context) {

    private lateinit var camera: Camera

    fun open(): Camera1 {
        camera = Camera.open()
        val params = camera.parameters
        val supportedPreviewSizes = params.supportedPreviewSizes

        params.setPreviewSize(supportedPreviewSizes[0].width, supportedPreviewSizes[0].height)
        camera.parameters = params
        return this
    }


    fun setSurfaceTexture(surfaceTexture: SurfaceTexture): Camera1 {
        camera.setPreviewTexture(surfaceTexture)
        return this
    }

    fun startPreview(): Camera1 {
        camera.startPreview()

        val mSize = camera.parameters.previewSize
        return this
    }


    fun getSize(): IntArray {
        val size = IntArray(2)
        val mSize = camera.parameters.previewSize
        size[0] = mSize.width
        size[1] = mSize.height
        return size
    }

    private fun setDisplayOrientation(camera: Camera, info: Camera.CameraInfo) {
        val activity = context as Activity
        val rotation: Int = activity.windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        var result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360
        } else {
            result = (info.orientation - degrees + 360) % 360
        }
        camera.setDisplayOrientation(result)
    }

    private fun getCameraInfo(): List<Camera.CameraInfo> {
        val cameraList = mutableListOf<Camera.CameraInfo>()
        val cameraCount = Camera.getNumberOfCameras()
        Log.i("MDY", "cameraCount:" + cameraCount)
        for (index in 0 until cameraCount) {
            val cameraInfo = Camera.CameraInfo()
            Camera.getCameraInfo(index, cameraInfo)
            Log.i("MDY", "orientation:" + cameraInfo.orientation + "   facing:" + cameraInfo.facing)
            cameraList.add(cameraInfo)
        }
        return cameraList
    }
}