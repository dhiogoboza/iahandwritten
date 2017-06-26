package br.ufrn.ia.nnhandwritten;


import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;

/**
 * Created by dhiogoboza on 13/06/17.
 */

public class CameraManager {

    private Camera mCamera;
    private Context context;

    public CameraManager(Context context) {
        this.context = context;

        mCamera = null;
        try {
            mCamera = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
    }

    public boolean checkCameraHardware() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public Camera getCamera() {
        return mCamera; // returns null if camera is unavailable
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.unlock();
            mCamera = null;
        }
    }
}
