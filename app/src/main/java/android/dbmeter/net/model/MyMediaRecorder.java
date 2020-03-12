package android.dbmeter.net.model;

import android.media.MediaRecorder;

import java.io.IOException;

public class MyMediaRecorder {
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false ;

    public int getMaxAmplitude() {
        if (mMediaRecorder != null) {
            return mMediaRecorder.getMaxAmplitude();
        }
        else {
            return -1;
        }
    }

    private void setUpRecorder(){
        mMediaRecorder = new MediaRecorder();

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setOutputFile("/dev/null");
    }

    public boolean startRecorder(){
        try {
            setUpRecorder();

            mMediaRecorder.prepare();
            mMediaRecorder.start();
            isRecording = true;

            return true;
        }
        catch(IOException e) {
            isRecording = false;
            e.printStackTrace();
        }

        return false;
    }

    public void stopRecorder(){
        if (mMediaRecorder != null){
            if(isRecording){
                try{
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    mMediaRecorder.release();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            isRecording = false;
        }
    }

    public void restartRecorder(){
        stopRecorder();
        startRecorder();
    }
}