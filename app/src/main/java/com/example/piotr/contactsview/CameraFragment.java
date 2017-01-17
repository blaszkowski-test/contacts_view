package com.example.piotr.contactsview;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


public class CameraFragment extends Fragment
{
    private boolean cameraSupported;
    private Camera camera;
    private CameraPreview cameraPreview;

    public CameraFragment()
    {
        // Required empty public constructor
    }

    public static CameraFragment newInstance()
    {
        CameraFragment fragment = new CameraFragment();
        return fragment;
    }

    private boolean checkCameraHardware(Context context)
    {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
        {

            return true;
        }
        else
        {

            return false;
        }
    }

    public void getCameraInstance()
    {
        try
        {
            camera = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e)
        {
            // Camera is not available (in use or does not exist)
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        cameraSupported = checkCameraHardware(getActivity());
        if (cameraSupported)
        {
            getCameraInstance();

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                camera.setDisplayOrientation(90);
            }
            else
            {
                camera.setDisplayOrientation(180);
            }

            Camera.Parameters params = camera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            //params.set("camera-id",2);
            camera.setParameters(params);

            cameraPreview = new CameraPreview(getActivity(), camera);
            FrameLayout preview = (FrameLayout) getActivity().findViewById(R.id.camera_preview);
            preview.addView(cameraPreview);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }


    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

    }

    @Override
    public void onPause()
    {
        super.onPause();

        if (camera != null)
        {
            camera.release();
        }
    }

}
